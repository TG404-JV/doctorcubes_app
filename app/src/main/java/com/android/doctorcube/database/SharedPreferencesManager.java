package com.android.doctorcube.database;


import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "user_details";
    private SharedPreferences sharedPreferences;
    private static SharedPreferencesManager instance;

    // Private constructor to enforce singleton pattern
    public SharedPreferencesManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing SharedPreferences", e);
        }
    }

    // Public static method to get the singleton instance
    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context);
        }
        return instance;
    }

    // Method to save user data
    public void saveUserData(Map<String, Object> userData) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_role", (String) userData.get("role"));
        editor.putString("user_name", (String) userData.get("name"));
        editor.putString("user_email", (String) userData.get("email"));
        editor.putString("user_phone", (String) userData.get("phone"));
        editor.putString("user_country", (String) userData.get("country"));
        editor.putString("user_state", (String) userData.get("state"));
        editor.putString("user_city", (String) userData.get("city"));
        editor.putBoolean("user_hasNeetScore", (boolean) userData.get("hasNeetScore"));
        if ((boolean) userData.get("hasNeetScore")) {
            editor.putString("user_neetScore", (String) userData.get("neetScore"));
        }
        editor.putBoolean("user_hasPassport", (boolean) userData.get("hasPassport"));
        editor.apply();
    }

    // Method to check if user data is stored
    public boolean isUserDataStored() {
        return sharedPreferences.contains("user_name"); //checks for name, returns true if name exists.
    }

    // Method to check if student data is stored
    public boolean isStudentDataStored() {
        return sharedPreferences.contains("user_hasNeetScore");
    }

    //Getter and Setter Methods for all the fields

    public void setUserRole(String userRole){
        sharedPreferences.edit().putString("user_role",userRole).apply();
    }
    public String getUserRole(){
        return sharedPreferences.getString("user_role",null);
    }

    public void setUserName(String userName){
        sharedPreferences.edit().putString("user_name",userName).apply();
    }
    public String getUserName(){
        return sharedPreferences.getString("user_name",null);
    }

    public void setUserEmail(String userEmail){
        sharedPreferences.edit().putString("user_email",userEmail).apply();
    }
    public String getUserEmail(){
        return sharedPreferences.getString("user_email",null);
    }

    public void setUserPhone(String userPhone){
        sharedPreferences.edit().putString("user_phone",userPhone).apply();
    }
    public String getUserPhone(){
        return sharedPreferences.getString("user_phone",null);
    }

    public void setUserCountry(String userCountry){
        sharedPreferences.edit().putString("user_country",userCountry).apply();
    }
    public String getUserCountry(){
        return sharedPreferences.getString("user_country",null);
    }

    public void setUserState(String userState){
        sharedPreferences.edit().putString("user_state",userState).apply();
    }
    public String getUserState(){
        return sharedPreferences.getString("user_state",null);
    }

    public void setUserCity(String userCity){
        sharedPreferences.edit().putString("user_city",userCity).apply();
    }
    public String getUserCity(){
        return sharedPreferences.getString("user_city",null);
    }

    public void setUserHasNeetScore(boolean userHasNeetScore){
        sharedPreferences.edit().putBoolean("user_hasNeetScore",userHasNeetScore).apply();
    }
    public boolean getUserHasNeetScore(){
        return sharedPreferences.getBoolean("user_hasNeetScore",false);
    }

    public void setUserNeetScore(String userNeetScore){
        sharedPreferences.edit().putString("user_neetScore",userNeetScore).apply();
    }
    public String getUserNeetScore(){
        return sharedPreferences.getString("user_neetScore",null);
    }

    public void setUserHasPassport(boolean userHasPassport){
        sharedPreferences.edit().putBoolean("user_hasPassport",userHasPassport).apply();
    }
    public boolean getUserHasPassport(){
        return sharedPreferences.getBoolean("user_hasPassport",false);
    }
    // Method to get all user data
    public Map<String, Object> getUserData() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("user_role", sharedPreferences.getString("user_role", null));
        userData.put("user_name", sharedPreferences.getString("user_name", null));
        userData.put("user_email", sharedPreferences.getString("user_email", null));
        userData.put("user_phone", sharedPreferences.getString("user_phone", null));
        userData.put("user_country", sharedPreferences.getString("user_country", null));
        userData.put("user_state", sharedPreferences.getString("user_state", null));
        userData.put("user_city", sharedPreferences.getString("user_city", null));
        userData.put("user_hasNeetScore", sharedPreferences.getBoolean("user_hasNeetScore", false));
        if(getUserHasNeetScore()){
            userData.put("user_neetScore", sharedPreferences.getString("user_neetScore", null));
        }
        userData.put("user_hasPassport", sharedPreferences.getBoolean("user_hasPassport", false));

        return userData;
    }

    // Method to clear all data
    public void clearPreferences() {
        sharedPreferences.edit().clear().apply();
    }
}


