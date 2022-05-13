package com.example.chatapp;

public class NotificationMessageEntity {
    String sender;
    String type;
    boolean checked;

    private NotificationMessageEntity(){}

    public NotificationMessageEntity(String sender, String type, boolean checked){
        this.sender = sender;
        this.type = type;
        this.checked = checked;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
