package com.example.chatapp.util;

import com.example.chatapp.Message;

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
}
