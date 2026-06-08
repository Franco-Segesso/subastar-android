package com.grupo6.subastar;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class TokenManager {

    private static final String PREFS_NAME = "subastar_secure_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_CLIENTE_ID = "cliente_id";
    private SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        try {

            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();


            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToken(String token) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
        }
    }

    public String getToken() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_TOKEN, null);
        }
        return null;
    }

    public void saveClienteId(Integer clienteId) {
        if (sharedPreferences != null && clienteId != null) {
            sharedPreferences.edit().putInt(KEY_CLIENTE_ID, clienteId).apply();
        }
    }

    public Integer getClienteId() {
        if (sharedPreferences != null && sharedPreferences.contains(KEY_CLIENTE_ID)) {
            return sharedPreferences.getInt(KEY_CLIENTE_ID, -1);
        }
        return null;
    }


    public void clearToken() {
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .remove(KEY_TOKEN)
                    .remove(KEY_CLIENTE_ID)
                    .apply();
        }
    }
}
