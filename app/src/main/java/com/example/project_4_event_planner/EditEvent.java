package com.example.project_4_event_planner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditEvent extends AppCompatActivity {

    private static final String[] timeConversion = new String[]{
            "12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM", "8AM", "9AM", "10AM", "11AM",
            "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        int _id = getIntent().getIntExtra("editing_event_id", 0);

        try {
            // Need only one JSONObject to keep updating with "put"
            JSONObject jsonDetails = JSONInteract.getJSONObject(2, "Event_ID", _id);
            Database db = new Database(EditEvent.this);

            // Change NAME - eventJSON.put("Event_Name", eventName);
            Button option_name = findViewById(R.id.chooseEventName);
            option_name.setOnClickListener(view -> {
                View _edittext = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, null);
                EditText _text = _edittext.findViewById(R.id.editTextBox);
                _text.setHint(R.string.hint_change_name);
                try { _text.setText(jsonDetails.getString("Event_Name")); }
                catch (JSONException e) { throw new RuntimeException(e); }

                AlertDialog _edit_dialog = new AlertDialog.Builder(EditEvent.this)
                        .setTitle("Edit Event Name")
                        .setView(_edittext)
                        .setPositiveButton("Apply", (dialogInterface, i) -> {
                            try {
                                jsonDetails.put("Event_Name", _text.getText().toString());
                                JSONInteract.writeToJSONFile(2, jsonDetails, "");

                                ContentValues val = new ContentValues();
                                val.put(db.getColEventName(), _text.getText().toString());
                                int _mod = db.updateData(_id, val);
                                Log.d("Event Name", "=== "+_mod);
                            }
                            catch (JSONException | IOException e) { throw new RuntimeException(e); }

                            dialogInterface.dismiss();
                            Toast.makeText(EditEvent.this,
                                    "Event Name Changed!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) ->
                                dialogInterface.cancel())
                        .create();
                _edit_dialog.show();
            });


            // Change DATE - eventJSON.put("Event_Date", selectedEventDate);
            Button option_date = findViewById(R.id.chooseEventDate);
            String[] _date = jsonDetails.getString("Event_Date").split("/");
            Calendar cal_start = Calendar.getInstance();
            int _year = Integer.parseInt(_date[2]);
            int _month = Integer.parseInt(_date[0]);
            int _day = Integer.parseInt(_date[1]);
            cal_start.set(_year, _month, _day, 0, 0, 0);
            option_date.setOnClickListener(view -> new DatePickerDialog(EditEvent.this,
                    (datePicker, year, month, day) -> {
                        Calendar temp = Calendar.getInstance();
                        temp.set(year, month, day, 0, 0, 0);
                        Calendar cal = Calendar.getInstance();
                        Log.d("Check Date", "=== "+(cal.before(temp) || cal.equals(temp)));
                        if(cal.before(temp) || cal.equals(temp)) {
                            cal_start.setTime(temp.getTime());
                            String selectedEventDate = new SimpleDateFormat("MM/dd/yyyy",
                                    Locale.getDefault()).format(temp.getTime());
                            try {
                                jsonDetails.put("Event_Date", selectedEventDate);
                                JSONInteract.writeToJSONFile(2, jsonDetails, "");
                            } catch (JSONException | IOException e) { throw new RuntimeException(e); }
                            Log.d("Date Picked", "=== "+temp.getTime());
                        }
                    }, _year, _month, _day).show());


            // Change START - eventJSON.put("Event_Start", startTime);
            Button option_start = findViewById(R.id.chooseEventStart);
            // Get Start Time in format [h:mm a]----CONVERT---->[24 Hour time]
            String[] startDate = jsonDetails.getString("Event_Start").split(" - ");
            String[] startTime = startDate[2].split(":");
            int _hourOfDay_start = getHour24(startTime[0] + (startTime[1].split(" ")[1]));
            int _min_start = Integer.parseInt(startTime[1].split(" ")[0]);
            option_start.setOnClickListener(v ->
                    new TimePickerDialog(EditEvent.this,
                            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                            (timePicker, hour, minute) -> {
                                Calendar start = Calendar.getInstance();
                                start.setTime(cal_start.getTime());
                                start.set(Calendar.HOUR_OF_DAY, hour);
                                start.set(Calendar.MINUTE, minute);
                                String newStartTime = new SimpleDateFormat("MM/dd/yyyy - EEE d - h:mm a",
                                        Locale.getDefault()).format(start.getTime());
                                try {
                                    jsonDetails.put("Event_Start", newStartTime);
                                    JSONInteract.writeToJSONFile(2, jsonDetails, "");

                                    ContentValues val = new ContentValues();
                                    val.put(db.getColStart(), newStartTime);
                                    int _mod = db.updateData(_id, val);
                                    Log.d("Event Start", "=== "+_mod);
                                } catch (JSONException | IOException e) { throw new RuntimeException(e); }
                            }, _hourOfDay_start, _min_start,false).show());


            // Change END - eventJSON.put("Event_End", endTime);
            Button option_end = findViewById(R.id.chooseEventEnd);
            // Get End Time in format [h:mm a]----CONVERT---->[24 Hour time]
            String[] EndDate = jsonDetails.getString("Event_End").split(" - ");
            String[] EndTime = EndDate[2].split(":");
            int _hourOfDay_end = getHour24(EndTime[0] + (EndTime[1].split(" ")[1]));
            int _min_end = Integer.parseInt(EndTime[1].split(" ")[0]);
            option_end.setOnClickListener(v ->
                    new TimePickerDialog(EditEvent.this,
                            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                            (timePicker, hour, minute) -> {
                                Calendar end = Calendar.getInstance();
                                end.setTime(cal_start.getTime());
                                end.set(Calendar.HOUR_OF_DAY, hour);
                                end.set(Calendar.MINUTE, minute);
                                String newEndTime = new SimpleDateFormat("MM/dd/yyyy - EEE d - h:mm a",
                                        Locale.getDefault()).format(end.getTime());
                                try {
                                    jsonDetails.put("Event_End", newEndTime);
                                    JSONInteract.writeToJSONFile(2, jsonDetails, "");

                                    ContentValues val = new ContentValues();
                                    val.put(db.getColEnd(), newEndTime);
                                    int _mod = db.updateData(_id, val);
                                    Log.d("Event End", "=== "+_mod);
                                } catch (JSONException | IOException e) { throw new RuntimeException(e); }
                            }, _hourOfDay_end, _min_end,false).show());


            // Change INVITEES - eventJSON.put("Event_Invitees", eventInvitees);
            Button option_inv = findViewById(R.id.chooseEventInvitees);
            option_inv.setOnClickListener(view -> {
                Toast.makeText(EditEvent.this,
                        "Sorry! This isn't available right now!", Toast.LENGTH_SHORT).show();
            });


            // Change LOCATION - eventJSON.put("Event_Location", location);
            Button option_loc = findViewById(R.id.chooseEventLocation);
            option_loc.setOnClickListener(view -> {
                View _edittext = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, null);
                EditText _text = _edittext.findViewById(R.id.editTextBox);
                _text.setHint(R.string.hint_change_location);
                try { _text.setText(jsonDetails.getString("Event_Location")); }
                catch (JSONException e) { throw new RuntimeException(e); }

                AlertDialog _edit_dialog = new AlertDialog.Builder(EditEvent.this)
                        .setTitle("Edit Event Location")
                        .setView(_edittext)
                        .setPositiveButton("Apply", (dialogInterface, i) -> {
                            try {
                                jsonDetails.put("Event_Location", _text.getText().toString());
                                JSONInteract.writeToJSONFile(2, jsonDetails, "");
                            }
                            catch (JSONException | IOException e) { throw new RuntimeException(e); }

                            dialogInterface.dismiss();
                            Toast.makeText(EditEvent.this,
                                    "Event Location Changed!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) ->
                                dialogInterface.cancel())
                        .create();
                _edit_dialog.show();
            });


            // Change NOTES - eventJSON.put("Event_Advisory", eventAdvisory);
            Button option_note = findViewById(R.id.chooseEventNote);
            option_note.setOnClickListener(view -> {
                View _edittext = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, null);
                EditText _text = _edittext.findViewById(R.id.editTextBox);
                _text.setHint(R.string.hint_change_notes);
                try { _text.setText(jsonDetails.getString("Event_Advisory")); }
                catch (JSONException e) { throw new RuntimeException(e); }

                AlertDialog _edit_dialog = new AlertDialog.Builder(EditEvent.this)
                        .setTitle("Edit Event Advisory Notes")
                        .setView(_edittext)
                        .setPositiveButton("Apply", (dialogInterface, i) -> {
                            try {
                                jsonDetails.put("Event_Advisory", _text.getText().toString());
                                JSONInteract.writeToJSONFile(2, jsonDetails, "");
                            }
                            catch (JSONException | IOException e) { throw new RuntimeException(e); }

                            dialogInterface.dismiss();
                            Toast.makeText(EditEvent.this,
                                    "Event Notes Changed!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) ->
                                dialogInterface.cancel())
                        .create();
                _edit_dialog.show();
            });
        } catch (JSONException | IOException e) { throw new RuntimeException(e); }
    }

    private int getHour24(String _hour) {
        for (int i = 0; i < timeConversion.length; i++) {
            if(timeConversion[i].equalsIgnoreCase(_hour)) {
                return i;
            }
        }
        return 24;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent goToEvents = new Intent(getApplicationContext(), Events.class);
        startActivity(goToEvents);
    }
}