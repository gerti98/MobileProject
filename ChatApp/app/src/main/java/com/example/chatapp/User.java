package com.example.chatapp;

public class User {

    private String uid;
    private String name;
    private String email;

    private User() {
    }

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
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
