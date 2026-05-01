package com.example.project_4_event_planner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User can view their events as well as their friends' events.
 * @note Was originally going to incorporate AlarmService but requires more research into multiple alarms
 */
public class Events extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener {
    private static final ArrayList<String> selectedFriends = new ArrayList<>();
    private static final ArrayList<String> toDisplay = new ArrayList<>();
    private static final ArrayList<String> groupNamesList = new ArrayList<>();
    private static final ArrayList<JSONArray> groupMembers = new ArrayList<>();
    private static final JSONObject friends = new JSONObject();
    private static String logged_in;
    private static String selectedDate;
    private static boolean applyFilter;
    private CheckBox cbNone;
    private CheckBox cbCasual;
    private CheckBox cbWork;
    private CheckBox cbSchool;
    private CheckBox cbOnline;
    private CheckBox cbGroup;
    private final Database db = new Database(Events.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        SharedPreferences user_sp = getSharedPreferences("current_user", MODE_PRIVATE);
        logged_in = user_sp.getString("username", null);

        // NOTE: Maps all friend tags to user friends such that
        // {"full name\n@username":["casual", "school", "work", "online"]}
        ArrayList<String> temp = db.selectWithTags(logged_in);
        // t = full name+"\n@"+username+':'+relation_type;
        for(String t : temp) {
            String[] t_arr = t.split(":");
            // t_arr = {full name+"\n@"+username, relation_type}
            try {
                if(friends.has(t_arr[0])) {
                    friends.getJSONArray(t_arr[0]).put(t_arr[1]);
                } else {
                    friends.put(t_arr[0], new JSONArray().put(t_arr[1]));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        cbNone = findViewById(R.id.checkboxNone);
        cbCasual = findViewById(R.id.checkboxCasual);
        cbWork = findViewById(R.id.checkboxWork);
        cbSchool = findViewById(R.id.checkboxSchool);
        cbOnline = findViewById(R.id.checkboxOnline);
        cbGroup = findViewById(R.id.checkboxOther);

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

        resetChecked();
        setEnabled(false);

        TextView _no_results = findViewById(R.id.noResultsEvents);
        _no_results.setVisibility(View.VISIBLE);

        CalendarView cv = findViewById(R.id.filterCalendarView);
        TextView textviewFilterTime = findViewById(R.id.dialogEventsTimeFilter);
        Button setTimeFilter = findViewById(R.id.buttonSetFilter);
        textviewFilterTime.setText("");
        textviewFilterTime.setEnabled(false);
        setTimeFilter.setEnabled(false);

        Calendar cal = Calendar.getInstance();
        cv.setOnDateChangeListener((calendarView, year, month, day) -> {
            cal.set(year, month, day, 0, 0, 0);
            selectedDate = new SimpleDateFormat("MM/dd/yyyy",
                    Locale.getDefault()).format(cal.getTime());
            Log.d("Selected Date", "=== "+selectedDate);
            setAvailabilityRecycler();

            // When date is selected, enables all other fields
            setEnabled(true);
            textviewFilterTime.setEnabled(true);
            setTimeFilter.setEnabled(true);
        });
        cv.setDate(cal.getTimeInMillis());

        Button navigateToCreate = findViewById(R.id.buttonCreateEvent);
        navigateToCreate.setOnClickListener(v -> {
            finish();
            Intent goToCreate = new Intent(getApplicationContext(), CreateEvent.class);
            startActivity(goToCreate);
        });
    }

    /**
     * Configures Event viewer recycler.
     * @see Events#onClickSelectTimeFilter(View)
     */
    private void setAvailabilityRecycler() {
        Log.d("Availability Recycler", "===== REACHED");
        ArrayList<String> usersEvents;
        TextView textviewFilterTime = findViewById(R.id.dialogEventsTimeFilter);
        if(textviewFilterTime.isEnabled() && (!textviewFilterTime.getText().toString().isEmpty())
                && applyFilter) {
            String filterTime = textviewFilterTime.getText().toString();
            Log.d("Enabled", "=== "+textviewFilterTime.isEnabled());
            Log.d("isNotEmpty", "=== "+(!textviewFilterTime.getText().toString().isEmpty()));
            Log.d("applyFilter", "=== "+applyFilter);
            usersEvents = db.selectEventsDuring(logged_in, selectedDate, filterTime);
            Log.d("User Events Size", "=== "+usersEvents.size());
            if(!selectedFriends.isEmpty()) {
                for(String sel : selectedFriends) {
                    usersEvents.addAll(db.selectEventsDuring(sel, selectedDate, filterTime));
                    Log.d("User Events Size", "=== "+usersEvents.size());
                }
            }
        } else {
            usersEvents = db.selectEventsDuring(logged_in, selectedDate);
            Log.d("User Events Size", "=== "+usersEvents.size());
            if(!selectedFriends.isEmpty()) {
                for(String sel : selectedFriends) {
                    usersEvents.addAll(db.selectEventsDuring(sel, selectedDate));
                    Log.d("User Events Size", "=== "+usersEvents.size());
                }
            }
        }

        TextView _no_results = findViewById(R.id.noResultsEvents);
        if (usersEvents.isEmpty()) {
            _no_results.setVisibility(View.VISIBLE);
        } else {
            _no_results.setVisibility(View.GONE);
        }

        RecyclerView rvEvents = findViewById(R.id.recyclerViewEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(Events.this));
        rvEvents.setAdapter(new AdapterAvailability(usersEvents, logged_in, EventID -> {
            AlertDialog.Builder vedb = new AlertDialog.Builder(Events.this);
            View event_details_layout = getLayoutInflater()
                    .inflate(R.layout.dialog_event_details, null);
            TextView displayEventName = event_details_layout.findViewById(R.id.editEventNameDisplay);
            TextView displayEventType = event_details_layout.findViewById(R.id.editEventTypeDisplay);
            TextView displayEventDate = event_details_layout.findViewById(R.id.editEventDateDisplay);
            TextView displayEventStart = event_details_layout.findViewById(R.id.editStartTimeDisplay);
            TextView displayEventEnd = event_details_layout.findViewById(R.id.editEndTimeDisplay);
            TextView displayEventOwner = event_details_layout.findViewById(R.id.editEventCreatorDisplay);
            TextView displayEventGuest = event_details_layout.findViewById(R.id.editEventInvitedDisplay);
            TextView displayEventLoc = event_details_layout.findViewById(R.id.editEventLocationDisplay);
            TextView displayEventNote = event_details_layout.findViewById(R.id.editEventNoteDisplay);
            Button returnToList = event_details_layout.findViewById(R.id.buttonBackToEvents);
            Button editEvent = event_details_layout.findViewById(R.id.buttonEditEvent);
            Button deleteEvent = event_details_layout.findViewById(R.id.buttonEventDelete);

            JSONObject jsonDetails = JSONInteract.getJSONObject(2, "Event_ID", EventID);
            String organizer = jsonDetails.getString("Event_Creator");
            String _name = db.selectQuery(0, organizer, new int[]{1}).get(0);
            displayEventName.setText(jsonDetails.getString("Event_Name"));
            displayEventType.setText(jsonDetails.getString("Event_Type"));
            displayEventDate.setText(conversion(jsonDetails.getString("Event_Date"), 0));
            displayEventStart.setText(conversion(jsonDetails.getString("Event_Start"), 1));
            displayEventEnd.setText(conversion(jsonDetails.getString("Event_End"), 1));
            displayEventOwner.setText(_name);
            JSONArray eventInvitees = jsonDetails.getJSONArray("Event_Invitees");
            switch (eventInvitees.length()) {
                case 0:
                    displayEventGuest.setText(R.string.none_choice);
                    break;
                case 1:
                    displayEventGuest.setText(eventInvitees.getString(0));
                    break;
                default:
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < eventInvitees.length(); i++) {
                        String fullname = db.selectQuery(0, eventInvitees.getString(i), new int[]{1}).get(0);
                        sb.append(fullname).append("\n");
                    }
                    displayEventGuest.setText(sb.toString().trim());
                    break;
            }
            displayEventLoc.setText(jsonDetails.getString("Event_Location"));
            displayEventNote.setText(jsonDetails.getString("Event_Advisory"));

            AlertDialog viewEventDialog = vedb.setView(event_details_layout).create();
            viewEventDialog.show();

            returnToList.setOnClickListener(view -> viewEventDialog.dismiss());

            editEvent.setOnClickListener(view -> {
                if(logged_in.equals(organizer)) {
                    Intent goToEdit = new Intent(getApplicationContext(), EditEvent.class);
                    goToEdit.putExtra("editing_event_id", EventID);
                    startActivity(goToEdit);
                } else {
                    Toast.makeText(Events.this,
                            "Only event organizer / creator can make changes to this event!",
                            Toast.LENGTH_SHORT).show();
                }
            });

            deleteEvent.setOnClickListener(view -> {
                if(logged_in.equals(organizer)) {
                    AlertDialog confirmDelete = new AlertDialog.Builder(Events.this)
                            .setTitle("Event Deletion Confirmation")
                            .setMessage("Are you sure you'd like to delete this event? This action cannot be undone.")
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> {
                                int modded = db.deleteData(EventID, null);
                                Log.d("Events: Remove entry", "=== "+modded);
                                try {
                                    if(modded > 0) {
                                        if(JSONInteract.deleteFromJSONFile(2, EventID)) {
                                            Toast.makeText(Events.this,
                                                    displayEventName.getText().toString()
                                                            + " deleted from your calendar!",
                                                    Toast.LENGTH_SHORT).show();
                                            setAvailabilityRecycler();
                                            dialogInterface.dismiss();
                                            viewEventDialog.dismiss();
                                        }
                                    }
                                } catch (IOException | JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .setNegativeButton(android.R.string.no, (dialogInterface, which) ->
                                    dialogInterface.cancel()).create();
                    confirmDelete.show();
                } else {
                    AlertDialog confirmDelete = new AlertDialog.Builder(Events.this)
                            .setTitle("Event Deletion Confirmation")
                            .setMessage("Are you sure you'd like to delete this event? This action cannot be undone.")
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> {
                                int modded = db.deleteData(EventID, new String[]{logged_in});
                                Log.d("Events: Remove entry", "=== "+modded);

                                if(modded > 0) {
                                    Toast.makeText(Events.this,
                                            displayEventName.getText().toString()
                                                    + " deleted from your calendar!",
                                            Toast.LENGTH_SHORT).show();
                                    setAvailabilityRecycler();
                                    dialogInterface.dismiss();
                                    viewEventDialog.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.no, (dialogInterface, which) ->
                                    dialogInterface.cancel()).create();
                    confirmDelete.show();
                }
            });
        }));
    }

    /**
     * Configures friends display recyclerView
     */
    private void setSelectedFriends() {
        RecyclerView rvFriends = findViewById(R.id.recyclerViewFriends);
        rvFriends.setVisibility(View.VISIBLE);
        rvFriends.setLayoutManager(new LinearLayoutManager(Events.this));
        rvFriends.setAdapter(new AdapterCheckbox(toDisplay, selectedFriends,
                (friendName, checked) -> {
                    if(checked) {
                        if(!selectedFriends.contains(friendName)) {
                            selectedFriends.add(friendName);
                            Toast.makeText(Events.this,
                                    "Showing availability of "
                                            + (friendName.split("\n@")[0]),
                                    Toast.LENGTH_SHORT).show();
                            Log.d("Selected Friends: Add", "=== "+selectedFriends.size());
                        }
                    }
                    else {
                        selectedFriends.remove(friendName);
                        Log.d("Selected Friends: Remove", "=== "+selectedFriends.size());
                    }
                    setAvailabilityRecycler();
                }, true));
    }

    /**
     * Set the accessibility of checkboxes. User cannot select any until they have chosen a date on the calendarView
     * @param enable if true, all checkboxes are clickable, meaning actions will affect the results displayed
     */
    private void setEnabled(boolean enable) {
        cbNone.setEnabled(enable);
        cbCasual.setEnabled(enable);
        cbWork.setEnabled(enable);
        cbSchool.setEnabled(enable);
        cbOnline.setEnabled(enable);
        cbGroup.setEnabled(enable);

        cbNone.setOnCheckedChangeListener(this);
        cbCasual.setOnCheckedChangeListener(this);
        cbWork.setOnCheckedChangeListener(this);
        cbSchool.setOnCheckedChangeListener(this);
        cbOnline.setOnCheckedChangeListener(this);
        cbGroup.setOnCheckedChangeListener(this);
    }

    private void resetChecked() {
        cbNone.setChecked(true);
        cbCasual.setChecked(false);
        cbWork.setChecked(false);
        cbSchool.setChecked(false);
        cbOnline.setChecked(false);
        cbGroup.setChecked(false);

        RecyclerView rvFriends = findViewById(R.id.recyclerViewFriends);
        rvFriends.setVisibility(View.GONE);

        Spinner groupSpinner = findViewById(R.id.displayGroupsSpinner);
        groupSpinner.setEnabled(false);
    }

    /**
     *
     * @param checkedBox
     * @param isChecked
     * @note For relation by tag
     */
    @Override
    public void onCheckedChanged(CompoundButton checkedBox, boolean isChecked) {
        try {
            JSONArray ObjKeys = friends.names();
            if(isChecked) {
                if (checkedBox.getId() == R.id.checkboxNone) {
                    // NOTE: if `None` is checked, no other checkbox should be
                    // Displays personal events
                    resetChecked();
                    selectedFriends.clear();
                    toDisplay.clear();
                    setAvailabilityRecycler();
                } else {
                    cbNone.setChecked(false);

                    if (checkedBox.getId() == R.id.checkboxCasual ||
                            checkedBox.getId() == R.id.checkboxWork ||
                            checkedBox.getId() == R.id.checkboxSchool ||
                            checkedBox.getId() == R.id.checkboxOnline) {
                        cbGroup.setChecked(false);

                        String tag = checkedBox.getText().toString();
                        for (int i = 0; i < (ObjKeys != null ? ObjKeys.length() : 0); i++) {
                            String key = ObjKeys.getString(i);
                            if(!toDisplay.contains(key)) {
                                JSONArray i_arr = friends.getJSONArray(key);
                                if(JSONInteract.contains(i_arr, tag)) {
                                    toDisplay.add(key);
                                }
                            }
                        }
                        // Display results in recyclerView
                        setSelectedFriends();
                    } else if (checkedBox.getId() == R.id.checkboxOther) {
                        cbCasual.setChecked(false);
                        cbWork.setChecked(false);
                        cbSchool.setChecked(false);
                        cbOnline.setChecked(false);

                        Spinner groupsSpinner = findViewById(R.id.displayGroupsSpinner);
                        String[] displayGroups = groupNamesList.toArray(new String[0]);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_dropdown_item,
                                displayGroups);
                        groupsSpinner.setEnabled(true);
                        groupsSpinner.setAdapter(adapter);
                        groupsSpinner.setOnItemSelectedListener(this);
                        Toast.makeText(Events.this,
                                "Nice! Use the dropdown to view a group of your friends!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                if (checkedBox.getId() == R.id.checkboxNone) {
                    if (getBoxesChecked(cbNone).length == 0 && (!cbGroup.isChecked()))  {
                        cbNone.setChecked(true);
                        Log.d("BoxesChecked: Size", "=== "+getBoxesChecked(cbNone).length);
                        Toast.makeText(Events.this,
                                "Select a tag or group instead!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (checkedBox.getId() == R.id.checkboxCasual ||
                        checkedBox.getId() == R.id.checkboxWork ||
                        checkedBox.getId() == R.id.checkboxSchool ||
                        checkedBox.getId() == R.id.checkboxOnline) {

                    if (!(cbCasual.isChecked() || cbSchool.isChecked() || cbWork.isChecked() ||
                            cbOnline.isChecked())) {
                        if (!cbGroup.isChecked()) {
                            cbNone.setChecked(true);
                        } else {
                            toDisplay.clear();
                            selectedFriends.clear();
                            return;
                        }
                    }

                    String[] typesChecked = getBoxesChecked(checkedBox.findViewById(checkedBox.getId()));
                    String tag = checkedBox.getText().toString();
                    // For each `friend` in friends, check if they the corresponding tag and remove
                    // them only if they do not have another tag that is selected to view
                    for (int i = 0; i < (ObjKeys != null ? ObjKeys.length() : 0); i++) {
                        String key = ObjKeys.getString(i);
                        JSONArray i_arr = friends.getJSONArray(key);
                        if(JSONInteract.contains(i_arr, tag)) {
                            if(i_arr.length() == 1 || (i_arr.length() > 1 &&
                                    (!JSONInteract.contains(i_arr, typesChecked)))) {
                                toDisplay.remove(key);
                            }
                        }
                    }
                } else if (checkedBox.getId() == R.id.checkboxOther) {
                    Spinner groupsSpinner = findViewById(R.id.displayGroupsSpinner);
                    groupsSpinner.setOnItemSelectedListener(null);
                    groupsSpinner.setSelection(0);
                    groupsSpinner.setEnabled(false);

                    if (!(cbCasual.isChecked() || cbSchool.isChecked() || cbWork.isChecked() ||
                            cbOnline.isChecked())) {
                        cbNone.setChecked(true);
                    }
                }

                // Display results in recyclerView
                setSelectedFriends();
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getBoxesChecked(CheckBox ignoreCheck) {
        ArrayList<String> checked = new ArrayList<>();
        if((!cbCasual.equals(ignoreCheck)) && cbCasual.isChecked()) { checked.add("Casual"); }
        if((!cbWork.equals(ignoreCheck))   && cbWork.isChecked())   { checked.add("Work"); }
        if((!cbSchool.equals(ignoreCheck)) && cbSchool.isChecked()) { checked.add("School"); }
        if((!cbOnline.equals(ignoreCheck)) && cbOnline.isChecked()) { checked.add("Online"); }
        return checked.toArray(new String[0]);
    }

    @Override
    public void onItemSelected(AdapterView<?> groupsSpinnerView, View view, int pos, long row) {
        if(pos > 0) {
            toDisplay.clear();
            selectedFriends.clear();

            int membersIndex = pos - 1;
            JSONArray membersJSONArray = groupMembers.get(membersIndex);

            for(int i = 0; i < membersJSONArray.length(); i++){
                try {
                    String m = membersJSONArray.getString(i);
                    if(m.equals(logged_in)) {
                        continue;
                    }
                    String fullname = db.selectQuery(0, m, new int[]{1}).get(0);
                    String disp = (fullname + "\n@" + m);
                    toDisplay.add(disp);
                    selectedFriends.add(disp);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            RecyclerView rvFriends = findViewById(R.id.recyclerViewFriends);
            rvFriends.setVisibility(View.VISIBLE);
            rvFriends.setLayoutManager(new LinearLayoutManager(Events.this));
            rvFriends.setAdapter(new AdapterCheckbox(toDisplay, selectedFriends,
                    (friendName, checked) -> {}, false));
            setAvailabilityRecycler();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> groupsSpinnerView) {
        groupsSpinnerView.setSelection(0);
    }

    /**
     *
     * @param view
     */
    public void onClickFilter(View view) {
        Button setTimeFilter = findViewById(R.id.buttonSetFilter);
        if(setTimeFilter.getText().toString().equals("Filter")) {
            setTimeFilter.setText(R.string.unfilter_button);
            applyFilter = true;
        } else {
            setTimeFilter.setText(R.string.filter_button);
            applyFilter = false;
            TextView setTimeText = findViewById(R.id.dialogEventsTimeFilter);
            setTimeText.setText(null);
        }

        setAvailabilityRecycler();
    }

    public void onClickSelectTimeFilter(View view) {
        TextView textviewFilterTime = findViewById(R.id.dialogEventsTimeFilter);
        textviewFilterTime.setOnClickListener(v ->
                new TimePickerDialog(Events.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        (timePicker, hour, minute) -> {
                            Calendar start = Calendar.getInstance();
                            start.set(Calendar.HOUR_OF_DAY, hour);
                            start.set(Calendar.MINUTE, minute);
                            String selTime = new SimpleDateFormat("h:mm a",
                                    Locale.getDefault()).format(start.getTime());
                            textviewFilterTime.setText(selTime);

                            Toast.makeText(Events.this,
                                    "Press `Filter` button to enable time filter!",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("Filter Time", "=== "+selTime);
                        },
                        0,
                        0,
                        false).show());
    }

    private String conversion(String date, int mode) throws ParseException {
        switch (mode) {
            case 0:
                DateFormat to   = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()); // wanted format
                DateFormat from = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()); // current format
                return (to.format(from.parse(date)));
            case 1:
                String[] time = date.split(" - ");
                return time[2];
        }
        return "";
    }

    public void finish() {
        // Reset all global variable containers
        selectedFriends.clear();
        toDisplay.clear();
        groupNamesList.clear();
        groupMembers.clear();

        try {
            if(friends.length() > 0) {
                JSONArray _keys = friends.names();
                for (int i = 0; i < Objects.requireNonNull(_keys).length(); i++) {
                    friends.remove(_keys.getString(i));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        db.close();
        Intent goToProfile = new Intent(getApplicationContext(), Profiles.class);
        startActivity(goToProfile);
    }

}