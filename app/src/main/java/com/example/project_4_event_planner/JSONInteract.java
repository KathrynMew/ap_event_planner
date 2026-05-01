package com.example.project_4_event_planner;

import android.os.Environment;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class JSONInteract {

    private static final String[] paths = {
            Environment.getExternalStorageDirectory() + "/Documents/registered_users.json",
            Environment.getExternalStorageDirectory() + "/Documents/created_groups.json",
            Environment.getExternalStorageDirectory() + "/Documents/created_events.json",
            Environment.getExternalStorageDirectory() + "/Documents/event_alarms.json"
    };

    public static JSONArray jsonFileToString(int dir)
            throws IOException, JSONException {
        if(new File(paths[dir]).exists()) {
            JSONObject contents = new JSONObject();
            BufferedReader br = new BufferedReader(new FileReader(paths[dir]));
            String line = br.readLine();
            if(line != null) {
                line = line.replace("\"{", "{");
                line = line.replace("}\",", "},");
                line = line.replace("}\"", "}");
                contents = (new JSONObject(line));
            }
            br.close();

            switch(dir) {
                case 0:
                    return contents.getJSONArray("planner_users");
                case 1:
                    return contents.getJSONArray("planner_groups");
                case 2:
                    return contents.getJSONArray("planner_events");
                case 3:
                    return contents.getJSONArray("planner_alarms");
            }
        }

        return new JSONArray();
    }

    /**
     * Used for writing new JSONObjects to JSONFile that do not exist
     * @param dir index to use the paths[]
     * @param jsonObj JSONObject to be put into the corresponding JSONArray
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static boolean writeToJSONFile(int dir, JSONObject jsonObj)
            throws IOException, JSONException {
        JSONObject contents = new JSONObject();
        if(new File(paths[dir]).exists()){
            BufferedReader br = new BufferedReader(new FileReader(paths[dir]));
            String line = br.readLine();
            if(line != null) {
                line = line.replace("\"{", "{");
                line = line.replace("}\",", "},");
                line = line.replace("}\"", "}");
                contents = new JSONObject(line);
            }
            br.close();

            if(contents.has("planner_users")) {
                (contents.getJSONArray("planner_users")).put(jsonObj);
            } else if (contents.has("planner_groups")) {
                (contents.getJSONArray("planner_groups")).put(jsonObj);
            } else if (contents.has("planner_events")) {
                (contents.getJSONArray("planner_events")).put(jsonObj);
            } else if (contents.has("planner_alarms")) {
                /**
                 * {
                 * "Username":<String>,
                 * "User_Alarms":[{"Event_ID":<int>, "Time_in_Milliseconds":<long>, "Frequency":<int>, "Message":<String>}]
                 * }
                 */
                String u = jsonObj.getString("Username");
                JSONObject _user = getJSONObject(3, "Username", u);
                if (_user.isNull("Username")) {
                    contents.getJSONArray("planner_alarms").put(jsonObj);
                } else {
                    // If a JSONObject with key "Username" and value logged_in user,
                    // Add the new JSONObject in jsonObj "User_Alarms" to existing entry.
                    JSONArray json_arr = contents.getJSONArray("planner_alarms");
                    JSONObject _alarm = jsonObj.getJSONArray("User_Alarms").getJSONObject(0);
                    for (int i = 0; i < json_arr.length(); i++) {
                        JSONObject planAlarmsObjs = json_arr.getJSONObject(i);
                        if (planAlarmsObjs.getString("Username").equalsIgnoreCase(u)) {
                            planAlarmsObjs.getJSONArray("User_Alarms").put(_alarm);
                            contents.getJSONArray("planner_alarms").put(i, planAlarmsObjs);
                            break;
                        }
                    }
                }
            }
        } else {
            switch(dir) {
                case 0:
                    contents.put("planner_users", new JSONArray().put(jsonObj));
                    break;
                case 1:
                    contents.put("planner_groups", new JSONArray().put(jsonObj));
                    break;
                case 2:
                    contents.put("planner_events", new JSONArray().put(jsonObj));
                    break;
                case 3:
                    contents.put("planner_alarms", new JSONArray().put(jsonObj));
            }
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(paths[dir]));
        bw.write(contents.toString());
        bw.close();

        return existsJSONObject(dir, jsonObj);
    }

    /**
     * Overwriting method for existing JSONObjects
     * @param dir [0 : Users] | [1 : Groups] | [2 : Events] | [3: Alarms]
     * @param jsonObj JSONObject to replace existing one.
     * @param user which user to replace for given dir equals either 0 or 3
     * @return true if successful
     * @throws IOException
     * @throws JSONException
     */
    public static boolean writeToJSONFile(int dir, JSONObject jsonObj, String user)
            throws IOException, JSONException {
        JSONObject contents = new JSONObject();
        if(new File(paths[dir]).exists()){
            BufferedReader br = new BufferedReader(new FileReader(paths[dir]));
            String line = br.readLine();
            if(line != null) {
                line = line.replace("\"{", "{");
                line = line.replace("}\",", "},");
                line = line.replace("}\"", "}");
                contents = new JSONObject(line);
            }
            br.close();

            if(contents.has("planner_users")) {
                JSONArray users = contents.getJSONArray("planner_users");
                for(int i = 0; i < users.length(); i++) {
                    JSONObject usersJSONObj = users.getJSONObject(i);
                    if(user.equals(usersJSONObj.getString("Username"))) {
                        users.put(i, jsonObj);
                        break;
                    }
                }
            } else if (contents.has("planner_groups")) {
                (contents.getJSONArray("planner_groups")).put(jsonObj);
            } else if (contents.has("planner_events")) {
                // (contents.getJSONArray("planner_events")).put(jsonObj);
                JSONArray events = contents.getJSONArray("planner_events");
                for(int i = 0; i < events.length(); i++) {
                    JSONObject usersJSONObj = events.getJSONObject(i);
                    if(jsonObj.getInt("Event_ID") == (usersJSONObj.getInt("Event_ID"))) {
                        events.put(i, jsonObj);
                        break;
                    }
                }
            } else if (contents.has("planner_alarms")) {
                /* {"planner_alarms":[
                {"Username":<String>,
                 "User_Alarms":[{"Event_ID":<int>, "Time_in_Milliseconds":<long>, "Frequency":<int>, "Message":<String>}]
                 }]}
                 */
                JSONArray alarms = contents.getJSONArray("planner_alarms");
                boolean needSearch = true;
                for(int i = 0; i < alarms.length(); i++) {
                    JSONObject alarmsJSONObj = alarms.getJSONObject(i);
                    if(needSearch) {
                        if(user.equals(alarmsJSONObj.getString("Username"))) {
                            JSONArray userAlarms = alarmsJSONObj.getJSONArray("User_Alarms");
                            for(int j = 0; j < userAlarms.length(); j++) {
                                JSONObject alarmForEvent = userAlarms.getJSONObject(j);
                                if(alarmForEvent.getInt("Event_ID") == jsonObj.getInt("Event_ID")) {
                                    userAlarms.put(j, jsonObj);
                                    contents.getJSONArray("planner_alarms").put(i, userAlarms);
                                    needSearch = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
        } else {
            return false;
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(paths[dir]));
        bw.write(contents.toString());
        bw.close();

        return existsJSONObject(dir, jsonObj);
    }

    /**
     * Used if deleting a User. Should be called if User is deleted from Database
     * @param dir
     * @param identifier
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static boolean deleteFromJSONFile(int dir, String identifier)
            throws IOException, JSONException {
        String jsonObjectName = null;
        JSONObject contents = new JSONObject();
        if(new File(paths[dir]).exists()){
            BufferedReader br = new BufferedReader(new FileReader(paths[dir]));
            String line = br.readLine();
            if(line != null) {
                line = line.replace("\"{", "{");
                line = line.replace("}\",", "},");
                line = line.replace("}\"", "}");
                contents = new JSONObject(line);
            }
            br.close();

            if(contents.has("planner_users")) {
                jsonObjectName = "planner_users";
                JSONArray users = contents.getJSONArray("planner_users");
                for(int i = 0; i < users.length(); i++) {
                    JSONObject usersJSONObj = users.getJSONObject(i);
                    if(identifier.equals(usersJSONObj.getString("Username"))) {
                        users.remove(i);
                        break;
                    }
                }
            }
        } else {
            return false;
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(paths[dir]));
        bw.write(contents.toString());
        bw.close();

        assert jsonObjectName != null;
        return (getJSONObject(dir, "Username", identifier).equals(new JSONObject()));
    }

    public static boolean deleteFromJSONFile(int dir, int identifier)
            throws IOException, JSONException {
        String jsonObjectKey = null;
        JSONObject contents = new JSONObject();
        if(new File(paths[dir]).exists()){
            BufferedReader br = new BufferedReader(new FileReader(paths[dir]));
            String line = br.readLine();
            if(line != null) {
                line = line.replace("\"{", "{");
                line = line.replace("}\",", "},");
                line = line.replace("}\"", "}");
                contents = new JSONObject(line);
            }
            br.close();

            if (contents.has("planner_groups")) {
                jsonObjectKey = "Group_ID";
                JSONArray groups = contents.getJSONArray("planner_groups");
                for(int i = 0; i < groups.length(); i++) {
                    JSONObject groupJSONObj = groups.getJSONObject(i);
                    if(identifier == (groupJSONObj.getInt("Group_ID"))) {
                        groups.remove(i);
                        break;
                    }
                }
            } else if (contents.has("planner_events")) {
                jsonObjectKey = "Event_ID";
                JSONArray events = contents.getJSONArray("planner_events");
                for(int i = 0; i < events.length(); i++) {
                    JSONObject eventJSONObj = events.getJSONObject(i);
                    if(identifier == (eventJSONObj.getInt("Event_ID"))) {
                        events.remove(i);
                        // TODO: Deleting an event should also delete any alarms connected to it
                        break;
                    }
                }
            }
        } else {
            return false;
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(paths[dir]));
        bw.write(contents.toString());
        bw.close();

        assert jsonObjectKey != null;
        return (getJSONObject(dir, jsonObjectKey, identifier).equals(new JSONObject()));
    }

    private static boolean existsJSONObject(int dir, JSONObject jsonObj)
            throws IOException, JSONException {
        JSONArray a = jsonFileToString(dir);
        for(int i = 0; i < a.length(); i++) {
            String o = a.getJSONObject(i).toString();
            if(o.equals(jsonObj.toString())) {
                return true;
            }
        }
        return false;
    }

    public static JSONObject getJSONObject(int dir, String objKey, String findValue)
            throws IOException, JSONException {
        JSONArray arr = jsonFileToString(dir);
        for(int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            if(findValue.equals(obj.getString(objKey))) {
                return obj;
            }
        }
        return new JSONObject();
    }

    public static JSONObject getJSONObject(int dir, String objKey, int findValue)
            throws IOException, JSONException {
        JSONArray arr = jsonFileToString(dir);
        for(int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            if(findValue == (obj.getInt(objKey))) {
                return obj;
            }
        }
        return new JSONObject();
    }

    public static boolean contains(JSONArray jsonArr, String findValue)
            throws JSONException {
        ArrayList<String> temporary = new ArrayList<>();
        for(int i = 0; i < jsonArr.length(); i++) {
            temporary.add(jsonArr.getString(i));
        }
        return temporary.contains(findValue);
    }

    /**
     * Check if a JSONArray contains any value in a given String[]
     * @param jsonArr JSONArray to iterate through
     * @param findValues String[] holds values to search for
     * @return boolean => false if JSONArray does not contain any value in String[]
     * @throws JSONException
     */
    public static boolean contains(JSONArray jsonArr, String[] findValues)
            throws JSONException {
        for(int i = 0; i < jsonArr.length(); i++) {
            for (String findV : findValues) {
                if (jsonArr.getString(i).equals(findV)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean contains(JSONArray jsonArr, int findValue)
            throws JSONException {
        ArrayList<Integer> temporary = new ArrayList<>();
        for(int i = 0; i < jsonArr.length(); i++) {
            temporary.add(jsonArr.getInt(i));
        }
        return temporary.contains(findValue);
    }

    public static ArrayList<JSONObject> getUserGroups(String user)
            throws JSONException, IOException {
        JSONArray userGroupIDs = getJSONObject(0, "Username", user).getJSONArray("User_Groups");
        JSONArray groups = jsonFileToString(1); // Reading created_groups.json

        // userGroups contains JSONObjects from "planner_groups"
        ArrayList<JSONObject> userGroups = new ArrayList<>();
        for(int i = 0; i < userGroupIDs.length(); i++) {
            for(int j = 0; j < groups.length(); j++) {
                JSONObject g = groups.getJSONObject(j);
                if(userGroupIDs.getInt(i) == g.getInt("Group_ID")) {
                    userGroups.add(g);
                    break;
                }
            }
        }
        return userGroups;
    }
}
