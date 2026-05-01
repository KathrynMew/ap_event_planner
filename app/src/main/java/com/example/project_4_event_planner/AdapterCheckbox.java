package com.example.project_4_event_planner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterCheckbox extends RecyclerView.Adapter<AdapterCheckbox.ViewHolder> {
    private static ArrayList<String> choices;
    private static ArrayList<String> selected;
    private static OnCheckedChangedListener cbListener;
    private static boolean enableCheckBox = true;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox box;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            box = itemView.findViewById(R.id.friendUserCB);
            if(enableCheckBox) {
                box.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                    cbListener.isCheckedText(box.getText().toString(), isChecked);
                });
            } else {
                box.setEnabled(false);
            }

        }
    }

    public AdapterCheckbox(ArrayList<String> display_list,
                           ArrayList<String> added_list,
                           OnCheckedChangedListener listener,
                           boolean enable) {
        choices = display_list;
        selected = added_list;
        cbListener = listener;
        enableCheckBox = enable;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_checkbox_friends, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.box.setText(choices.get(position));
        if(selected.contains(choices.get(position))) {
            holder.box.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    public interface OnCheckedChangedListener {
        void isCheckedText(String friendName, boolean checked);
    }
}
