package com.example.project_4_event_planner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdapterOutgoingFriendRequests extends RecyclerView.Adapter<AdapterOutgoingFriendRequests.ViewHolder> {
    private static ArrayList<String> out_requests = new ArrayList<>();
    private static JSONObject mapped_tags = new JSONObject();
    private static OnClickListener clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView userDisplay;
        private final View tag_casual;
        private final View tag_school;
        private final View tag_work;
        private final View tag_online;
        private final Button removeFR;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tag_casual = itemView.findViewById(R.id.CasualTag);
            tag_school = itemView.findViewById(R.id.SchoolTag);
            tag_work = itemView.findViewById(R.id.WorkTag);
            tag_online = itemView.findViewById(R.id.OnlineTag);
            userDisplay = itemView.findViewById(R.id.friendTextView);
            removeFR = itemView.findViewById(R.id.buttonCancel);
        }
    }

    public AdapterOutgoingFriendRequests(JSONObject jsonObject,
                                         ArrayList<String> display_list,
                                         OnClickListener onClickListener) {
        out_requests = display_list;
        mapped_tags = jsonObject;
        clickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_outgoing_friend_requests_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String friend_key = out_requests.get(position);
        holder.userDisplay.setText(friend_key);
        holder.removeFR.setOnClickListener(view ->
                clickListener.onPositionClick(friend_key));
        try {
            JSONArray tag_array = mapped_tags.getJSONArray(friend_key);
            if(JSONInteract.contains(tag_array, "Casual")) {
                holder.tag_casual.setVisibility(View.VISIBLE);
            } else {
                holder.tag_casual.setVisibility(View.GONE);
            }

            if (JSONInteract.contains(tag_array, "School")) {
                holder.tag_school.setVisibility(View.VISIBLE);
            } else {
                holder.tag_school.setVisibility(View.GONE);
            }

            if(JSONInteract.contains(tag_array, "Work")) {
                holder.tag_work.setVisibility(View.VISIBLE);
            } else {
                holder.tag_work.setVisibility(View.GONE);
            }

            if(JSONInteract.contains(tag_array, "Online")) {
                holder.tag_online.setVisibility(View.VISIBLE);
            } else {
                holder.tag_online.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        return out_requests.size();
    }

    public interface OnClickListener {
        void onPositionClick(String request);
    }
}
