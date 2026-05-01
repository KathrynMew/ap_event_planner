package com.example.project_4_event_planner;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.*;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CreateEvent extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener,
        RadioGroup.OnCheckedChangeListener,
        CompoundButton.OnCheckedChangeListener,
        FragmentAlarmPrompt.OnOptionPressedListener,
        FragmentAlarmPicker.OnAlarmSetListener {
    private static String logged_in;
    private static final int seed = 1000000000;
    private RadioGroup eventTypeRadioGroup;
    private CheckBox chooseGroupCheckbox;
    private static final ArrayList<String> groupNamesList = new ArrayList<>();
    private static final ArrayList<JSONArray> groupMembers = new ArrayList<>();
    private static final ArrayList<String> invitees = new ArrayList<>();
    private static final ArrayList<String> toDisplay = new ArrayList<>();
    private static String selectedEventDate;
    private final Database db = new Database(CreateEvent.this);

    /**
     * @see #writeToEventJSON(int, String, String, String, String, String, JSONArray, String) for edit "planner_events"
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        SharedPreferences user_sp = getSharedPreferences("current_user", MODE_PRIVATE);
        SharedPreferences events_sp = getSharedPreferences("event_times", MODE_PRIVATE);
        logged_in = user_sp.getString("username", null);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        int currYear = cal.get(Calendar.YEAR);
        int currMonth = cal.get(Calendar.MONTH);
        int currDay = cal.get(Calendar.DAY_OF_MONTH);

        EditText input_event_name = findViewById(R.id.editEventName);
        TextView event_date_selector = findViewById(R.id.eventDateDialog);
        TextView stp = findViewById(R.id.eventStartDialog);
        TextView etp = findViewById(R.id.eventEndDialog);
        TextView displayStart = findViewById(R.id.showStartTime);
        TextView displayEnd = findViewById(R.id.showEndTime);

        event_date_selector.setOnClickListener(view -> new DatePickerDialog(CreateEvent.this,
                    (datePicker, year, month, day) -> {
                Calendar temp = Calendar.getInstance();
                temp.set(year, month, day, 0, 0, 0);
                Log.d("Check Date", "=== "+(cal.before(temp) || cal.equals(temp)));
                if(cal.before(temp) || cal.equals(temp)) {
                    cal.setTime(temp.getTime());
                    selectedEventDate = new SimpleDateFormat("MM/dd/yyyy",
                            Locale.getDefault()).format(temp.getTime());
                    events_sp.edit().putString("eventDate", selectedEventDate).apply();
                    event_date_selector.setText(selectedEventDate);
                    Log.d("Date Picked", "=== "+temp.getTime());

                    // Allow for user to access other fields
                    if(!stp.isEnabled()) {
                        stp.setEnabled(true);
                        for (int i = 0; i < eventTypeRadioGroup.getChildCount(); i++) {
                            eventTypeRadioGroup.getChildAt(i).setEnabled(true);
                        }
                        eventTypeRadioGroup.setOnCheckedChangeListener(this);
                    }
                }
            }, currYear, currMonth, currDay).show());

        groupNamesList.add("Choose a group!");
        try {
            ArrayList<JSONObject> groupObjs = JSONInteract.getUserGroups(logged_in);
            for(JSONObject g : groupObjs) {
                groupNamesList.add(g.getString("Group_Name"));
                groupMembers.add(g.getJSONArray("Members"));
            }
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
        Spinner groupsSpinner = findViewById(R.id.displayGroupsSpinner);

        // RadioGroup & RadioButton
        eventTypeRadioGroup = findViewById(R.id.relationTypeRadioGroup);
        chooseGroupCheckbox = findViewById(R.id.checkboxOther);
        eventTypeRadioGroup.clearCheck();
        chooseGroupCheckbox.setChecked(false);

        // Disable all views until a date is chosen
        groupsSpinner.setEnabled(false);
        for (int i = 0; i < eventTypeRadioGroup.getChildCount(); i++) {
            eventTypeRadioGroup.getChildAt(i).setEnabled(false);
        }
        chooseGroupCheckbox.setEnabled(false);
        stp.setEnabled(false);
        etp.setEnabled(false);

        // START TIME PICKER
        stp.setOnClickListener(view -> {
            TimePickerDialog std = new TimePickerDialog(CreateEvent.this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    (timePicker, hour, minute) -> {
                        Calendar temp = Calendar.getInstance();
                        temp.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH), hour, minute);
                        events_sp.edit().putString("eventStartTime",
                                new SimpleDateFormat("MM/dd/yyyy - EEE d - h:mm a",
                                        Locale.getDefault()).format(temp.getTime())).apply();

                        String[] output = (Objects.requireNonNull(events_sp.getString(
                                "eventStartTime", null))).split(" - ");
                        String forDisplayStart = output[0] + " - " + output[1];
                        displayStart.setText(forDisplayStart);
                        stp.setText(output[2]);
                        Log.d("Start Time", "=== "
                                + new SimpleDateFormat("MM/dd/yyyy - EEE d - h:mm a",
                                Locale.getDefault()).format(temp.getTime()));
                        etp.setEnabled(true);
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false);
            std.create();
            std.show();
        });

        etp.setOnClickListener(view -> {
            TimePickerDialog etd = new TimePickerDialog(CreateEvent.this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    (timePicker, hour, minute) -> {
                        try {
                            // Create temporary Calendar instance to compare times
                            Calendar temp = Calendar.getInstance();
                            temp.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH), hour, minute);

                            // Convert time format "h:mm aa" to Date objects for comparison
                            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a",
                                    Locale.getDefault());
                            Date start = sdf.parse((events_sp.getString("eventStartTime",
                                    "0:00 NA")).split(" - ")[2]);
                            Date end = sdf.parse(sdf.format(temp.getTime()));

                            // If end time is before start time, set end date to next day
                            assert end != null;
                            if (end.before(start) || end.equals(start)) {
                                temp.roll(Calendar.DAY_OF_MONTH, true);
                                Log.d("End Time: temp", "===== Increased Day of Month");
                            } else {
                                temp.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
                                Log.d("End Time: temp", "===== Reverted Day of Month");
                            }

                            events_sp.edit().putString("eventEndTime",
                                    new SimpleDateFormat("MM/dd/yyyy - EEE d - h:mm a",
                                            Locale.getDefault()).format(temp.getTime())).apply();
                            String[] output = (Objects.requireNonNull(events_sp.getString(
                                    "eventEndTime", null))).split(" - ");
                            String forDisplayEnd = output[0] + " - " + output[1];
                            displayEnd.setText(forDisplayEnd);
                            etp.setText(output[2]);

                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false);
            etd.create();
            etd.show();
        });

        TextView _no_results = findViewById(R.id.noResultsCreateEvents);
        _no_results.setVisibility(View.VISIBLE);

        EditText adv = findViewById(R.id.editAdvisoryNotes);
        adv.setMovementMethod(new ScrollingMovementMethod());

        // Alarm Setting Prompt
        loadFragment(new FragmentAlarmPrompt());

        // Create Event Button Handling
        Button create = findViewById(R.id.buttonCreateEvent);
        create.setOnClickListener(v -> {
            String event_name = input_event_name.getText().toString();
            String start_date = events_sp.getString("eventStartTime", null);
            String end_date = events_sp.getString("eventEndTime", null);

            EditText loc = findViewById(R.id.editLocation);
            String input_location = loc.getText().toString();
            if(input_location.isEmpty()) {
                input_location = "Not Provided";
            }

            String input_adv = adv.getText().toString();

            if(event_name.isEmpty() || start_date == null || end_date == null ||
                    (eventTypeRadioGroup.getCheckedRadioButtonId() == -1)) {
                Toast.makeText(CreateEvent.this,
                        "Required Fields: Event Name, Type, Start and End Times!",
                        Toast.LENGTH_LONG).show();
                Log.d("Create Button", "===== REQUIRED FIELDS EMPTY");
            } else {
                ArrayList<Integer> usedEventIDs = db.getEventIDs();
                int event_id = new Random().nextInt(seed + seed);
                while(usedEventIDs.contains(event_id)) {
                    event_id = new Random().nextInt(seed + seed);
                }

                RadioButton selectedType = eventTypeRadioGroup.findViewById(eventTypeRadioGroup.getCheckedRadioButtonId());
                String event_type = selectedType.getText().toString();

                String[] eventEntry = new String[]{logged_in,
                        String.valueOf(event_id), event_name, start_date, end_date, event_type};

                try {
                    JSONArray event_invitees = new JSONArray();
                    for(String friend : invitees) {
                        event_invitees.put((friend.split("\n@"))[1]);
                    }

                    if(db.insertData(1, eventEntry) != -1 && writeToEventJSON(event_id,
                            event_name, start_date, end_date, input_location, event_type,
                            event_invitees, input_adv)) {

                        // Populate EVENTS table with mappings to each invitee
                        for(String inv : invitees) {
                            eventEntry = new String[]{inv.split("\n@")[1],
                                    String.valueOf(event_id), event_name,
                                    start_date, end_date, event_type};
                            db.insertData(1, eventEntry);
                        }
                        // Add alarm to JSONObject for user
                        if (events_sp.getBoolean("Alarm Set", false)) {
                            writeToAlarmJSON(event_id);
                        }
                        Toast.makeText(CreateEvent.this,
                                event_name + " successfully created!", Toast.LENGTH_SHORT).show();
                        Log.d("Created Event: ID", "=== "+event_id);

                        // Reset activity
                        finish();
                        startActivity(getIntent());
                    }

                } catch (NoSuchAlgorithmException | JSONException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> user_groups, View view, int pos, long row) {
        if(pos > 0) {
            int membersIndex = pos - 1;
            JSONArray membersJSONArray = groupMembers.get(membersIndex);
            for(int i = 0; i < membersJSONArray.length() - 1; i++){
                try {
                    String m = membersJSONArray.getString(i);
                    String fullname = db.selectQuery(0, m, new int[]{1}).get(0);
                    String disp = (fullname + "\n@" + m);
                    toDisplay.remove(disp);
                    toDisplay.add(i, disp);
                    if(!invitees.contains(disp)) {
                        invitees.add(disp);
                        Toast.makeText(CreateEvent.this,
                                "Delivering invitation to "
                                        + (disp.split("\n@")[0]),
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            RecyclerView rvInvitees = findViewById(R.id.recyclerViewInvitees);
            rvInvitees.setLayoutManager(new LinearLayoutManager(CreateEvent.this));
            rvInvitees.setAdapter(new AdapterCheckbox(toDisplay, invitees,
                    (friendName, checked) -> {
                        if(checked) {
                            if(!invitees.contains(friendName)) {
                                invitees.add(friendName);
                                Toast.makeText(CreateEvent.this,
                                        "Delivering invitation to "
                                                + (friendName.split("\n@")[0]),
                                        Toast.LENGTH_SHORT).show();
                                Log.d("Invitees: Add", "=== "+invitees.size());
                            }
                        }
                        else {
                            Toast.makeText(CreateEvent.this,
                                    "Are you sure about this? Well, okay...",
                                    Toast.LENGTH_SHORT).show();
                            invitees.remove(friendName);
                            setAvailabilityRecycler();
                            Log.d("Invitees: Remove", "=== "+invitees.size());
                        }
                    }, true));

            setAvailabilityRecycler();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> user_groups) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        TextView header = findViewById(R.id.inviteesHeader);
        RecyclerView rvInvitees = findViewById(R.id.recyclerViewInvitees);

        ArrayList<String> toDisplay;
        if(checkedId == R.id.radiobuttonPersonal) {
            invitees.clear();
            chooseGroupCheckbox.setChecked(false);
            chooseGroupCheckbox.setEnabled(false);
            header.setVisibility(View.GONE);
            rvInvitees.setVisibility(View.GONE);
            setAvailabilityRecycler();
            Log.d("Invitees", "=== "+invitees.size());
        } else {
            chooseGroupCheckbox.setEnabled(true);
            chooseGroupCheckbox.setOnCheckedChangeListener(this);
            header.setVisibility(View.VISIBLE);
            rvInvitees.setVisibility(View.VISIBLE);

            rvInvitees.setLayoutManager(new LinearLayoutManager(CreateEvent.this));

            RadioButton selected = radioGroup.findViewById(checkedId);
            String tag = selected.getText().toString();
            Log.d("Relation Tag", "=== " + tag);

            ArrayList<String> friendUsernames = db.selectQuery(3, logged_in, new int[]{1}, tag);
            // Get the full name for each username in `friendUsernames`
            toDisplay = db.selectQuery(0, friendUsernames, new int[]{1});
            for (int i = 0; i < toDisplay.size(); i++) {
                toDisplay.set(i, toDisplay.get(i) + "\n@" + friendUsernames.get(i));
            }

            Collections.sort(toDisplay);
            // Display on RecyclerView
            rvInvitees.setAdapter(new AdapterCheckbox(toDisplay, invitees,
                    (friendName, checked) -> {
                if (checked) {
                    // Only update `invitees` if not already added
                    if(!invitees.contains(friendName)) {
                        invitees.add(friendName);
                        Toast.makeText(CreateEvent.this,
                                "Delivering invitation to "
                                        + (friendName.split("\n@")[0]),
                                Toast.LENGTH_SHORT).show();
                        Log.d("Invitees: Add", "=== " + invitees.size());
                    }
                } else {
                    invitees.remove(friendName);
                    Log.d("Invitees: Remove", "=== " + invitees.size());
                }

                setAvailabilityRecycler();
            }, true));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Spinner groupsSpinner = findViewById(R.id.displayGroupsSpinner);
        // Checkbox must be checked in order to use spinner
        if(isChecked) {
            if(eventTypeRadioGroup.getCheckedRadioButtonId() == R.id.radiobuttonPersonal) {
                Toast.makeText(CreateEvent.this,
                        "Sorry, cannot add friends to personal events!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String[] displayGroups = groupNamesList.toArray(new String[0]);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    displayGroups);
            groupsSpinner.setEnabled(true);
            groupsSpinner.setAdapter(adapter);
            groupsSpinner.setOnItemSelectedListener(this);
            Toast.makeText(CreateEvent.this,
                    "Nice! Use the dropdown to add a group of friends!",
                    Toast.LENGTH_SHORT).show();
        } else {
            groupsSpinner.setEnabled(false);
        }
    }

    private void setAvailabilityRecycler() {
        TextView _no_results = findViewById(R.id.noResultsCreateEvents);
        _no_results.setVisibility(View.VISIBLE);

        ArrayList<String> usersEvents = db.selectEventsDuring(logged_in, selectedEventDate);
        Log.d("User Events Size", "=== "+usersEvents.size());

        if(!invitees.isEmpty()) {
            for(String i : invitees) {
                String invUsername = i.split("\n@")[1];
                usersEvents.addAll(db.selectEventsDuring(invUsername, selectedEventDate));
                Log.d("User Events Size", "=== "+usersEvents.size());
            }
        }

        if(usersEvents.size() > 0) {
            _no_results.setVisibility(View.GONE);
        }

        RecyclerView rvAvailable = findViewById(R.id.recyclerViewAvailable);
        rvAvailable.setLayoutManager(new LinearLayoutManager(CreateEvent.this));
        rvAvailable.setAdapter(new AdapterAvailability(usersEvents, logged_in));
    }

    /**
     * Puts parameters and global variables necessary into a JSONObject to be written to JSON File
     * "planner_events"
     * @param eventID corresponds to "Event_ID"
     * @param eventName corresponds to "Event_Name"
     * @param startTime corresponds to "Event_Start". Formatted as: "MM/dd/yyyy - EEE d - h:mm a
     * @param endTime corresponds to "Event_End". Formatted as: "MM/dd/yyyy - EEE d - h:mm a
     * @param location corresponds to "Event_Location". Optional string. May be used to store links or images?
     * @param eventType corresponds to "Event_Type". May be changed, but current types are the same as the relation types
     * @param eventInvitees corresponds to "Event_Invitees". JSONArray containing the friends added by user
     * @param eventAdvisory corresponds to "Event_Advisory". May be empty
     * @return
     * @throws JSONException
     * @throws IOException
     */
    private static boolean writeToEventJSON(int eventID, String eventName, String startTime,
                                            String endTime, String location, String eventType,
                                            JSONArray eventInvitees, String eventAdvisory)
            throws JSONException, IOException {
        JSONObject eventJSON = new JSONObject();
        eventJSON.put("Event_ID", eventID);
        eventJSON.put("Event_Name", eventName);
        eventJSON.put("Event_Date", selectedEventDate);
        eventJSON.put("Event_Start", startTime);
        eventJSON.put("Event_End", endTime);
        eventJSON.put("Event_Type", eventType);
        eventJSON.put("Event_Creator", logged_in);
        eventJSON.put("Event_Invitees", eventInvitees);
        eventJSON.put("Event_Location", location);
        eventJSON.put("Event_Advisory", eventAdvisory);

        return JSONInteract.writeToJSONFile(2, eventJSON);
    }

    private void writeToAlarmJSON(int eventID)
            throws JSONException, IOException {
        SharedPreferences events_sp = getSharedPreferences("event_times", MODE_PRIVATE);
        /* {"planner_alarms":
              [
                {
                "Username":<String>,
                "User_Alarms":[{"Event_ID":<int>, "Time_in_Milliseconds":<long>, "Frequency":<int>, "Message":<String>}]
                }
              ]
           }
        */
        // JSONObject to be put inside User_Alarms JSONArray
        JSONObject alarmJSON = new JSONObject()
                .put("Event_ID", eventID)
                .put("Time_in_Milliseconds", events_sp.getLong("eventAlarmTimeInMS", 0))
                .put("Frequency", events_sp.getInt("eventAlarmFrequency", 0))
                .put("Message", events_sp.getString("eventAlarmNotifMessage", ""));

        JSONObject userAlarmJSON = new JSONObject()
                .put("Username", logged_in)
                .put("User_Alarms", new JSONArray().put(alarmJSON));
        JSONInteract.writeToJSONFile(3, userAlarmJSON);
    }

    /***
     * Replaces FrameLayout with Fragment
     * @param fragment Fragment corresponding fragment class.
     */
    private void loadFragment(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.fragmentContainerViewAlarm, fragment).commit();
    }

    @Override
    public void onButtonPressed() {
        if (selectedEventDate == null ||
                getSharedPreferences("event_times", MODE_PRIVATE)
                .getString("eventStartTime", null) == null ||
                getSharedPreferences("event_times", MODE_PRIVATE)
                .getString("eventStartTime", null) == null) {
            AlertDialog cannotProceed = new AlertDialog.Builder(CreateEvent.this)
                    .setTitle("Cannot Set Alarm Yet")
                    .setMessage("Please ensure that an event date, start time, and end time have been chosen before you set an alarm!")
                    .setIcon(R.drawable.sharp_sentiment_neutral_24)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) ->
                            dialogInterface.cancel()).create();
            cannotProceed.show();
        } else {
            loadFragment(new FragmentAlarmPicker());
        }
    }

    @Override
    public void onAlarmSetButton(boolean alarmSet) {
        if (alarmSet) {
            getSharedPreferences("event_times", MODE_PRIVATE).edit().putBoolean("Alarm Set", true).apply();
            Toast.makeText(CreateEvent.this,
                    "Your alarm has been created for this event!", Toast.LENGTH_SHORT).show();
        } else {
            loadFragment(new FragmentAlarmPrompt());
            getSharedPreferences("event_times", MODE_PRIVATE).edit().putBoolean("Alarm Set", false).apply();
        }
    }

    public void finish() {
        selectedEventDate = null;
        getSharedPreferences("event_times", MODE_PRIVATE).edit().clear().apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent goToEvents = new Intent(getApplicationContext(), Events.class);
        startActivity(goToEvents);
    }
}