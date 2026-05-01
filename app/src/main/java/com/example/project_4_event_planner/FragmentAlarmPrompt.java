package com.example.project_4_event_planner;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAlarmPrompt extends Fragment {

    private OnOptionPressedListener optionListener;

    public FragmentAlarmPrompt() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_set_alarm_prompt, container, false);

        Button yes_choice = rootView.findViewById(R.id.option_yes);
        yes_choice.setOnClickListener(view -> optionListener.onButtonPressed());

        Button no_choice = rootView.findViewById(R.id.option_no);
        no_choice.setOnClickListener(view -> Toast.makeText(container.getContext(),
                "No alarm will be added!", Toast.LENGTH_SHORT).show());

        return rootView;
    }

    public interface OnOptionPressedListener {
        void onButtonPressed();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnOptionPressedListener) {
            optionListener = (OnOptionPressedListener) context;
        } else {
            throw new ClassCastException(context
                    + " must implement FragmentAlarmPrompt.OnOptionPressedListener");
        }
    }
}