package com.example.project_4_event_planner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Profiles extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        SharedPreferences user_sp = getSharedPreferences("current_user", MODE_PRIVATE);
        String logged_in = user_sp.getString("username", "");

        Database db = new Database(Profiles.this);
        int _count;
        try {
            JSONObject userJSON = JSONInteract.getJSONObject(0, "Username", logged_in);
            _count = userJSON.getJSONArray("User_Groups").length();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }

        ArrayList<String> _profile = db.selectQuery(5, logged_in, new int[]{1,2});
        Log.d("Profile Details", "=== "+_profile.size());
        Log.d("Display Name", "=== "+_profile.get(0));
        Log.d("About Me", "=== "+_profile.get(1));

        ImageView profilePic = findViewById(R.id.userProfilePicture);
        profilePic.setOnClickListener(view -> {});

        TextView _display_profile_name = findViewById(R.id.displayFullName);
        _display_profile_name.setText(_profile.get(0));
        _display_profile_name.setOnClickListener(view -> {
            View _edittext = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, null);
            EditText _text = _edittext.findViewById(R.id.editTextBox);
            _text.setText(_display_profile_name.getText().toString());
            AlertDialog _edit_dialog = new AlertDialog.Builder(Profiles.this)
                    .setTitle("Edit Display Name")
                    .setMessage("Users will see this display name if they look at your profile, but full name will be seen for any searches")
                    .setView(_edittext)
                    .setPositiveButton("Apply", (dialogInterface, i) -> {
                        ContentValues content = new ContentValues();
                        content.put(db.getColDisplayName(), _text.getText().toString());
                        int _mod = db.updateData(5, content, logged_in, null, null, null);
                        Log.d("Updated 1 Profile?", "=== "+(_mod == 1));

                        _display_profile_name.setText(_text.getText().toString());
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) ->
                            dialogInterface.cancel())
                    .create();
            _edit_dialog.show();
        });

        TextView _display_username = findViewById(R.id.displayUsername);
        _display_username.setText(("@").concat(logged_in));

        if (_profile.get(1) == null) {
            AlertDialog _prompt = new AlertDialog.Builder(Profiles.this)
                    .setTitle("Personal 'About Me'")
                    .setMessage("You may be a new user! You can tap the profile picture, display full name, and about me sections to edit them!")
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        _profile.set(1, "");
                        dialogInterface.cancel();
                    })
                    .create();
            _prompt.show();
        }

        TextView aboutMe = findViewById(R.id.displayAboutMe);
        aboutMe.setText(_profile.get(1));
        aboutMe.setOnClickListener(view -> {
            View _edittext = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, null);
            EditText _text = _edittext.findViewById(R.id.editTextBox);
            _text.setHint(R.string.hint_about_me);
            _text.setText(aboutMe.getText().toString());
            AlertDialog _edit_dialog = new AlertDialog.Builder(Profiles.this)
                    .setTitle("Edit About Me")
                    .setView(_edittext)
                    .setPositiveButton("Apply", (dialogInterface, i) -> {
                        ContentValues content = new ContentValues();
                        content.put(db.getColAboutMe(), _text.getText().toString());
                        int _mod = db.updateData(5, content, logged_in, null, null, null);
                        Log.d("Updated 1 Profile?", "=== "+(_mod == 1));

                        aboutMe.setText(_text.getText().toString());
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) ->
                            dialogInterface.cancel())
                    .create();
            _edit_dialog.show();
        });

        TextView num_events = findViewById(R.id.displayNumberOfEvents);
        num_events.setText(String.valueOf(db.getCount(1, logged_in, new String[]{db.getColEventId()})));

        TextView num_friends = findViewById(R.id.displayNumberOfFriends);
        num_friends.setText(String.valueOf(db.getCount(3, logged_in, new String[]{db.getColFriend()})));

        TextView num_groups = findViewById(R.id.displayNumberOfGroups);
        num_groups.setText(String.valueOf(_count));

        CardView navi_events = findViewById(R.id.displayReportEvents);
        navi_events.setOnClickListener(view -> {
            Intent goToEvents = new Intent(getApplicationContext(), Events.class);
            startActivity(goToEvents);
        });

        CardView navi_friends = findViewById(R.id.displayReportFriends);
        navi_friends.setOnClickListener(view -> {
            Intent goToFriends = new Intent(getApplicationContext(), Friends.class);
            startActivity(goToFriends);
        });

        CardView navi_groups = findViewById(R.id.displayReportGroups);
        navi_groups.setOnClickListener(view -> {
            Intent goToGroups = new Intent(getApplicationContext(), Groups.class);
            startActivity(goToGroups);
        });

        TextView _no_results = findViewById(R.id.noResultsProfiles);
        _no_results.setVisibility(View.VISIBLE);

        RecyclerView rv = findViewById(R.id.displayCurrentDayEvents);
        rv.setLayoutManager(new LinearLayoutManager(Profiles.this));
        ArrayList<String> _upcoming = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        try {
            // Get user's events for the next 7 days
            for(int i = 0; i < 7; i++) {
                String _date = new SimpleDateFormat("MM/dd/yyyy",
                        Locale.getDefault()).format(c.getTime());
                _upcoming.addAll(db.selectEventsWithinWeek(logged_in, _date));
                Log.d("Upcoming Size", "=== "+_upcoming.size());
                // Check if the day is the last day of the month
                if (c.getActualMaximum(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH)) {
                    c.roll(Calendar.MONTH, 1);
                    c.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    c.roll(Calendar.DAY_OF_MONTH, 1);
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        rv.setAdapter(new AdapterUpcoming(_upcoming));
        if (_upcoming.size() > 0) {
            _no_results.setVisibility(View.GONE);
        }

        Button _logout = findViewById(R.id.buttonLogout);
        _logout.setOnClickListener(view -> {
            db.close();
            Intent goToLogin = new Intent(getApplicationContext(), Login.class);
            goToLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(goToLogin);
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        // Ignore backPressed
    }
}