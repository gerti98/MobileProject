package com.example.chatapp.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmotionProcessing {
    public static boolean isEmotionStringValid(String value){
        if(value.equals("joy") || value.equals("sadness") || value.equals("fear") || value.equals("neutral") || value.equals("anger"))
            return true;
        else
            return false;
    }

    public static String getEmotionClassMajority(List<String> emotions){
        Map<String,Integer> emotionMap = new HashMap<>();
        Integer old;
        String maxEmotion = null;
        int max = 0;

        emotionMap.put("joy", 0);
        emotionMap.put("sadness", 0);
        emotionMap.put("fear", 0);
        emotionMap.put("neutral", 0);
        emotionMap.put("anger", 0);


        for(String emotion: emotions){
            if(emotionMap.containsKey(emotion)){
                old = emotionMap.get(emotion);
                emotionMap.put(emotion, old+1);
                if(old + 1 > max){
                    max = old+1;
                    maxEmotion = emotion;
                }
            }
        }

        if(max == 0) {
            return null;
        }

        return maxEmotion;
    }
}
