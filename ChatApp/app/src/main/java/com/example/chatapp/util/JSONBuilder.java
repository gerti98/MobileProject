package com.example.chatapp.util;

import com.example.chatapp.dto.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class JSONBuilder {
    public static String buildMessageTextJSON(List<Message> messages){
        JSONObject mainObj = new JSONObject();
        JSONArray ja = new JSONArray();
        try{
            if(messages != null) {
                for (Message message : messages)
                    if (!message.getIsAudio())
                        ja.put(message.getText());
            }
            mainObj.put("msgs", ja);
            StringWriter out = new StringWriter();
        } catch (Exception e){
            e.printStackTrace();
        }
        return mainObj.toString();
    }

        //List of list of this format expected:
    //      0        1
    //0: Message, label
    //1: Message, label
    //2: Message, label
    public static String buildLabelingJSON(ArrayList<ArrayList<Object>> labelingFormData){
        Message message;
        String label;
        JSONObject mainObj = new JSONObject();
        JSONArray text_ja = new JSONArray();
        JSONArray audio_ja = new JSONArray();
        JSONArray inner_ja;

        for (ArrayList<Object> labels : labelingFormData){
            message = (Message) labels.get(0);
            label = (String) labels.get(1);

            if(!message.getIsAudio()){
                inner_ja = new JSONArray();
                inner_ja.put(message.getText());
                inner_ja.put(label);
                text_ja.put(inner_ja);
            }
        }

        for (ArrayList<Object> labels : labelingFormData){
            message = (Message) labels.get(0);
            label = (String) labels.get(1);

            if(message.getIsAudio()){
                inner_ja = new JSONArray();
                inner_ja.put(message.getFilename().replace("/", ""));
                inner_ja.put(label);
                audio_ja.put(inner_ja);
            }
        }

        try {
            mainObj.put("text_labels", text_ja);
            mainObj.put("audio_labels", audio_ja);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mainObj.toString();
    }
}
