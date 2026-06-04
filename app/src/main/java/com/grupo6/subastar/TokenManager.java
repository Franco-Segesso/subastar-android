package com.grupo6.subastar;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class TokenManager {

    private static final String PREFS_NAME = "subastar_secure_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        try {
            // Generamos o recuperamos la llave maestra del Keystore de Android
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Inicializamos las preferencias encriptadas
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

    // Método para cerrar sesión borrando el token
    public void clearToken() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(KEY_TOKEN).apply();
        }
    }
}