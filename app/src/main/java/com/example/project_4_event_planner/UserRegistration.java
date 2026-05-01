package com.example.project_4_event_planner;

import java.io.IOException;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static java.lang.Character.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserRegistration extends AppCompatActivity {
    private final Database db = new Database(UserRegistration.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        EditText input_fullname = findViewById(R.id.editFullName);
        TextView displayAge = findViewById(R.id.showAge);
        displayAge.setText(R.string.prompt_display_age);
        EditText input_username = findViewById(R.id.editUsername);
        EditText input_password = findViewById(R.id.editPassword);

        Calendar cal = Calendar.getInstance();
        int currYear = cal.get(Calendar.YEAR);
        int currMonth = cal.get(Calendar.MONTH);
        int currDay = cal.get(Calendar.DAY_OF_MONTH);

        DatePicker dp = findViewById(R.id.BirthDatePicker);

        // Restrict users from being younger than 13
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 13);
        dp.setMaxDate(cal.getTimeInMillis());
        dp.init((currYear - 13), currMonth, currDay, null);
        dp.setOnDateChangedListener((datePicker, y, m, d) -> {
            cal.set(y, m, d);
            int estimatedAge = (currYear - y);
            if(currMonth < m || (currMonth == m && currDay < d)) {
                estimatedAge--;
            }
            displayAge.setText(String.valueOf(estimatedAge));
            Log.d("Birthdate", "=== "+ new SimpleDateFormat("MM-dd-yyyy",
                    Locale.getDefault()).format(cal.getTime()));
        });

        Button submit = findViewById(R.id.submitInfoButton);
        submit.setOnClickListener(v -> {
            String fullname = input_fullname.getText().toString();
            String birthdate = new SimpleDateFormat("MM-dd-yyyy",
                    Locale.getDefault()).format(cal.getTime());
            String checkUsername = input_username.getText().toString().replaceAll("\\s", "");
            String checkPassword = input_password.getText().toString().replaceAll("\\s", "");

            if ((fullname.isEmpty()) || (birthdate.isEmpty())
                    || (checkUsername.isEmpty()) || (checkPassword.isEmpty())) {
                Log.d("Register.Fields", "===== Field is Empty");
                AlertDialog voidFields = new AlertDialog.Builder(UserRegistration.this)
                        .setTitle("Invalid Registration:\nEmpty Input Fields")
                        .setMessage("Please fill all fields before proceeding to register!")
                        .setIcon(R.drawable.sharp_sentiment_neutral_24)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok,
                                (dialogInterface, which) ->
                                        dialogInterface.cancel()).create();
                voidFields.show();
            } else if (!validUsername(checkUsername)) {
                Log.d("Register.Username", "===== Not Valid");
                AlertDialog invalidInput = new AlertDialog.Builder(UserRegistration.this)
                        .setTitle("Invalid Registration: Username")
                        .setMessage("Username must have a length of AT LEAST 5, not including spaces.")
                        .setIcon(R.drawable.sharp_sentiment_neutral_24)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok,
                                (dialogInterface, which) ->
                                        dialogInterface.cancel()).create();
                invalidInput.show();
            } else if (existsUsername(checkUsername)) {
                Log.d("Register.Username", "===== Existing User");
                AlertDialog invalidInput = new AlertDialog.Builder(UserRegistration.this)
                        .setTitle("Invalid Registration: Existing Username")
                        .setMessage("Sorry, '" + checkUsername + "' is either similar to or being used by another user!")
                        .setIcon(R.drawable.sharp_sentiment_neutral_24)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok,
                                (dialogInterface, which) ->
                                        dialogInterface.cancel()).create();
                invalidInput.show();
            } else if (!validPassword(checkPassword)) {
                Log.d("Register.Password", "===== Not Valid");
                AlertDialog invalidInput = new AlertDialog.Builder(UserRegistration.this)
                        .setTitle("Invalid Registration: Password")
                        .setMessage("Password must have a length of AT LEAST 8, not including spaces, and have at least one number.")
                        .setIcon(R.drawable.sharp_sentiment_neutral_24)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok,
                                (dialogInterface, which) ->
                                        dialogInterface.cancel()).create();
                invalidInput.show();
            } else {
                try {
                    if(db.insertData(0, new String[]{checkUsername, fullname, birthdate, checkPassword}) != -1 &&
                            writeToUserJSON(checkUsername, fullname, birthdate)) {
                        String welcome_message = "Welcome " + fullname.split(" ")[0] + '!';
                        Toast.makeText(UserRegistration.this, welcome_message,
                                Toast.LENGTH_LONG).show();
                        db.insertData(5, new String[]{checkUsername, fullname, null, null});
                        Log.d("Register", "===== NEW USER!");

                        Intent goToLogin = new Intent(getApplicationContext(), Login.class);
                        startActivity(goToLogin);
                    } else {
                        Log.d("Register", "===== ERROR!");
                    }
                } catch (NoSuchAlgorithmException | JSONException | IOException e) {
                    throw new RuntimeException(e);
                }

                // Reset EditText values
                input_fullname.setText("");
                displayAge.setText(R.string.prompt_display_age);
                dp.updateDate(Calendar.getInstance().get(Calendar.YEAR) - 13,
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                input_username.setText("");
                input_password.setText("");
            }
        });
    }

    public static Boolean containsNumeric(String pass) {
        for (char c : pass.toCharArray()) {
            if (isDigit(c)) { return true; }
        }
        return false;
    }

    // Username must be at least 5 characters
    public static Boolean validUsername(String user) {
        Log.d("valid.User", "====="+user);
        return (user.length() >= 5 && (!user.contains(" ")));
    }

    // Password must be at least 8 characters (no spaces), and alphanumeric
    public static Boolean validPassword(String pass) {
        Log.d("valid.Pass", "====="+pass);
        return (pass.length() >= 8) && (containsNumeric(pass)) && (!pass.contains(" "));
    }

    /**
     * Checks if a username is in use or not. Not case-sensitive!
     * @param user username to be searched for in SQLiteDatabase
     * @return false if a username (not case-sensitive) is taken
     */
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

    private static boolean writeToUserJSON(String user, String name, String birthday)
            throws JSONException, IOException {
        JSONObject userJSON = new JSONObject();
        userJSON.put("Username", user);
        userJSON.put("Full_Name", name);
        userJSON.put("Birthdate", birthday);
        userJSON.put("Friends", new JSONArray());
        userJSON.put("User_Groups", new JSONArray());

        return JSONInteract.writeToJSONFile(0, userJSON);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        db.close();
        Intent toMain = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(toMain);
    }
}