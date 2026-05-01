package com.example.project_4_event_planner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import android.content.SharedPreferences;
import android.os.Bundle;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class AddFriends extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private static final ArrayList<String> userFriends = new ArrayList<>();
    private static final ArrayList<String> RELATION_TAGS = new ArrayList<>();
    private static final ArrayList<String> toDisplay = new ArrayList<>();
    private static final ArrayList<String> canSearchFor = new ArrayList<>();
    private static String logged_in;
    private static String toAdd;
    private final Database db = new Database(AddFriends.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        SharedPreferences user_sp = getSharedPreferences("current_user", MODE_PRIVATE);
        logged_in = user_sp.getString("username", null);
        setUserFriends();
        filterOut();

        EditText search_input = findViewById(R.id.editSearchFriend);
        search_input.setOnEditorActionListener((textView, action_id, keyEvent) -> {
            if(action_id == EditorInfo.IME_ACTION_DONE) {
                findSearchResults();
                Log.d("ActionDone: Reached", "=== "+search_input.getText().toString());
            }
            return false;
        });

        ImageButton apply_search = findViewById(R.id.applySearchButton);
        apply_search.setOnClickListener(view -> findSearchResults());

        CheckBox _casual = findViewById(R.id.casualFriendTag);
        CheckBox _school = findViewById(R.id.schoolFriendTag);
        CheckBox _work = findViewById(R.id.workFriendTag);
        CheckBox _online = findViewById(R.id.onlineFriendTag);
        _casual.setOnCheckedChangeListener(this);
        _school.setOnCheckedChangeListener(this);
        _work.setOnCheckedChangeListener(this);
        _online.setOnCheckedChangeListener(this);

        setSearchResults();

        Button _send = findViewById(R.id.buttonAddFriend);
        _send.setOnClickListener(view -> {
            for(String t : RELATION_TAGS) {
                try {
                    long row = db.insertData(4, new String[]{logged_in, toAdd, t});
                    Log.d("AddFriends: user", "=== "+row);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
            Toast.makeText(AddFriends.this,
                    "Friend Request sent to "+toDisplay.get(0), Toast.LENGTH_SHORT).show();

            finish();
            startActivity(getIntent());
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton box, boolean isChecked) {
        String _tag = box.getText().toString();
        if(isChecked && !RELATION_TAGS.contains(_tag)) {
            RELATION_TAGS.add(_tag);
            Log.d("AddFriend: Tag Added", "=== "+_tag);
        } else {
            RELATION_TAGS.remove(_tag);
            Log.d("AddFriend: Tag Removed", "=== "+_tag);
        }
    }
    private void setUserFriends() {
        ArrayList<String> temp = db.selectQuery(3, logged_in, new int[]{1});
        for(int i = 0; i < temp.size(); i++) {
            String u = temp.get(i);
            String full = db.selectQuery(0, u, new int[]{1}).get(0);
            userFriends.add(full.concat("\n@"+u));
        }
    }

    // TODO: Fix this issue: java.util.ConcurrentModificationException
    /*private ArrayList<String> filterOut() {
        ArrayList<String> allUsers = db.selectAllUsersBut(logged_in);
        ArrayList<String> existingRequests = db.selectRequests(logged_in, true);
        existingRequests.addAll(db.selectRequests(logged_in, false));

        for(String lookFor : allUsers) {
            if(!userFriends.contains(lookFor) && !existingRequests.contains(lookFor)) {
                // allUsers.remove(lookFor);

                Log.d("All Users: Removed", "=== "+lookFor);
            }
        }
        return allUsers;
    }*/

    private void filterOut() {
        ArrayList<String> allUsers = db.selectAllUsersBut(logged_in);
        ArrayList<String> existingRequests = db.selectRequests(logged_in, true);
        existingRequests.addAll(db.selectRequests(logged_in, false));

        for(String lookFor : allUsers) {
            if(!userFriends.contains(lookFor) && !existingRequests.contains(lookFor)) {
                // allUsers.remove(lookFor);
                canSearchFor.add(lookFor);
                Log.d("All Users: Removed", "=== "+lookFor);
            }
        }
    }

    private boolean contains(String full_string, ArrayList<String> sequential) {
        for(String s : sequential) {
            if(full_string.contains(s)) { return true; }
        }
        return false;
    }

    private boolean equalsIgnoreCase(String[] _full, String _sub) {
        for(String f : _full) {
            if(_sub.equalsIgnoreCase(f)) { return true; }
        }
        return false;
    }

    private void findSearchResults() {
        EditText search_input = findViewById(R.id.editSearchFriend);
        String searchContents = search_input.getText().toString();

        if(toAdd != null) {
            Log.d("Search: Add", "=== "+toAdd);
            Toast.makeText(AddFriends.this,
                    "Cannot change search results unless added person is removed!",
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (searchContents.isEmpty()) {
            toDisplay.clear();
            setSearchResults();
            return;
        }

        ArrayList<String> sequential_matches = new ArrayList<>();
        for (int i = 1; i < searchContents.length(); i++) {
            sequential_matches.add(searchContents.toLowerCase().substring(0, i));
        }

        for(String u : canSearchFor) {
            Log.d("Search: User", "=== "+u);
            if(!toDisplay.contains(u)) {
                String[] u_arr = u.split("\n@");
                String[] full_name = u_arr[0].split(" ");
                if (equalsIgnoreCase(full_name, searchContents) ||
                        u_arr[1].equalsIgnoreCase(searchContents)) {
                    Log.d("Search: Match", "=== "+ searchContents.concat(", "+u));
                    toDisplay.add(u);
                } else if (contains(u_arr[0].toLowerCase(), sequential_matches) ||
                        contains(u_arr[1].toLowerCase(), sequential_matches)) {
                    Log.d("Search: Possible", "=== "+ searchContents.concat(", "+u));
                    toDisplay.add(u);
                }
            }
        }
        setSearchResults();
    }

    /**
     * Sets the RecyclerView to display filtered search results
     */
    private void setSearchResults() {
        Log.d("AddFriends", ":: setSearchResults()");
        TextView _no_results = findViewById(R.id.noResultsSearch);

        RecyclerView rv = findViewById(R.id.displayPossibleMatches);
        rv.setLayoutManager(new LinearLayoutManager(AddFriends.this));
        if (toDisplay.isEmpty()) {
            _no_results.setVisibility(View.VISIBLE);
        } else {
            _no_results.setVisibility(View.GONE);
        }

        rv.setAdapter(new AdapterAddFriends(toDisplay, (person, button_id) -> {
            if(button_id == R.id.buttonAddPerson) {
                if(toAdd == null) {
                    toAdd = person.split("\n@")[1];
                } else {
                    Toast.makeText(AddFriends.this,
                            "Sorry, you can only friend one person at a time for now!",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (button_id == R.id.buttonCancelRequest) {
                toAdd = null;
            }
            setSearchResults();
        }, toAdd));
    }

    /**
     * Reset all global variables
     */
    public void finish() {
        userFriends.clear();
        toDisplay.clear();
        RELATION_TAGS.clear();
        canSearchFor.clear();
        toAdd = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        db.close();
        Intent goToFriends = new Intent(getApplicationContext(), Friends.class);
        startActivity(goToFriends);
    }
}