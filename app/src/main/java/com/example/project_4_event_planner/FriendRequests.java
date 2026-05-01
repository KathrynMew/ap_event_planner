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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class FriendRequests extends AppCompatActivity {

    private static final JSONObject in = new JSONObject();
    private static final ArrayList<String> in_keys = new ArrayList<>();
    private static final JSONObject out = new JSONObject();
    private static final ArrayList<String> out_keys = new ArrayList<>();
    private static String logged_in;
    private final Database db = new Database(FriendRequests.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        SharedPreferences user_sp = getSharedPreferences("current_user", MODE_PRIVATE);
        logged_in = user_sp.getString("username", null);

        Log.d("Out Keys", "=== "+out_keys.size());
        Log.d("In Keys", "=== "+in_keys.size());

        // NOTE: Maps all friend tags to user friends such that
        // {"full name\n@username":["casual", "school", "work", "online"]}
        ArrayList<String> temp = db.selectWithTags(logged_in, false);
        // t = full name+"\n@"+username+':'+relation_type;
        for(String t : temp) {
            String[] t_arr = t.split(":");
            // t_arr = {full name+"\n@"+username, relation_type}
            try {
                if(in.has(t_arr[0])) {
                    in.getJSONArray(t_arr[0]).put(t_arr[1]);
                } else {
                    in_keys.add(t_arr[0]);
                    in.put(t_arr[0], new JSONArray().put(t_arr[1]));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        temp = db.selectWithTags(logged_in, true);
        for(String t : temp) {
            String[] t_arr = t.split(":");
            // t_arr = {full name+"\n@"+username, relation_type}
            try {
                if(out.has(t_arr[0])) {
                    out.getJSONArray(t_arr[0]).put(t_arr[1]);
                } else {
                    out_keys.add(t_arr[0]);
                    out.put(t_arr[0], new JSONArray().put(t_arr[1]));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        setIncoming();
        setOutgoing();
    }

    private void setIncoming() {
        TextView _no_results = findViewById(R.id.noResultsIncoming);
        if (in_keys.isEmpty()) {
            _no_results.setVisibility(View.VISIBLE);
            return;
        }
        _no_results.setVisibility(View.GONE);

        RecyclerView incoming = findViewById(R.id.recyclerviewIncomingRequests);
        incoming.setLayoutManager(new LinearLayoutManager(FriendRequests.this));
        incoming.setAdapter(new AdapterIncomingFriendRequests(in, in_keys, (requester, id) -> {
            String[] req_arr = requester.split("\n@");
            int modded = db.deleteData(4, req_arr[1], logged_in,
                    null, null);
            if((id == R.id.acceptButton) && (modded > 0)) {
                JSONArray relation_types = in.getJSONArray(requester);
                for(int i = 0; i < relation_types.length(); i++) {
                    long id1 = db.insertData(3, new String[]{logged_in, req_arr[1],
                            relation_types.getString(i)});
                    long id2 = db.insertData(3, new String[]{req_arr[1], logged_in,
                            relation_types.getString(i)});
                    Log.d("Database FRIENDS: User", "=== "+id1);
                    Log.d("Database FRIENDS: Requester", "=== "+id2);
                }

                if(editFriendsInUserJSON(requester)) {
                    Toast.makeText(FriendRequests.this,
                            req_arr[0] + " has been added to friend list!",
                            Toast.LENGTH_SHORT).show();
                }
            } // If R.id.denyButton, do nothing special

            in.remove(requester);
            in_keys.remove(requester);
            Log.d("Incoming: Size", "=== "+in_keys.size());

            // Reset Recycler
            setIncoming();
        }));
    }

    private void setOutgoing() {
        TextView _no_results = findViewById(R.id.noResultsOutgoing);
        if (out_keys.isEmpty()) {
            _no_results.setVisibility(View.VISIBLE);
            return;
        }
        _no_results.setVisibility(View.GONE);

        RecyclerView outgoing = findViewById(R.id.recyclerviewOutgoingRequests);
        outgoing.setLayoutManager(new LinearLayoutManager(FriendRequests.this));
        outgoing.setAdapter(new AdapterOutgoingFriendRequests(out, out_keys, (request) -> {
            String[] req_arr = request.split("\n@");
            AlertDialog confirmRemove = new AlertDialog.Builder(FriendRequests.this)
                    .setTitle("Cancel Friend Request")
                    .setMessage("Are you sure you'd like to cancel friend request to "+req_arr[0]+"?")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> {
                        int modded = db.deleteData(4, logged_in,
                                req_arr[1], null, null);
                        Log.d("Requests: Remove entry", "=== "+modded);
                        out.remove(request);
                        out_keys.remove(request);
                        Log.d("Outgoing: Size", "=== "+out_keys.size());

                        // Reset Recycler
                        setOutgoing();
                    })
                    .setNegativeButton("Cancel", (dialogInterface, which) ->
                            dialogInterface.cancel()).create();
            confirmRemove.show();
        }));
    }

    private static boolean editFriendsInUserJSON(String some_user)
            throws IOException, JSONException {
        String requester;
        if(some_user.contains("\n@")) {
            requester = some_user.split("\n@")[1];
        } else {
            requester = some_user;
        }

        // Edit `Friends` for Requester
        JSONObject r = JSONInteract.getJSONObject(0, "Username", requester);
        r.getJSONArray("Friends").put(logged_in);
        if(JSONInteract.writeToJSONFile(0, r, requester)) {
            Log.d("JSON Users: Requester", "=== "+ r.getJSONArray("Friends"));

            // Edit `Friends` for Logged_in user
            JSONObject u = JSONInteract.getJSONObject(0, "Username", logged_in);
            u.getJSONArray("Friends").put(requester);
            Log.d("JSON Users: User", "=== "+ u.getJSONArray("Friends"));
            return JSONInteract.writeToJSONFile(0, u, logged_in);
        }
        return false;
    }

    public void finish() {
        // Empty all containers
        in_keys.clear();
        out_keys.clear();

        try {
            if(in.length() > 0) {
                JSONArray _keys = in.names();
                for (int i = 0; i < Objects.requireNonNull(_keys).length(); i++) {
                    in.remove(_keys.getString(i));
                }
            }

            if(out.length() > 0) {
                JSONArray _keys = out.names();
                for (int i = 0; i < Objects.requireNonNull(_keys).length(); i++) {
                    out.remove(_keys.getString(i));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        TextView _no_incoming = findViewById(R.id.noResultsIncoming);
        TextView _no_outgoing = findViewById(R.id.noResultsOutgoing);
        _no_incoming.setVisibility(View.VISIBLE);
        _no_outgoing.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent goToFriends = new Intent(getApplicationContext(), Friends.class);
        startActivity(goToFriends);
    }
}