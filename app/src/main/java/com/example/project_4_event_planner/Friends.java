package com.example.project_4_event_planner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Friends extends AppCompatActivity {

    private static final JSONObject friends = new JSONObject();
    private static final ArrayList<String> friend_keys = new ArrayList<>();
    private static ArrayList<String> toDisplay = new ArrayList<>();
    private static String logged_in;
    private final Database db = new Database(Friends.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        SharedPreferences user_sp = getSharedPreferences("current_user", MODE_PRIVATE);
        logged_in = user_sp.getString("username", null);

        View tag_casual = findViewById(R.id.CasualTag);
        View tag_school = findViewById(R.id.SchoolTag);
        View tag_work = findViewById(R.id.WorkTag);
        View tag_online = findViewById(R.id.OnlineTag);

        TextView _no_results = findViewById(R.id.noResultsFriends);
        _no_results.setVisibility(View.VISIBLE);

        // NOTE: Maps all friend tags to user friends such that
        // {"full name\n@username":["casual", "school", "work", "online"]}
        ArrayList<String> temp = db.selectWithTags(logged_in);
        if (temp.size() > 0) {
            _no_results.setVisibility(View.GONE);
        }
        // t = full name+"\n@"+username+':'+relation_type;
        for(String t : temp) {
            String[] t_arr = t.split(":");
            // t_arr = {full name+"\n@"+username, relation_type}
            try {
                if(friends.has(t_arr[0])) {
                    friends.getJSONArray(t_arr[0]).put(t_arr[1]);
                } else {
                    friend_keys.add(t_arr[0]);
                    friends.put(t_arr[0], new JSONArray().put(t_arr[1]));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        Button goToAddFriend = findViewById(R.id.addFriend);
        goToAddFriend.setOnClickListener(view -> {
            finish();
            Intent adding = new Intent(getApplicationContext(), AddFriends.class);
            startActivity(adding);
        });

        Button goToViewRequests = findViewById(R.id.viewRequests);
        goToViewRequests.setOnClickListener(view -> {
            finish();
            Intent requests = new Intent(getApplicationContext(), FriendRequests.class);
            startActivity(requests);
        });

        Button goToViewGroups = findViewById(R.id.viewGroups);
        goToViewGroups.setOnClickListener(view -> {
            finish();
            Intent viewing = new Intent(getApplicationContext(), Groups.class);
            startActivity(viewing);
        });

        RadioGroup tag_selection = findViewById(R.id.relationTypeRadioGroup);
        tag_selection.clearCheck();
        tag_selection.setOnCheckedChangeListener((typesGroup, id) -> {
            if(id == R.id.radiobuttonAll) {
                tag_casual.setVisibility(View.VISIBLE);
                tag_school.setVisibility(View.VISIBLE);
                tag_work.setVisibility(View.VISIBLE);
                tag_online.setVisibility(View.VISIBLE);
                toDisplay = new ArrayList<>(friend_keys);
                Log.d("Friends: toDisplay Size", "=== "+toDisplay.size());
            } else {
                toDisplay.clear();
                RadioButton tag_rb = findViewById(id);
                String tag_text = tag_rb.getText().toString();
                Log.d("Friends: Relation Tag", "=== "+tag_text);

                if(tag_text.equals(tag_casual.getContentDescription().toString())) {
                    tag_casual.setVisibility(View.VISIBLE);
                    tag_school.setVisibility(View.GONE);
                    tag_work.setVisibility(View.GONE);
                    tag_online.setVisibility(View.GONE);
                } else if (tag_text.equals(tag_school.getContentDescription().toString())) {
                    tag_casual.setVisibility(View.GONE);
                    tag_school.setVisibility(View.VISIBLE);
                    tag_work.setVisibility(View.GONE);
                    tag_online.setVisibility(View.GONE);
                } else if (tag_text.equals(tag_work.getContentDescription().toString())) {
                    tag_casual.setVisibility(View.GONE);
                    tag_school.setVisibility(View.GONE);
                    tag_work.setVisibility(View.VISIBLE);
                    tag_online.setVisibility(View.GONE);
                } else if (tag_text.equals(tag_online.getContentDescription().toString())) {
                    tag_casual.setVisibility(View.GONE);
                    tag_school.setVisibility(View.GONE);
                    tag_work.setVisibility(View.GONE);
                    tag_online.setVisibility(View.VISIBLE);
                }

                for(String k : friend_keys) {
                    try {
                        if(JSONInteract.contains(friends.getJSONArray(k),
                                tag_rb.getText().toString())) {
                            toDisplay.add(k);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            // Display Friends in Recycler
            setFriendRecycler();
        });
        tag_selection.check(R.id.radiobuttonAll);
    }

    private void setFriendRecycler() {
        RecyclerView rv = findViewById(R.id.recyclerviewFriends);
        rv.setLayoutManager(new LinearLayoutManager(Friends.this));
        rv.setAdapter(new AdapterFriendsList(friends, toDisplay, (selected, button) -> {
            // full name \n@ username
            String[] friend_arr = selected.split("\n@");
            String friend_username = friend_arr[1];
            if(button == R.id.removeFriendButton) {
                AlertDialog confirmRemove = new AlertDialog.Builder(Friends.this)
                        .setTitle("Remove Friend")
                        .setMessage("Are you sure you'd like to unfriend "+friend_arr[0]+"?")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> {
                            int user_modded = db.deleteData(3, logged_in, friend_username,
                                    null, null);
                            int frnd_modded = db.deleteData(3, friend_username, logged_in,
                                    null, null);
                            Log.d("Friends: Remove entry", "=== "+user_modded+frnd_modded);
                            try {
                                if(user_modded+frnd_modded > 0) {
                                    if(editFriendsInUserJSON(friend_username)) {
                                        Toast.makeText(Friends.this,
                                                friend_arr[0] + " removed from friend list!",
                                                Toast.LENGTH_SHORT).show();
                                        friends.remove(selected);
                                        friend_keys.remove(selected);
                                        toDisplay.remove(selected);
                                        Log.d("Friends: Keys Size", "=== "+friend_keys.size());
                                        setFriendRecycler();
                                        dialogInterface.dismiss();
                                    }
                                }
                            } catch (IOException | JSONException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .setNegativeButton(android.R.string.no, (dialogInterface, which) ->
                                dialogInterface.cancel()).create();
                confirmRemove.show();
            } else if (button == R.id.viewFriendProfileButton) {
                // TODO: Add UserProfile Picture
                ArrayList<String> _profile = db.selectQuery(5, friend_username, new int[]{1,2});
                // Populate Profile view with friend's profile in database
                View friend_profile_layout = getLayoutInflater()
                        .inflate(R.layout.dialog_friend_profile, null);
                // Full Name
                TextView _display_profile_name = friend_profile_layout.findViewById(R.id.displayFullName);
                _display_profile_name.setText(_profile.get(0));
                // Username
                TextView _display_username = friend_profile_layout.findViewById(R.id.displayUsername);
                _display_username.setText(("@").concat(friend_username));
                // About Me
                TextView aboutMe = friend_profile_layout.findViewById(R.id.displayAboutMe);
                aboutMe.setText(_profile.get(1));
                // Number of Events
                TextView num_events = friend_profile_layout.findViewById(R.id.displayNumberOfEvents);
                num_events.setText(String.valueOf(db.getCount(1, friend_username, new String[]{db.getColEventId()})));
                // Number of Friends
                TextView num_friends = friend_profile_layout.findViewById(R.id.displayNumberOfFriends);
                num_friends.setText(String.valueOf(db.getCount(3, friend_username, new String[]{db.getColFriend()})));
                // Number of Groups
                TextView num_groups = friend_profile_layout.findViewById(R.id.displayNumberOfGroups);
                int _count;
                try {
                    JSONObject userJSON = JSONInteract.getJSONObject(0, "Username", friend_username);
                    _count = userJSON.getJSONArray("User_Groups").length();
                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                }
                num_groups.setText(String.valueOf(_count));

                // Display AlertDialog with results
                AlertDialog _dialog = new AlertDialog.Builder(Friends.this)
                        .setView(friend_profile_layout).create();
                _dialog.show();
            }
        }));
    }

    /**
     * Edit the JSONArray stored with name "Friends" in JSONFile to remove friends
     * @param some_user the "ex-friend" of the current logged_in user
     * @return true if successful
     * @throws IOException
     * @throws JSONException
     */
    private static boolean editFriendsInUserJSON(String some_user)
            throws IOException, JSONException {
        String removing;
        if(some_user.contains("\n@")) {
            removing = some_user.split("\n@")[1];
        } else {
            removing = some_user;
        }

        // Edit `Friends` for User
        JSONObject u = JSONInteract.getJSONObject(0, "Username", logged_in);
        JSONArray user_arr = u.getJSONArray("Friends");
        for(int i = 0; i < u.length(); i++) {
            if(user_arr.getString(i).equals(removing)) {
                u.getJSONArray("Friends").remove(i);
                Log.d("Friends: Removed Index", "=== "+i);
                break;
            }
        }
        if(JSONInteract.writeToJSONFile(0, u, logged_in)) {
            JSONObject r = JSONInteract.getJSONObject(0, "Username", removing);
            JSONArray r_arr = r.getJSONArray("Friends");
            for(int i = 0; i < r_arr.length(); i++) {
                if(r_arr.getString(i).equals(logged_in)) {
                    r.getJSONArray("Friends").remove(i);
                    Log.d("Friends: Removed Index", "=== "+i);
                    return JSONInteract.writeToJSONFile(0, r, removing);
                }
            }
        }
        return false;
    }

    /**
     * Manual reset of global variables.
     */
    @Override
    public void finish() {
        friend_keys.clear();
        toDisplay.clear();

        try {
            if(friends.length() > 0) {
                JSONArray _keys = friends.names();
                for (int i = 0; i < Objects.requireNonNull(_keys).length(); i++) {
                    friends.remove(_keys.getString(i));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        db.close();
        Intent goToProfile = new Intent(getApplicationContext(), Profiles.class);
        startActivity(goToProfile);
    }
}