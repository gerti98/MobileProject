package com.example.chatapp;

// class to represent a chat message
public class Message {
    String text;
    String sender_name;
    String receiver_name;
    Long timestamp;
    boolean isAudio;

    private Message(){
    }

    public Message(String text, String sender_name, String receiver_name, Long timestamp){
        this.text = text;
        this.sender_name = sender_name;
        this.receiver_name = receiver_name;
        this.timestamp = timestamp;
        this.isAudio = false;
    }

    public Message(String text, String sender_name, String receiver_name, Long timestamp, boolean isAudio){
        this.text = text;
        this.sender_name = sender_name;
        this.receiver_name = receiver_name;
        this.timestamp = timestamp;
        this.isAudio = isAudio;
    }



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getReceiver_name() {
        return receiver_name;
    }

    public void setReceiver_name(String receiver_name) {
        this.receiver_name = receiver_name;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getIsAudio() { return isAudio; }

    public void setIsAudio(boolean isAudio) { this.isAudio = isAudio; }
}
