package com.example.chatapp.favorites;

import android.content.Context;
import android.os.Environment;

import com.example.chatapp.dto.User;
import com.example.chatapp.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

// This class handles the set of favorites of different logged user in this client
public class FavoritesHandler {
    private static HashMap<String, JSONObject> favoritesList;
    private static String filename;

    //load the favorites from file
    public static void loadUserFavorites(Context context) {
        filename = FirebaseAuth.getInstance().getUid() + "_favorites.json";

        //trying to open the file
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(filename);
            initUserFavorites(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            favoritesList = null;
        }

        //if filename does not exists we create a new one
        if (favoritesList == null){
            File file = new File(context.getFilesDir(), filename);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //empty favorites list
            favoritesList = new HashMap<>();
        }
    }

    //initialize the favorite's list
    private static void initUserFavorites(FileInputStream fis) {
        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        JSONArray favoritesJsonArray = new JSONArray();

        //reading the file's content
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //extracting the json array from the json string
        String jsonString = stringBuilder.toString();
        try {
            favoritesJsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        favoritesList = new HashMap<>();

        //if the array is empty, return
        if (favoritesJsonArray.isNull(0)) {
            return;
        }

        //initialize the favorite's list
        for (int i=0; i<favoritesJsonArray.length(); i++){
            try {
                JSONObject userJson = favoritesJsonArray.getJSONObject(i);
                String key = userJson.getString("uid");
                favoritesList.put(key, userJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<User> getFavoritesList(){
        if (favoritesList.size() == 0){
            return null;
        }
        ArrayList<User> usersList = new ArrayList<User>();
        for (JSONObject userJson: favoritesList.values()){
            usersList.add(new User (userJson));
        }
        return usersList;
    }

    //save the favorite's list to persistent json file
    public static void saveUserFavorites(Context context){
        JSONArray favoritesJsonArray = new JSONArray();

        for (JSONObject user: favoritesList.values()){
            favoritesJsonArray.put(user);
        }
        String fileContents = favoritesJsonArray.toString();

        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(fileContents.getBytes(StandardCharsets.UTF_8)); //maybe fileContents.toByteArray()
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearFavoritesFromMemory(){
        favoritesList.clear();
        filename = null;
    }

    public static boolean addToFavorites(User user){
        if (favoritesList.size() <= Constants.MAX_FAVORITES_LIST_SIZE) {
            favoritesList.put(user.getUid(), user.toJson());
            return true;
        }
        return false;
    }

    public static void removeFromFavorites(User user){
        favoritesList.remove(user.getUid());
    }

    public static boolean isFavorite(String uid){
        return favoritesList.containsKey(uid);
    }

}
