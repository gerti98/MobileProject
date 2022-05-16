package com.example.chatapp.util;

import com.example.chatapp.Message;

import java.util.ArrayList;
import java.util.List;

public class JSONBuilder {
    public static String buildMessageJSON(List<Message> messages){
        StringBuilder json = new StringBuilder("{ \"msgs\" : [");
        if(messages != null) {
            for (Message message : messages)
                if (!message.getIsAudio())
                    json.append("\"").append(message.getText()).append("\",");
            json.deleteCharAt(json.length()-1);
        }
        json.append("]}");
        return json.toString();
    }

    //List of list of this format expected:
    //      0        1
    //0: Message, label
    //1: Message, label
    //2: Message, label
    public static String buildLabelingJSON(ArrayList<ArrayList<Object>> labelingFormData){
        StringBuilder json = new StringBuilder("{ \"labels\" : [");
        Message message;
        String label;
        for (ArrayList<Object> labels : labelingFormData){
            message = (Message) labels.get(0);
            label = (String) labels.get(1);

            json.append("[\"").append(message.getText()).append("\",").append("\"").append(label).append("\"],");
        }
        json.deleteCharAt(json.length()-1);
        json.append("]}");
        return json.toString();

    }
}
