package com.example.chatapp.dto;

import java.io.Serializable;

// class to represent a chat message
public class Message implements Serializable {
    String text;
    String filename;
    String sender_name;
    String receiver_name;

    public String getSender_uid() {
        return sender_uid;
    }

    public void setSender_uid(String sender_uid) {
        this.sender_uid = sender_uid;
    }

    String sender_uid;
    Long timestamp;
    boolean isAudio;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    int counter;

    private Message(){
    }

    public Message(String text, String sender_name, String receiver_name, Long timestamp){
        this.text = text;
        this.sender_name = sender_name;
        this.receiver_name = receiver_name;
        this.timestamp = timestamp;
        this.isAudio = false;
        this.filename = "";
    }

    public Message(String text, String sender_name, String receiver_name, Long timestamp, boolean isAudio){
        this.text = text;
        this.sender_name = sender_name;
        this.receiver_name = receiver_name;
        this.timestamp = timestamp;
        this.isAudio = isAudio;
        this.filename = "";
    }

    public Message(String text, String filename, String sender_name, String receiver_name, Long timestamp, boolean isAudio){
        this.text = text;
        this.sender_name = sender_name;
        this.receiver_name = receiver_name;
        this.timestamp = timestamp;
        this.isAudio = isAudio;
        this.filename = filename;
    }

    @Override //debug
    public String toString(){
        return this.text;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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
