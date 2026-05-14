package com.example.easytrack.session;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager acts as our app's memory for the current user.
 * It uses SharedPreferences to save small pieces of data (like the user ID)
 * so that the user doesn't have to log in every time they open the app.
 */
public class SessionManager {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // Set up the shared preferences file
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Call this when a user successfully logs in to remember who they are.
     */
    public void createLoginSession(int userId, String username) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply(); // apply() saves changes in the background
    }

    /**
     * Keeps the session in sync if the user decides to change their username.
     */
    public void updateUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    // Quick helpers to grab the stored user details
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    /**
     * Wipes all stored data. Use this when the user clicks 'Sign Out'.
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    /**
     * Checks if we have a valid user ID stored. 
     * If not, the user is considered logged out.
     */
    public boolean isLoggedIn() {
        return getUserId() != -1;
    }
}
