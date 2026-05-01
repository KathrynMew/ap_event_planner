package com.example.project_4_event_planner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Groups extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_groups);

        SharedPreferences user_sp = getSharedPreferences("current_user", MODE_PRIVATE);
        String logged_in = user_sp.getString("username", null);

        TextView _no_results = findViewById(R.id.noResultsGroups);
        _no_results.setVisibility(View.VISIBLE);
        try {
            ArrayList<JSONObject> jsonGroups = JSONInteract.getUserGroups(logged_in);
            if (jsonGroups.size() > 0) {
                _no_results.setVisibility(View.GONE);
            }

            ArrayList<String> groupNames = new ArrayList<>();
            for(JSONObject g : jsonGroups) {
                groupNames.add(g.getString("Group_Name"));
            }

            RecyclerView rv = findViewById(R.id.displayGroupsRecycler);
            rv.setLayoutManager(new LinearLayoutManager(Groups.this));
            rv.setAdapter(new AdapterGroups(groupNames));
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }

        Button create = findViewById(R.id.buttonCreateGroup);
        create.setOnClickListener(view -> {
            Intent goToCreateGroups = new Intent(getApplicationContext(), CreateGroup.class);
            startActivity(goToCreateGroups);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent goToProfile = new Intent(getApplicationContext(), Profiles.class);
        startActivity(goToProfile);
    }
}