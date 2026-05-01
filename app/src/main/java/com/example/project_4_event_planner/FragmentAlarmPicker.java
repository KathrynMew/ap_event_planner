package com.example.project_4_event_planner;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAlarmPicker extends Fragment {
    private static final String[] timeConversion = new String[]{
            "12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM", "8AM", "9AM", "10AM", "11AM",
            "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM"};
    private OnAlarmSetListener btnSetListener;

    public FragmentAlarmPicker() {
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
        SharedPreferences events_sp = getContext().getSharedPreferences("event_times", MODE_PRIVATE);
        View rootView = inflater.inflate(R.layout.fragment_set_alarm_picker, container, false);

        // Temporarily Storing Values Until User presses setAlarmButton
        SharedPreferences temp_sp = getContext().getSharedPreferences("temporary_store", MODE_PRIVATE);

        // Display user selections for date, time, and frequency
        TextView displayCurrentSelect = rootView.findViewById(R.id.setAlarmTitle);
        TextView displayTimeSelect = rootView.findViewById(R.id.displayAlarmTime);
        TextView displayFreqSelect = rootView.findViewById(R.id.displayAlarmFreq);

        // Store pre-edited text: "Set Alarm Date:\t"
        String _disp = displayCurrentSelect.getText().toString();
        // Display currently selected event day
        displayCurrentSelect.setText(_disp.concat(events_sp.getString("eventDate", "")));

        // Set up Calendar date instance; ALARM DATE should be BEFORE the event start date
        String[] _date = events_sp.getString("eventDate", "").split("/");
        Calendar cal_start = Calendar.getInstance();
        int _year = Integer.parseInt(_date[2]);
        int _month = Integer.parseInt(_date[0]);
        int _day = Integer.parseInt(_date[1]);
        cal_start.set(_year, _month, _day, 0, 0, 0);

        // Calendar instance to hold user configurations of alarm
        Calendar _alarm = Calendar.getInstance();
        _alarm.setTime(cal_start.getTime());

        Button alarm_date_button = rootView.findViewById(R.id.setAlarmDate);
        alarm_date_button.setOnClickListener(view -> {
            // Setup current date; ALARM DATE should be AFTER this date
            Calendar temp_curr = Calendar.getInstance();
            int currYear = temp_curr.get(Calendar.YEAR);
            int currMonth = temp_curr.get(Calendar.MONTH);
            int currDay = temp_curr.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(getContext(),
                    (datePicker, year, month, day) -> {
                        Calendar _temp = Calendar.getInstance();
                        _temp.set(year, month, day, 0, 0, 0);
                        Log.d("Check Date", "=== " + (temp_curr.before(_alarm) && _alarm.before(cal_start)));
                        if (temp_curr.before(_temp) && _temp.before(cal_start)) {
                            _alarm.set(year, month, day, 0, 0, 0);
                            String selectedAlarmDate = new SimpleDateFormat("MM/dd/yyyy",
                                    Locale.getDefault()).format(_alarm.getTime());
                            displayCurrentSelect.setText(_disp.concat(selectedAlarmDate));
                            Log.d("Date Picked", "=== " + _alarm.getTime());
                        } else {
                            AlertDialog invalidDate = new AlertDialog.Builder(getContext())
                                    .setTitle("Invalid Date")
                                    .setMessage("Alarm Date must be within the time between today's date and the event start date!")
                                    .setCancelable(true)
                                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                                        dialogInterface.dismiss();
                                    }).create();
                            invalidDate.show();
                        }
                    }, currYear, currMonth, currDay).show();
        });

        String[] startDate = (events_sp.getString("eventStartTime", "")).split(" - ");
        String[] startTime = startDate[2].split(":");
        int _hourOfDay = getHour24(startTime[0] + (startTime[1].split(" ")[1]));
        int _min = Integer.parseInt(startTime[1].split(" ")[0]);
        cal_start.set(Calendar.HOUR_OF_DAY, _hourOfDay);
        cal_start.set(Calendar.MINUTE, _min);

        Button alarm_time_button = rootView.findViewById(R.id.setAlarmTime);
        alarm_time_button.setOnClickListener(view -> {
            TimePickerDialog std = new TimePickerDialog(getContext(),
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    (timePicker, hour, minute) -> {
                        Calendar _temp = Calendar.getInstance();
                        _temp.setTime(_alarm.getTime());
                        _temp.set(Calendar.HOUR_OF_DAY, hour);
                        _temp.set(Calendar.MINUTE, minute);

                        Calendar temp_curr = Calendar.getInstance();
                        if (temp_curr.before(_temp) && _temp.before(cal_start)) {
                            // Set hour and minute of alarm set
                            _alarm.set(Calendar.HOUR_OF_DAY, hour);
                            _alarm.set(Calendar.MINUTE, minute);

                            String selectedAlarmTime = new SimpleDateFormat("h:mm a",
                                    Locale.getDefault()).format(_alarm.getTime());
                            displayTimeSelect.setText(selectedAlarmTime);
                            Log.d("Date Picked", "=== " + _alarm.getTime());
                        } else {
                            AlertDialog invalidDate = new AlertDialog.Builder(getContext())
                                    .setTitle("Invalid Time")
                                    .setMessage("Alarm Time must be within the time between today's date and the event start date!")
                                    .setCancelable(true)
                                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                                        dialogInterface.dismiss();
                                    }).create();
                            invalidDate.show();
                        }
                    }, _hourOfDay, _min,false);
            std.create();
            std.show();
        });

        Button alarm_freq_button = rootView.findViewById(R.id.setAlarmDuration);
        alarm_freq_button.setOnClickListener(view -> {
            // Configure number picker for dialog
            View numberDialog = getLayoutInflater().inflate(R.layout.numberpicker_alarm_frequency, container, false);
            NumberPicker np = numberDialog.findViewById(R.id.dialog_number_picker);
            np.setMaxValue(60);
            np.setMinValue(0);
            np.setWrapSelectorWheel(false);
            np.setOnValueChangedListener((numberPicker, _old, _new) ->
                    Log.d("Alarm: Number Picker", "=== " + _old + "->" + _new));

            // Create Alert Dialog for user to use NumberPicker
            AlertDialog alarmDurationDialog = new AlertDialog.Builder(container.getContext())
                    .setTitle("Set Event Alarm Repeat Frequency")
                    .setMessage("Note: Frequency unit time is minutes!")
                    .setView(numberDialog)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        displayFreqSelect.setText(String.valueOf(np.getValue()));
                        temp_sp.edit().putInt("frequency", np.getValue()).apply();
                        Log.d("NumberPicker", "=== " + temp_sp.getInt("frequency", 0));
                    }).create();
            alarmDurationDialog.show();
        });

        EditText alarm_notif_message = rootView.findViewById(R.id.setAlarmMessage);

        FloatingActionButton cancel_set_alarm = rootView.findViewById(R.id.cancelAlarmButton);
        cancel_set_alarm.setOnClickListener(view -> {
            AlertDialog confirmCancelSetting = new AlertDialog.Builder(getContext())
                    .setTitle("Cancel Setting Alarm")
                    .setMessage("Are you sure you want to cancel setting an alarm?")
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        dialogInterface.cancel();
                        Log.d("After Cancel", "===== REACHED");
                        btnSetListener.onAlarmSetButton(false);
                    })
                    .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel())
                    .create();
            confirmCancelSetting.show();
        });

        FloatingActionButton create_new_alarm = rootView.findViewById(R.id.setAlarmButton);
        create_new_alarm.setOnClickListener(view -> {
            events_sp.edit().putLong("eventAlarmTimeInMS", _alarm.getTimeInMillis()).apply();
            events_sp.edit().putInt("eventAlarmFrequency", temp_sp.getInt("frequency", 0)).apply();
            events_sp.edit().putString("eventAlarmNotifMessage", alarm_notif_message.getText().toString()).apply();
            btnSetListener.onAlarmSetButton(true);
            Log.d("Alarm Set", "===== Sent!");
        });

        return rootView;
    }

    private int getHour24(String _hour) {
        for (int i = 0; i < timeConversion.length; i++) {
            if(timeConversion[i].equalsIgnoreCase(_hour)) {
                return i;
            }
        }
        return 24;
    }

    public interface OnAlarmSetListener {
        void onAlarmSetButton(boolean alarmSet);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentAlarmPicker.OnAlarmSetListener) {
            btnSetListener = (FragmentAlarmPicker.OnAlarmSetListener) context;
        } else {
            throw new ClassCastException(context
                    + " must implement FragmentAlarmPicker.OnAlarmSetListener");
        }
    }
}