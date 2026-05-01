package com.example.project_4_event_planner;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterAddFriends extends RecyclerView.Adapter<AdapterAddFriends.ViewHolder> {
    private static ArrayList<String> displaySearchResults = new ArrayList<>();
    private static OnClickListener personClickListener;
    private static String addedPerson;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView displayPerson;
        private final Button addPerson;
        private final Button cancelPerson;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayPerson = itemView.findViewById(R.id.searchResultText);
            addPerson = itemView.findViewById(R.id.buttonAddPerson);
            cancelPerson = itemView.findViewById(R.id.buttonCancelRequest);

        }
    }

    public AdapterAddFriends(ArrayList<String> to_display_list,
                             OnClickListener listener,
                             @Nullable String added) {
        displaySearchResults = to_display_list;
        personClickListener = listener;
        addedPerson = added;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.textview_search_result_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String p = displaySearchResults.get(position);
        Log.d("Holder: displayPerson", "=== "+p);
        holder.displayPerson.setText(p);

        if(addedPerson == null) {
            Log.d("Holder: Null", "=== "+null);
            holder.addPerson.setVisibility(View.VISIBLE);
            holder.addPerson.setOnClickListener(view ->
                    personClickListener.onPositionClick(p, holder.addPerson.getId()));
            holder.cancelPerson.setVisibility(View.GONE);
        } else {
            if (p.contains(addedPerson)) {
                Log.d("Holder: Cancel", "=== "+addedPerson);
                holder.addPerson.setVisibility(View.GONE);
                holder.cancelPerson.setVisibility(View.VISIBLE);
                holder.cancelPerson.setOnClickListener(view ->
                        personClickListener.onPositionClick(p, holder.cancelPerson.getId()));
            } else {
                Log.d("Holder: Add", "=== "+addedPerson);
                holder.addPerson.setVisibility(View.VISIBLE);
                holder.addPerson.setOnClickListener(view ->
                        personClickListener.onPositionClick(p, holder.addPerson.getId()));
                holder.cancelPerson.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return displaySearchResults.size();
    }

    public interface OnClickListener {
        void onPositionClick(String person, int button_id);
    }

}
