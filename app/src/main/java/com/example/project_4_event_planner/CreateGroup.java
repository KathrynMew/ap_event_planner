package com.example.project_4_event_planner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Views Needed:
 * EditText - Group Name
 * RecyclerView with Checkbox - Add Friends
 */
public class CreateGroup extends AppCompatActivity {
    private static String logged_in;
    private static final int seed = 1000000000;
    private final Database db = new Database(CreateGroup.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        SharedPreferences user_sp = getSharedPreferences("current_user", MODE_PRIVATE);
        logged_in = user_sp.getString("username", null);

        EditText name = findViewById(R.id.editGroupName);
        RecyclerView displayFriends = findViewById(R.id.recyclerViewFriends);

        ArrayList<String> addedFriends = new ArrayList<>();
        RadioGroup relations = findViewById(R.id.relationTypeRG);
        relations.clearCheck();
        relations.setOnCheckedChangeListener((radioGroup, checkedId) -> {

            displayFriends.setLayoutManager(new LinearLayoutManager(CreateGroup.this));
            ArrayList<String> toDisplay;

            if(checkedId == R.id.radiobuttonAll) {
                // Get the usernames of all friends
                // ArrayList<String> friendUsernames = db.selectQuery(3, logged_in, new int[]{1});
                // toDisplay = db.selectQuery(0, friendUsernames, new int[]{1});
                ArrayList<String> friendUsernames = db.selectQuery(3, logged_in, new String[]{db.getColFriend()});
                toDisplay = db.selectQuery(0, friendUsernames, new String[]{db.getColFullname()});
                for(int i = 0; i < toDisplay.size(); i++) {
                    toDisplay.set(i, toDisplay.get(i) + "\n@" + friendUsernames.get(i));
                }
                Log.d("Display All", "===== REACHED!");
            } else {
                RadioButton selected = radioGroup.findViewById(checkedId);
                String tag = selected.getText().toString();
                Log.d("Relation Tag", "=== "+tag);

                ArrayList<String> friendUsernames = db.selectQuery(3, logged_in, new int[]{1}, tag);
                toDisplay = db.selectQuery(0, friendUsernames, new int[]{1});
                for(int i = 0; i < toDisplay.size(); i++) {
                    toDisplay.set(i, toDisplay.get(i) + "\n@" + friendUsernames.get(i));
                }
            }


            Collections.sort(addedFriends);
            Collections.sort(toDisplay);
            for(int i = 0; i < addedFriends.size(); i++) {
                String f = addedFriends.get(i);
                toDisplay.remove(f);
                toDisplay.add(i, f);
            }

            // Display on RecyclerView
            displayFriends.setAdapter(new AdapterCheckbox(toDisplay, addedFriends,
                    (friendName, checked) -> {
                if(checked) {
                    if(!addedFriends.contains(friendName)) {
                        addedFriends.add(friendName);
                        Log.d("Group: Add", "===== "+addedFriends.size());
                    }
                }
                else {
                    addedFriends.remove(friendName);
                    Log.d("Group: Remove", "===== "+addedFriends.size());
                }
            }, true));
        });
        relations.check(R.id.radiobuttonAll);

        Button create = findViewById(R.id.buttonCreateGroup);
        create.setOnClickListener(v -> {
            String gn = name.getText().toString();
            if(gn.isEmpty()) {
                Toast.makeText(CreateGroup.this,
                        "Creation Denied:\nNameless groups aren't fun! :(",
                        Toast.LENGTH_SHORT).show();
            } else if (addedFriends.isEmpty()) {
                Toast.makeText(CreateGroup.this,
                        "Creation Denied:\nCan't be in a group by YOURSELF! :P",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Create sorted JSONArray holding desired group members, including current user
                Collections.sort(addedFriends);
                JSONArray members = new JSONArray();
                for(String mem : addedFriends) {
                    members.put((mem.split("\n@"))[1]);
                }
                members.put(logged_in);

                // Create unique group ID
                ArrayList<Integer> usedGroupIDs = db.getGroupIDs();
                int id = new Random().nextInt(seed + seed);
                while(usedGroupIDs.contains(id)) {
                    id = new Random().nextInt(seed + seed);
                }
                try {
                    AtomicBoolean proceed = new AtomicBoolean(true);
                    ArrayList<JSONObject> existing_groups = JSONInteract.getUserGroups(logged_in);
                    for(JSONObject g : existing_groups) {
                        if(g.getJSONArray("Members").equals(members)) {
                            AlertDialog caution = new AlertDialog.Builder(CreateGroup.this)
                                    .setTitle("Warning: Existing Group Structure")
                                    .setMessage("Group: " + g.getString("Group_Name")
                                            + " contains the same group members. Would you like to proceed?")
                                    .setCancelable(false)
                                    .setPositiveButton(android.R.string.yes,
                                            (dialogInterface, which) ->
                                                    dialogInterface.cancel())
                                    .setNegativeButton(android.R.string.no,
                                            (dialogInterface, which) -> {
                                                proceed.set(false);
                                                dialogInterface.cancel();
                                            }).create();
                            caution.show();
                            break;
                        }
                    }

                    if(proceed.get()) {
                        // JSONObject -> {"Group_ID":<int>, "Group_Name":<String>, "Members":<String[]>}
                        if(writeToGroupJSON(id, gn, members) && (editGroupsInUserJSON(id, members))) {
                            Toast.makeText(CreateGroup.this,
                                    gn + " successfully created!", Toast.LENGTH_SHORT).show();
                            Log.d("Created Group: ID", "=== "+id);
                            db.insertData(6, new String[]{String.valueOf(id), gn});

                            finish();
                            startActivity(getIntent());
                        } else {
                            Toast.makeText(CreateGroup.this,
                                    "Oops! Something went wrong...", Toast.LENGTH_SHORT).show();
                            Log.d("Created Group", "===== EMPTY!");
                        }
                    }
                } catch (IOException | JSONException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static boolean writeToGroupJSON(int groupID, String groupName, JSONArray groupMembers)
            throws IOException, JSONException {
        JSONObject groupJSON = new JSONObject();
        groupJSON.put("Group_ID", groupID);
        groupJSON.put("Group_Name", groupName);
        groupJSON.put("Members", groupMembers);
        return JSONInteract.writeToJSONFile(1, groupJSON);
    }

    private static boolean editGroupsInUserJSON(int groupID, JSONArray membersArray)
            throws IOException, JSONException {
        boolean status = true;
        for(int i = 0; i < membersArray.length(); i++) {
            String m = membersArray.getString(i);
            JSONObject u = JSONInteract.getJSONObject(0, "Username", m);
            u.getJSONArray("User_Groups").put(groupID);
            if(!JSONInteract.writeToJSONFile(0, u, m)) {
                status = false;
            }
            Log.d("User", "=== "+ u.getJSONArray("User_Groups"));
        }
        return status;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        db.close();
        Intent toGroups = new Intent(getApplicationContext(), Groups.class);
        startActivity(toGroups);
    }
}