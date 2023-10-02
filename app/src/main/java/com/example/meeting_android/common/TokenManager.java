package com.example.meeting_android.common;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "mitting_webrtc_android";
    private static final String KEY_TOKEN = "jwt_token";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}
