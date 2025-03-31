package com.example.yumbox.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_ROLE = "userRole";
    private static final String KEY_SESSION = "isLoggedIn";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Constructor
    public UserPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUserRole(String role) {
        if (!sharedPreferences.getBoolean(KEY_SESSION, false)) {
            editor.putString(KEY_ROLE, role);
            editor.putBoolean(KEY_SESSION, true);
            editor.apply();
        }
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    // Kiểm tra user đã đăng nhập trong phiên chưa
    public boolean isSessionActive() {
        return sharedPreferences.getBoolean(KEY_SESSION, false);
    }

    public void clearUserRole() {
        editor.remove(KEY_ROLE);
        editor.remove(KEY_SESSION);
        editor.apply();
    }
}
