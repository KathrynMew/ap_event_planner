package com.example.project_4_event_planner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Planner";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME_USERS = "Users";
    private static final String TABLE_NAME_FRIENDS = "Friends";
    private static final String TABLE_NAME_FRIEND_REQUESTS = "FriendRequests";
    private static final String TABLE_NAME_EVENTS = "Events";
    private static final String TABLE_NAME_EVENT_REQUESTS = "EventRequests";
    private static final String TABLE_NAME_PROFILES = "Profiles";
    private static final String TABLE_NAME_GROUPS = "Groups";
    private static final String COL_USERNAME = "Username";
    private static final String COL_FULLNAME = "FullName";
    private static final String COL_BIRTHDATE = "Birthday";
    private static final String COL_PASSWORD = "Password";
    private static final String COL_FRIEND = "FriendUsername";
    private static final String COL_RECEIVER = "RequestReceiver";
    private static final String COL_RELATION_TYPE = "RelationType";
    private static final String COL_FR_TYPE = "RequestType";
    private static final String COL_EVENT_INVITE = "EventInvite";
    private static final String COL_EVENT_NAME = "EventTitle";
    private static final String COL_EVENT_ID = "EventID";
    private static final String COL_DATE = "EventDate";
    private static final String COL_START = "EventStartTime";
    private static final String COL_END = "EventEndTime";
    private static final String COL_EVENT_TYPE = "EventType";
    private static final String COL_INVITEES = "EventInvitees";
    private static final String COL_LOCATION = "EventLocation";
    private static final String COL_DISPLAY_NAME = "DisplayName";
    private static final String COL_PROFILE_PIC = "ProfilePicture";
    private static final String COL_ABOUT_ME = "AboutMe";
    private static final String COL_GROUP_ID = "GroupID";
    private static final String COL_GROUP_NAME = "GroupName";

    /** Stores all table names
     * Users        : 0
     * Events       : 1
     * Event Reqs   : 2
     * Friends      : 3
     * Friend Reqs  : 4
     * Profiles     : 5
     */
    private static final String[] TABLE_NAMES = {
            TABLE_NAME_USERS,
            TABLE_NAME_EVENTS,
            TABLE_NAME_EVENT_REQUESTS,
            TABLE_NAME_FRIENDS,
            TABLE_NAME_FRIEND_REQUESTS,
            TABLE_NAME_PROFILES,
            TABLE_NAME_GROUPS
    };

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_NAME_USERS + " ("
                + COL_USERNAME + " TEXT, "
                + COL_FULLNAME + " TEXT, "
                + COL_BIRTHDATE + " TEXT, "
                + COL_PASSWORD + " BLOB)";

        String createEvents = "CREATE TABLE " + TABLE_NAME_EVENTS + " ("
                + COL_USERNAME + " TEXT, "
                + COL_EVENT_ID + " INTEGER, "
                + COL_EVENT_NAME + " TEXT, "
                + COL_START + " TEXT, "
                + COL_END + " TEXT, "
                + COL_EVENT_TYPE + " TEXT)";

        String createEventRequests = "CREATE TABLE " + TABLE_NAME_EVENT_REQUESTS + " ("
                + COL_USERNAME + " TEXT, "
                + COL_FRIEND + " TEXT, "
                + COL_EVENT_INVITE + " TEXT)";

        String createFriends = "CREATE TABLE " + TABLE_NAME_FRIENDS + " ("
                + COL_USERNAME + " TEXT, "
                + COL_FRIEND + " TEXT, "
                + COL_RELATION_TYPE + " TEXT)";

        String createFriendRequests = "CREATE TABLE " + TABLE_NAME_FRIEND_REQUESTS + " ("
                + COL_USERNAME + " TEXT, "
                + COL_RECEIVER + " TEXT,"
                + COL_RELATION_TYPE + " TEXT)";

        String createProfiles = "CREATE TABLE " + TABLE_NAME_PROFILES + " ("
                + COL_USERNAME + " TEXT, "
                + COL_DISPLAY_NAME + " TEXT, "
                + COL_ABOUT_ME + " TEXT, "
                + COL_PROFILE_PIC + " TEXT)";

        String createGroups = "CREATE TABLE " + TABLE_NAME_GROUPS + " ("
                + COL_GROUP_ID + " INTEGER, "
                + COL_GROUP_NAME + " TEXT)";

        db.execSQL(createUsers);
        db.execSQL(createEvents);
        db.execSQL(createEventRequests);
        db.execSQL(createFriends);
        db.execSQL(createFriendRequests);
        db.execSQL(createProfiles);
        db.execSQL(createGroups);
    }

    /**
     *
     * @param table
     * @param data
     * @note - USERS        {Username, Full Name, Birthday, Password}
     * @note - EVENTS       {Username, ID, Name, Date, Start, End, Location, Type, Invitees, Medical}
     * @note - EVENT REQS   {Username, Friend (Event Owner), Invite Details}
     * @note - FRIENDS      {Username, Friend, Relation Type}
     * @note - FRIEND REQS  {Username, Friend, Relation Type, Friend Request Type}
     * @note - PROFILES     {Username, About Me, Interests, Medical Advisory}
     */
    public long insertData(int table, String[] data)
            throws NoSuchAlgorithmException {
        ContentValues values = new ContentValues();
        switch(table) {
            case 0: // USERS
                values.put(COL_USERNAME, data[0]);
                values.put(COL_FULLNAME, data[1]);
                values.put(COL_BIRTHDATE, data[2]);
                values.put(COL_PASSWORD, messageDigest(data[3]));
                break;
            case 1: // EVENTS
                values.put(COL_USERNAME, data[0]);
                values.put(COL_EVENT_ID, Integer.parseInt(data[1]));
                values.put(COL_EVENT_NAME, data[2]);
                values.put(COL_START, data[3]);
                values.put(COL_END, data[4]);
                values.put(COL_EVENT_TYPE, data[5]);
                break;
            case 2: // EVENT REQUESTS
                values.put(COL_USERNAME, data[0]);
                values.put(COL_FRIEND, data[1]);
                values.put(COL_EVENT_INVITE, data[2]);
                break;
            case 3: // FRIENDS
                values.put(COL_USERNAME, data[0]);
                values.put(COL_FRIEND, data[1]);
                values.put(COL_RELATION_TYPE, data[2]);
                break;
            case 4: // FRIEND REQUESTS
                values.put(COL_USERNAME, data[0]);
                values.put(COL_RECEIVER, data[1]);
                values.put(COL_RELATION_TYPE, data[2]);
                break;
            case 5: // PROFILES
                values.put(COL_USERNAME, data[0]);
                values.put(COL_DISPLAY_NAME, data[1]);
                values.put(COL_ABOUT_ME, data[2]);
                values.put(COL_PROFILE_PIC, data[3]);
                break;
            case 6: // GROUPS
                values.put(COL_GROUP_ID, Integer.parseInt(data[0]));
                values.put(COL_GROUP_NAME, data[1]);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(TABLE_NAMES[table], null, values);
        db.close();
        return id;
    }

    /**
     * Encodes Password for security
     * @param s password to be encoded
     * @return byte array to be added into `Users` table
     * @throws NoSuchAlgorithmException
     */
    public static byte[] messageDigest(String s) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called to calculate message digest of an input
        // and return array of byte
        return md.digest(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * View data in database from a specific table for a user
     * @param table : TABLE_NAMES
     *              Users        : 0
     *              Events       : 1
     *              Event Reqs   : 2
     *              Friends      : 3
     *              Friend Reqs  : 4
     *              Profiles     : 5
     * @param user  : Filters and returns query results by user
     * @return ArrayList<String> contains query results
     */
    public ArrayList<String> selectQuery(int table, String user, int[] col_ids) {
        Log.d("selectQuery: user", "=== "+user);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_NAMES[table], null,
                COL_USERNAME + "=?", new String[]{user},
                null, null, null, null);

        ArrayList<String> data = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                for(int index : col_ids) {
                    data.add(cursor.getString(index));
                }
            }
        }
        cursor.close();
        return data;
    }

    public ArrayList<String> selectQuery(int table, String user, String[] distinct) {
        Log.d("selectQuery: user", "=== "+user);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_NAMES[table], distinct,
                COL_USERNAME + "=?", new String[]{user},
                null, null, null, null);

        ArrayList<String> data = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                for(int i = 0; i < distinct.length; i++) {
                    data.add(cursor.getString(i));
                }
            }
        }
        cursor.close();
        return data;
    }

    /**
     * Retrieves the usernames of all entries stored in USERS excluding logged in user
     * @see AddFriends
     * @param user logged in user
     * @return ArrayList containing concatenated strings of usernames and their full names
     */
    public ArrayList<String> selectAllUsersBut(String user) {
        Log.d("selectAllUsersBut: user", "=== "+user);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_NAME_USERS,
                new String[]{COL_USERNAME, COL_FULLNAME},
                COL_USERNAME + " NOT IN ('" + user + "')", null,
                null, null, null, null);

        ArrayList<String> data = new ArrayList<>();
        if (cursor.getCount() > 0) {
            Log.d("selectAllUsersBut: cursor", "=== "+cursor.getCount());
            while(cursor.moveToNext()) {
                data.add((cursor.getString(1)).concat("\n@"+cursor.getString(0)));
            }
        }
        cursor.close();
        return data;
    }

    /**
     * Select query with multiple selectionArgs[]
     * @param table
     * @param users selectionArgs
     * @param col_ids
     * @return
     */
    public ArrayList<String> selectQuery(int table, ArrayList<String> users, int[] col_ids) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> data = new ArrayList<>();
        for(String u : users) {
            Log.d("selectQuery: users", "=== "+u);
            Cursor cursor = db.query(true, TABLE_NAMES[table], null,
                    COL_USERNAME + "=?", new String[]{u},
                    null, null, null, null);
            if (cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    for(int index : col_ids) {
                        data.add(cursor.getString(index));
                    }
                }
            }
            cursor.close();
        }
        return data;
    }

    public ArrayList<String> selectQuery(int table, ArrayList<String> users, String[] distinct) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> data = new ArrayList<>();
        for(String u : users) {
            Log.d("selectQuery: users", "=== "+u);
            Cursor cursor = db.query(true, TABLE_NAMES[table], distinct,
                    COL_USERNAME + "=?", new String[]{u},
                    null, null, null, null);
            if (cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    for(int i = 0; i < distinct.length; i++) {
                        data.add(cursor.getString(i));
                    }
                }
            }
            cursor.close();
        }
        return data;
    }

    public ArrayList<String> selectQuery(int table, String user, int[] col_ids, String whereClause) {
        Log.d("selectQuery: user", "=== "+user);
        if(whereClause == null) {
            return new ArrayList<>();
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "";
        switch(table) {
            case 1: // EVENTS
                selection = COL_USERNAME + "=? AND " + COL_EVENT_ID + "=?";
                break;
            case 3: // FRIENDS
                selection = COL_USERNAME + "=? AND " + COL_RELATION_TYPE + "=?";
                break;
        }

        ArrayList<String> data = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAMES[table], null, selection,
                new String[]{user, whereClause}, null, null, null);
        if (cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                for(int index : col_ids) {
                    data.add(cursor.getString(index));
                }
            }
        }
        cursor.close();
        return data;
    }

    public ArrayList<String> selectQuery(int table, String user, String[] distinct, String whereClause) {
        Log.d("selectQuery: user", "=== "+user);
        if(whereClause == null) {
            return new ArrayList<>();
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "";
        switch(table) {
            case 1: // EVENTS
                selection = COL_USERNAME + "=? AND " + COL_EVENT_ID + "=?";
                break;
            case 3: // FRIENDS
                selection = COL_USERNAME + "=? AND " + COL_RELATION_TYPE + "=?";
                break;
        }

        ArrayList<String> data = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAMES[table], distinct, selection,
                new String[]{user, whereClause}, null, null, null);
        if (cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                for(int i = 0; i < distinct.length; i++) {
                    data.add(cursor.getString(i));
                }
            }
        }
        cursor.close();
        return data;
    }

    /**
     * Update a row in some table with data
     - SQL Syntax
         UPDATE table_name
         SET column1 = 'value1', column2 = value2, ...
         WHERE condition;
     * @param table : TABLE_NAMES
     *              Users        : 0
     *              Events       : 1
     *              Event Reqs   : 2
     *              Friends      : 3
     *              Friend Reqs  : 4
     *              Profiles     : 5
     * @param data : ContentValues containing data to be updated
     */
    public int updateData(int table, ContentValues data,
                             @Nullable String user, @Nullable String friend,
                             @Nullable String EventID, @Nullable String EventDetails) {
        SQLiteDatabase db = this.getWritableDatabase();
        int affected = 0;
        switch(table) {
            case 0: // USERS
                affected = db.update(TABLE_NAME_USERS, data,
                        COL_USERNAME + "=?", new String[]{user});
                break;
            case 1: // EVENTS
                if(user == null || EventID == null) {
                    return 0;
                }
                affected = db.update(TABLE_NAME_EVENTS, data,
                        COL_USERNAME + "=? AND " + COL_EVENT_ID + "=?",
                        new String[]{user, EventID});
                break;
            case 2: // EVENT REQUESTS
                affected = db.update(TABLE_NAME_EVENT_REQUESTS, data,
                        COL_FRIEND + "=? AND " + COL_EVENT_INVITE + "=?",
                        new String[]{friend, EventDetails});
                break;
            case 3: // FRIENDS
                affected = db.update(TABLE_NAME_FRIENDS, data,
                        COL_USERNAME + "=? AND " + COL_FRIEND + "=? OR "
                                + COL_USERNAME + "=? AND " + COL_FRIEND + "=?",
                        new String[]{user, friend, friend, user});
                break;
            case 4: // FRIEND REQUESTS
                affected = db.update(TABLE_NAME_FRIEND_REQUESTS, data,
                        COL_USERNAME + "=? AND " + COL_RECEIVER + "=?",
                        new String[]{user, friend});
                break;
            case 5: // PROFILES
                affected = db.update(TABLE_NAME_PROFILES, data, COL_USERNAME + "=?",
                        new String[]{user});
                break;
        }
        db.close();
        return affected;
    }

    public int updateData(int EventID, ContentValues data) {
        SQLiteDatabase db = this.getWritableDatabase();
        int affected = db.update(TABLE_NAME_EVENTS, data, COL_EVENT_ID + "=" + EventID, null);
        db.close();
        return affected;
    }

    public int deleteData(int table, @Nullable String user, @Nullable String friend,
                           @Nullable String EventID, @Nullable String EventDetails) {
        SQLiteDatabase db = this.getWritableDatabase();
        int affected = 0;
        switch(table) {
            case 0: // USERS
                affected = db.delete(TABLE_NAME_USERS, COL_USERNAME + "=?", new String[]{user});
                affected += db.delete(TABLE_NAME_EVENTS, COL_USERNAME + "=?", new String[]{user});
                affected += db.delete(TABLE_NAME_EVENT_REQUESTS,
                        COL_USERNAME + "=? OR " + COL_FRIEND + "=?",
                        new String[]{user, user});
                affected += db.delete(TABLE_NAME_FRIENDS,
                        COL_USERNAME + "=? OR " + COL_FRIEND + "=?",
                        new String[]{user, user});
                affected += db.delete(TABLE_NAME_FRIEND_REQUESTS,
                        COL_USERNAME + "=? OR " + COL_RECEIVER + "=?",
                        new String[]{user, user});
                affected += db.delete(TABLE_NAME_PROFILES, COL_USERNAME + "=?", new String[]{user});
                break;
            case 1: // EVENTS
                affected = db.delete(TABLE_NAME_EVENTS,COL_EVENT_ID + "=?", new String[]{EventID});
                break;
            case 2: // EVENT REQUESTS
                affected = db.delete(TABLE_NAME_EVENT_REQUESTS, COL_EVENT_INVITE + "=?",
                        new String[]{EventDetails});
                break;
            case 3: // FRIENDS
                affected = db.delete(TABLE_NAME_FRIENDS,
                        COL_USERNAME + "=? AND " + COL_FRIEND + "=?",
                        new String[]{user, friend});
                break;
            case 4: // FRIEND REQUESTS
                affected = db.delete(TABLE_NAME_FRIEND_REQUESTS,
                        COL_USERNAME + "=? AND " + COL_RECEIVER + "=?",
                        new String[]{user, friend});
                break;
            case 5: // PROFILES
                // Cannot delete profile without deleting user
                break;
        }
        db.close();
        return affected;
    }

    /**
     * Special Case delete operation for EventID
     * @param EventID event identifier to delete
     * @param user specific user to delete event for
     * @return number of entries affected
     */
    public int deleteData(int EventID, @Nullable String[] user) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(user == null) {
            int affected = db.delete(TABLE_NAME_EVENTS, COL_EVENT_ID + "=" + EventID, null);
            db.close();
            return affected;
        }

        int affected = db.delete(TABLE_NAME_EVENTS, COL_USERNAME + "=? AND "
                + COL_EVENT_ID + "=" + EventID, user);
        db.close();
        return affected;
    }

    public byte[] getPasswordEntry(String user) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USERS, new String[]{COL_PASSWORD},
                COL_USERNAME + "=?", new String[]{user},null, null, null);
        cursor.moveToFirst();
        byte[] p = cursor.getBlob(0);
        cursor.close();
        db.close();
        return p;
    }

    /**
     * Specialized selectQuery that filters through `Events` table to find events for a user
     * that occur within the date
     * @param date Format in "MM/dd/yyyy"
     * @return ArrayList
     */
    public ArrayList<String> selectEventsDuring(String user, String date) {
        Log.d("selectEventsDuring: user", "=== "+user);
        ArrayList<String> eventsDurations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        if(user.contains("\n@")) {
            String username = user.split("\n@")[1];
            Cursor cursor = db.query(true, TABLE_NAME_EVENTS, new String[]{COL_EVENT_ID,
                            COL_START, COL_END, COL_EVENT_TYPE},
                    COL_USERNAME + "=?", new String[]{username},
                    null, null, null, null);
            if(cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    String[] startDate = (cursor.getString(1)).split(" - ", 2);
                    String[] endDate = (cursor.getString(2)).split(" - ", 2);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy",
                            Locale.getDefault());
                    LocalDate ds = LocalDate.parse(startDate[0], formatter);
                    LocalDate de = LocalDate.parse(endDate[0], formatter);
                    LocalDate seekDate = LocalDate.parse(date, formatter);
                    Log.d("Date Start", "=== "+ds);
                    Log.d("Date End", "=== "+de);
                    Log.d("seekDate", "=== "+seekDate);
                    if(seekDate.isEqual(ds)) {
                        if (ds.isEqual(de)) {
                            String duration = (startDate[1].split(" - ")[1]
                                    + "\n" + endDate[1].split(" - ")[1])
                                    + " : " + user
                                    + " : " + cursor.getString(3)
                                    + " : " + cursor.getInt(0);
                            eventsDurations.add(duration);
                        } else {
                            String duration = (startDate[1] + "\n" + endDate[1])
                                    + " : " + user
                                    + " : " + cursor.getString(3)
                                    + " : " + cursor.getInt(0);
                            eventsDurations.add(duration);
                        }
                    }
                }
            }
            cursor.close();
        } else {
            Cursor cursor = db.query(true, TABLE_NAME_EVENTS, new String[]{COL_EVENT_ID,
                            COL_START, COL_END, COL_EVENT_TYPE},
                    COL_USERNAME + "=?", new String[]{user},
                    null, null, null, null);
            if(cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    String[] startDate = (cursor.getString(1)).split(" - ", 2);
                    String[] endDate = (cursor.getString(2)).split(" - ", 2);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy",
                            Locale.getDefault());
                    LocalDate ds = LocalDate.parse(startDate[0], formatter);
                    LocalDate de = LocalDate.parse(endDate[0], formatter);
                    LocalDate seekDate = LocalDate.parse(date, formatter);
                    Log.d("Date Start", "=== "+ds);
                    Log.d("Date End", "=== "+de);
                    Log.d("seekDate", "=== "+seekDate);
                    if(seekDate.isEqual(ds)) {
                        String fullname = selectQuery(0, user, new int[]{1}).get(0);
                        if (ds.isEqual(de)) {
                            String duration = (startDate[1].split(" - ")[1]
                                    + "\n" + endDate[1].split(" - ")[1])
                                    + " : " + (fullname + "\n@" + user)
                                    + " : " + cursor.getString(3)
                                    + " : " + cursor.getInt(0);
                            eventsDurations.add(duration);
                        } else {
                            String duration = (startDate[1] + "\n" + endDate[1])
                                    + " : " + (fullname + "\n@" + user)
                                    + " : " + cursor.getString(3)
                                    + " : " + cursor.getInt(0);
                            eventsDurations.add(duration);
                        }
                    }
                }
            }
            cursor.close();
        }
        return eventsDurations;
    }

    public ArrayList<String> selectEventsDuring(String user, String date, String time) {
        Log.d("selectEventsDuring: user", "=== "+user);
        ArrayList<String> eventsDurations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        if(user.contains("\n@")) {
            String username = user.split("\n@")[1];
            Cursor cursor = db.query(true, TABLE_NAME_EVENTS, new String[]{COL_EVENT_ID,
                            COL_START, COL_END, COL_EVENT_TYPE},
                    COL_USERNAME + "=?", new String[]{username},
                    null, null, null, null);
            if(cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    String[] startDate = (cursor.getString(1)).split(" - ");
                    String[] endDate = (cursor.getString(2)).split(" - ");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a",
                            Locale.getDefault());
                    LocalDateTime ds = LocalDateTime.parse(startDate[0] + " " + startDate[2], formatter);
                    LocalDateTime de = LocalDateTime.parse(endDate[0] + " " + endDate[2], formatter);
                    LocalDateTime seekDate = LocalDateTime.parse(date + " " + time, formatter);
                    Log.d("Date Start", "=== "+ds);
                    Log.d("Date End", "=== "+de);
                    Log.d("seekDate", "=== "+seekDate);
                    if(seekDate.isEqual(ds) || seekDate.isBefore(ds)) {
                        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MM/dd/yyyy",
                                Locale.getDefault());
                        LocalDate ds1 = LocalDate.parse(startDate[0], formatter1);
                        LocalDate de1 = LocalDate.parse(endDate[0], formatter1);
                        if(ds1.isEqual(de1)) {
                            String duration = (startDate[2] + "\n" + endDate[2])
                                    + " : " + user
                                    + " : " + cursor.getString(3)
                                    + " : " + cursor.getInt(0);
                            eventsDurations.add(duration);
                        } else {
                            String duration = ((startDate[1] + " - " + startDate[2])
                                    + "\n" + (endDate[1] + " - " + endDate[2]))
                                    + " : " + user
                                    + " : " + cursor.getString(3)
                                    + " : " + cursor.getInt(0);
                            eventsDurations.add(duration);
                        }
                    }
                }
            }
            cursor.close();
        } else {
            Cursor cursor = db.query(true, TABLE_NAME_EVENTS, new String[]{COL_EVENT_ID,
                            COL_START, COL_END, COL_EVENT_TYPE},
                    COL_USERNAME + "=?", new String[]{user},
                    null, null, null, null);
            if(cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    String[] startDate = (cursor.getString(1)).split(" - ");
                    String[] endDate = (cursor.getString(2)).split(" - ");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a",
                            Locale.getDefault());
                    LocalDateTime ds = LocalDateTime.parse(startDate[0] + " " + startDate[2], formatter);
                    LocalDateTime de = LocalDateTime.parse(endDate[0] + " " + endDate[2], formatter);
                    LocalDateTime seekDate = LocalDateTime.parse(date + " " + time, formatter);
                    Log.d("Date Start", "=== "+ds);
                    Log.d("Date End", "=== "+de);
                    Log.d("seekDate", "=== "+seekDate);
                    if(seekDate.isEqual(ds) || seekDate.isBefore(ds)) {
                        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MM/dd/yyyy",
                                Locale.getDefault());
                        LocalDate ds1 = LocalDate.parse(startDate[0], formatter1);
                        LocalDate de1 = LocalDate.parse(endDate[0], formatter1);
                        String fullname = selectQuery(0, user, new int[]{1}).get(0);
                        if(ds1.isEqual(de1)) {
                            String duration = (startDate[2] + "\n" + endDate[2])
                                    + " : " + (fullname + "\n@" + user)
                                    + " : " + cursor.getString(3)
                                    + " : " + cursor.getInt(0);
                            eventsDurations.add(duration);
                        } else {
                            String duration = ((startDate[1] + " - " + startDate[2])
                                    + "\n" + (endDate[1] + " - " + endDate[2]))
                                    + " : " + (fullname + "\n@" + user)
                                    + " : " + cursor.getString(3)
                                    + " : " + cursor.getInt(0);
                            eventsDurations.add(duration);
                        }
                    }
                }
            }
            cursor.close();
        }
        return eventsDurations;
    }

    public ArrayList<String> selectEventsWithinWeek(String user, String date)
            throws ParseException {
        ArrayList<String> eventsDurations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_NAME_EVENTS, new String[]{COL_EVENT_ID,
                        COL_EVENT_NAME, COL_START, COL_END},
                COL_USERNAME + "=?", new String[]{user},
                null, null, null, null);

        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                String[] startDate = (cursor.getString(2)).split(" - ");
                String[] endDate = (cursor.getString(3)).split(" - ");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy",
                        Locale.getDefault());
                LocalDate ds = LocalDate.parse(startDate[0], formatter);
                LocalDate de = LocalDate.parse(endDate[0], formatter);
                LocalDate seekDate = LocalDate.parse(date, formatter);
                Log.d("Date Start", "=== "+ds);
                Log.d("Date End", "=== "+de);
                Log.d("seekDate", "=== "+seekDate);
                if(seekDate.isEqual(ds)) {
                    DateFormat to   = new SimpleDateFormat("MMM, dd", Locale.getDefault()); // wanted format
                    DateFormat from = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()); // current format
                    String month_day = (to.format(from.parse(date)));
                        String duration = month_day
                                + ", " + startDate[2] // h:mm a
                                + ", " + endDate[2] // h:mm a
                                + ", " + cursor.getString(1); // Event Name
                        eventsDurations.add(duration);
                }
            }
        }
        cursor.close();
        return eventsDurations;
    }

    public ArrayList<String> selectWithTags(String user) {
        Log.d("selectFriendsWithTags: user", "=== "+user);
        ArrayList<String> friendTags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_FRIENDS, new String[]{COL_FRIEND,
                        COL_RELATION_TYPE}, COL_USERNAME + "=?", new String[]{user},
                null, null, null);
        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                String fullname = selectQuery(0, cursor.getString(0), new int[]{1}).get(0);
                String ft = fullname+"\n@"+cursor.getString(0)+':'+cursor.getString(1);
                friendTags.add(ft);
            }
        }
        cursor.close();
        return friendTags;
    }

    /**
     * Special `selectQuery` used for Friend Requests table
     * @param user whereArg
     * @param isRequester determines the column used in WHERE clause - if user isRequester, results are `outgoing`; otherwise, requests are `incoming`
     * @return ArrayList of users corresponding to incoming or outgoing requests
     */
    public ArrayList<String> selectWithTags(String user, boolean isRequester) {
        Log.d("selectWithTags: user", "=== "+user);
        ArrayList<String> friendTags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if(isRequester) {
            cursor = db.query(TABLE_NAME_FRIEND_REQUESTS,
                    new String[]{COL_RECEIVER, COL_RELATION_TYPE},
                    COL_USERNAME + "=?", new String[]{user},
                    null, null, null);
        } else {
            cursor = db.query(TABLE_NAME_FRIEND_REQUESTS,
                    new String[]{COL_USERNAME, COL_RELATION_TYPE},
                    COL_RECEIVER + "=?", new String[]{user},
                    null, null, null);
        }

        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                String fullname = selectQuery(0, cursor.getString(0), new int[]{1}).get(0);
                String ft = fullname+"\n@"+cursor.getString(0)+':'+cursor.getString(1);
                friendTags.add(ft);
            }
        }
        cursor.close();
        return friendTags;
    }

    public ArrayList<String> selectRequests(String user, boolean isRequester) {
        Log.d("selectRequests: user", "=== "+user);
        ArrayList<String> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if(isRequester) {
            cursor = db.query(TABLE_NAME_FRIEND_REQUESTS,
                    new String[]{COL_RECEIVER},
                    COL_USERNAME + "=?", new String[]{user},
                    null, null, null);
        } else {
            cursor = db.query(TABLE_NAME_FRIEND_REQUESTS,
                    new String[]{COL_USERNAME},
                    COL_RECEIVER + "=?", new String[]{user},
                    null, null, null);
        }

        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                String fullname = selectQuery(0, cursor.getString(0), new int[]{1}).get(0);
                String ft = fullname.concat("\n@"+cursor.getString(0));
                requests.add(ft);
            }
        }
        cursor.close();
        return requests;
    }

    public String getColFullname() { return COL_FULLNAME; }
    public String getColUsername() { return COL_USERNAME; }
    public String getColReceiver() { return COL_RECEIVER; }
    public String getColRelationType() { return COL_RELATION_TYPE; }
    public String getColFriend() { return COL_FRIEND; }
    public String getColEventId() { return COL_EVENT_ID; }
    public String getColEventName() { return COL_EVENT_NAME; }
    public String getColStart() { return COL_START; }
    public String getColEnd() { return COL_END; }
    public String getColDisplayName() {
        return COL_DISPLAY_NAME;
    }
    public String getColAboutMe() {
        return COL_ABOUT_ME;
    }
    public String getColProfilePic() {
        return COL_PROFILE_PIC;
    }

    public ArrayList<Integer> getEventIDs() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_EVENTS, new String[]{COL_EVENT_ID}, null,
                null, null, null, null, null);

        ArrayList<Integer> data = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                data.add(cursor.getInt(0));
            }
        }
        cursor.close();
        return data;
    }

    /**
     * Retrieves all GroupIDs from table
     * @return
     */
    public ArrayList<Integer> getGroupIDs() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_GROUPS, new String[]{COL_GROUP_ID}, null,
                null, null, null, null, null);

        ArrayList<Integer> data = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                data.add(cursor.getInt(0));
            }
        }
        cursor.close();
        return data;
    }

    /**
     * Retrieve count of entries based on value of user parameter
     * @param table : TABLE_NAMES
     *              Users        : 0
     *              Events       : 1
     *              Event Reqs   : 2
     *              Friends      : 3
     *              Friend Reqs  : 4
     *              Profiles     : 5
     * @param user  : Filters and returns query results by user
     * @return count of entries
     */
    public int getCount(int table, String user, String[] distinctCols) {
        Log.d("selectQuery: user", "=== "+user);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_NAMES[table], distinctCols,
                COL_USERNAME + "=?", new String[]{user},
                null, null, null, null);

        int data = cursor.getCount();
        cursor.close();
        return data;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EVENT_REQUESTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FRIEND_REQUESTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PROFILES);
        onCreate(db);
    }
}
