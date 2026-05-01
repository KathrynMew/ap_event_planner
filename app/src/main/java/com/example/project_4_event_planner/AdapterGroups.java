package com.example.project_4_event_planner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterGroups extends RecyclerView.Adapter<AdapterGroups.ViewHolder> {
    private static ArrayList<String> adapter_groups = new ArrayList<>();
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Button groupButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupButton = itemView.findViewById(R.id.displayGroupNameList);
            groupButton.setOnClickListener(view -> Toast.makeText(itemView.getContext(),
                    "Sorry, any further action is unavailable at this time!",
                    Toast.LENGTH_SHORT).show());
        }
    }

    public AdapterGroups(ArrayList<String> groupsToDisplay) {
        adapter_groups = groupsToDisplay;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_group_names, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.groupButton.setText(adapter_groups.get(position));
    }

    @Override
    public int getItemCount() {
        return adapter_groups.size();
    }
}
