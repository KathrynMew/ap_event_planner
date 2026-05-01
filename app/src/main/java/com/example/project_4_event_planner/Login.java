package com.example.project_4_event_planner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Login extends AppCompatActivity {
    private final Database db = new Database(Login.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Keep track of how many attempts have been used. Maximum attempts: 3.
        AtomicInteger attempts = new AtomicInteger();

        EditText input_username = findViewById(R.id.editLoginUsername);
        EditText input_password = findViewById(R.id.editLoginPassword);

        Button login = findViewById(R.id.goToHomeButton);
        login.setOnClickListener(v -> {
            String username = input_username.getText().toString().replaceAll("\\s", "");
            String password = input_password.getText().toString().replaceAll("\\s", "");

            // Check if entered username exists in the database
            if(existsUsername(username)) {
                try {
                    // Check if entered password is correct
                    if(matches(username, password)) {
                        SharedPreferences sp = getSharedPreferences("current_user", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("username", username);
                        // SELECT name FROM Users WHERE username='<username>'
                        editor.putString("fullname", db.selectQuery(0, username, new int[]{1}).get(0));
                        editor.apply();

                        Log.d("Login.SP", "=== contains User:"+ sp.contains("username"));
                        Log.d("Login.SP", "=== contains Name:"+ sp.contains("fullname"));
                        Intent goToProfile = new Intent(getApplicationContext(), Profiles.class);
                        startActivity(goToProfile);
                    } else {
                        attempts.incrementAndGet();
                        if (attempts.get() == 3) {
                            setPasswordFailureAlarm(username);
                            AlertDialog wrongPassword = new AlertDialog.Builder(Login.this)
                                    .setTitle("Invalid Login: Incorrect Password")
                                    .setIcon(R.drawable.sharp_sentiment_sad_24)
                                    .setMessage("You've made 3 attempts. Please ensure that the username you input is correct.")
                                    .setCancelable(true)
                                    .setPositiveButton(android.R.string.ok, (dialogInterface, which) ->
                                            dialogInterface.cancel()).create();
                            wrongPassword.show();
                        } else {
                            AlertDialog wrongPassword = new AlertDialog.Builder(Login.this)
                                    .setTitle("Invalid Login: Incorrect Password")
                                    .setIcon(R.drawable.baseline_sentiment_dissatisfied_24)
                                    .setMessage("Your attempt to sign in with: "+password+" is incorrect.")
                                    .setCancelable(true)
                                    .setPositiveButton(android.R.string.ok, (dialogInterface, which) ->
                                            dialogInterface.cancel()).create();
                            wrongPassword.show();
                        }
                        Log.d("Login.Password", "===== Incorrect Password");
                    }
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } else {
                AlertDialog noUserFound = new AlertDialog.Builder(Login.this)
                        .setTitle("No User Found!")
                        .setIcon(R.drawable.person_not_found)
                        .setMessage("There is no existing user with username "+username+". Would you like to sign up or try again?")
                        .setCancelable(false)
                        .setPositiveButton(R.string.signup, (dialogInterface, which) -> {
                            dialogInterface.dismiss();
                            Intent goToSignUp = new Intent(getApplicationContext(), UserRegistration.class);
                            startActivity(goToSignUp);
                        })
                        .setNegativeButton(R.string.newAttempt, (dialogInterface, which) ->
                                dialogInterface.cancel()).create();
                noUserFound.show();

                Log.d("Login.Username", "===== No match");
            }
        });
    }

    private void setPasswordFailureAlarm(String user) {
        Intent pass = new Intent(Login.this, BroadcastReceiverPassword.class);
        pass.putExtra("attemptedUsername", user);
        pass.putExtra("notificationID", System.currentTimeMillis());
        PendingIntent _pi = PendingIntent.getBroadcast(Login.this, 0, pass,
                PendingIntent.FLAG_IMMUTABLE);

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis(), _pi);
    }

    private Boolean existsUsername(String user) {
        // SELECT username FROM Users WHERE username='<user>'
        ArrayList<String> res = db.selectQuery(0, user, new int[]{0});
        for(String u : res) {
            if(u.equalsIgnoreCase(user)) {
                return true;
            }
        }
        Log.d("exists.User", "====="+user);
        Log.d("exists.DB", "====="+res);
        return false;
    }

    private Boolean matches(String user, String pass)
            throws NoSuchAlgorithmException {
        byte[] res = db.getPasswordEntry(user);
        byte[] hash_pass = Database.messageDigest(pass);
        Log.d("Login.DB", "====="+Arrays.toString(res));
        Log.d("Login.Password", "====="+pass);
        return (Arrays.equals(res, hash_pass));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        db.close();
        Intent toMain = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(toMain);
    }
}