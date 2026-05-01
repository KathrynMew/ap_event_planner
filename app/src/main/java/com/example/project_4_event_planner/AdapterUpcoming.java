package com.example.project_4_event_planner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterUpcoming extends RecyclerView.Adapter<AdapterUpcoming.ViewHolder> {
    private static ArrayList<String> displayUpcoming = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView monthDayView;
        private final TextView eventName;
        private final TextView displayStartEndTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            monthDayView = itemView.findViewById(R.id.displayMonthDay);
            eventName = itemView.findViewById(R.id.displayEventNameCard);
            displayStartEndTime = itemView.findViewById(R.id.displayStartEndCard);
        }
    }

    public AdapterUpcoming(ArrayList<String> upcoming) {
        displayUpcoming = upcoming;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_profile_display_events, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] _up = displayUpcoming.get(position).split(", ");
        String _month_day = _up[0] + "\n" + _up[1];
        String _start_end = _up[2] + " - " + _up[3];
        holder.monthDayView.setText(_month_day);
        holder.eventName.setText(_up[4]);
        holder.displayStartEndTime.setText(_start_end);
    }

    @Override
    public int getItemCount() {
        return displayUpcoming.size();
    }
}
