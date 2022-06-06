package com.staygrateful.app.server.utils;

import android.content.Context;

import androidx.annotation.RawRes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceUtils {

    public static String getString(Context context, @RawRes int id) {
        StringBuilder myData = new StringBuilder();
        try {
            final InputStream inputStream = context.getResources().openRawResource(id);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String strLine;

            while ((strLine = reader.readLine()) != null) {
                myData.append(strLine).append("\n");
            }
            reader.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myData.toString();
    }
}
