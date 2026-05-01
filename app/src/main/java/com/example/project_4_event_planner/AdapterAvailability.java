package com.example.project_4_event_planner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class AdapterAvailability extends RecyclerView.Adapter<AdapterAvailability.ViewHolder> {
    private static ArrayList<String> users;
    private static String logged_user;
    private static boolean viewEventEnabled = false;
    private static ViewEventOnClickListener viewEventListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeDisplay;
        private final TextView userDisplay;
        private final ImageButton viewDetails;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeDisplay = itemView.findViewById(R.id.textEventDuration);
            userDisplay = itemView.findViewById(R.id.displayEvent);
            viewDetails = itemView.findViewById(R.id.viewEventDetailsButton);
            if(viewEventEnabled) {
                viewDetails.setOnClickListener(view -> {
                    if(viewDetails.getContentDescription().equals("available")) {
                        String[] details = users.get(getAdapterPosition()).split(" : ");
                        try {
                            viewEventListener.onEventPositionClick(Integer.parseInt(details[3]));
                        } catch (JSONException | IOException | ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
    }

    public AdapterAvailability(ArrayList<String> display_list,
                               String currUser) {
        users = display_list;
        logged_user = currUser;
    }

    public AdapterAvailability(ArrayList<String> display_list,
                               String currUser,
                               ViewEventOnClickListener listener) {
        users = display_list;
        logged_user = currUser;
        viewEventListener = listener;
        viewEventEnabled = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_availability, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] details = users.get(position).split(" : ");
        holder.timeDisplay.setText(details[0]);
        holder.userDisplay.setText(details[1]);

        if(viewEventEnabled) {
            holder.viewDetails.setVisibility(View.VISIBLE);
            if(details[1].contains(logged_user)) {
                holder.viewDetails.setImageResource(R.drawable.sharp_event_note_24);
                holder.viewDetails.setContentDescription("available");
            }
        } else {
            holder.viewDetails.setVisibility(View.GONE);
        }

        if(details[2].equals("Personal")) {
            holder.timeDisplay.setBackgroundResource(R.color.personal_event_background);
            holder.timeDisplay.setTextColor(ContextCompat.getColor(holder.timeDisplay.getContext(),
                    R.color.personal_event_text));
        } else if(details[2].equals("Casual")) {
            holder.timeDisplay.setBackgroundResource(R.color.casual_background);
            holder.timeDisplay.setTextColor(ContextCompat.getColor(holder.timeDisplay.getContext(),
                    R.color.casual_text));
        } else if(details[2].equals("Work")) {
            holder.timeDisplay.setBackgroundResource(R.color.work_background);
            holder.timeDisplay.setTextColor(ContextCompat.getColor(holder.timeDisplay.getContext(),
                    R.color.work_text));
        } else if(details[2].equals("School")) {
            holder.timeDisplay.setBackgroundResource(R.color.school_background);
            holder.timeDisplay.setTextColor(ContextCompat.getColor(holder.timeDisplay.getContext(),
                    R.color.school_text));
        } else if(details[2].equals("Online")) {
            holder.timeDisplay.setBackgroundResource(R.color.online_background);
            holder.timeDisplay.setTextColor(ContextCompat.getColor(holder.timeDisplay.getContext(),
                    R.color.online_text));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface ViewEventOnClickListener {
        void onEventPositionClick(int EventID) throws JSONException, IOException, ParseException;
    }
}
