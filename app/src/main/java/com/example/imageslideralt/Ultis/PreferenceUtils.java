package com.example.imageslideralt.Ultis;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {
    // pref file name
    private static final String PREFS_FILE_NAME = "image_browser_pref";
    public static void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime){
        SharedPreferences sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }
    public static boolean isFirstTimeAskingPermission(Context context, String permission){
        return context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE).getBoolean(permission, true);
    }
}
