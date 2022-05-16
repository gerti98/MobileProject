package com.example.chatapp.connection;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class LocalFileManager {
    private String TAG = "FileManager";
    public Uri createFileFromString(String fileToWrite, String localFilename, Context context){

        File file = null;
        try {
            Writer output = null;
            String recFilePath = context.getExternalCacheDir().getAbsolutePath();
            recFilePath += localFilename;

            file = new File(recFilePath);
            output = new BufferedWriter(new FileWriter(file));
            output.write(fileToWrite);
            output.close();
            Log.i(TAG, "Saved");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Uri.fromFile(file);
    }

}
