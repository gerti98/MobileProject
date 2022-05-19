package com.example.chatapp.dto;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private String value;
    private String uid;
    private String name;
    private String email;

    private User() {
    }

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;;
    }

    public User (JSONObject json) {
        try {
            this.name = json.getString("name");
            this.uid = json.getString("uid");
            this.email = json.getString("email");
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", uid);
            jsonObject.put("name", name);
            jsonObject.put("email", email);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public String toString() {
       // String ret = uid + " - " + name + " - " + email;
        String ret = name + " - " + email;
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        if (!(o instanceof User)) {
            return false;
        }

        // typecast u to User so that we can compare data members
        User u = (User) o;

        // Compare the data members and return accordingly
        if(u.getEmail() == this.email)
            return true;
        else
            return false;
    }
}
