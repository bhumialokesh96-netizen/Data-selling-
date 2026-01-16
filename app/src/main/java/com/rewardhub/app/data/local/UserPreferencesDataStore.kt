package com.rewardhub.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rewardhub_prefs")

class UserPreferencesDataStore(private val context: Context) {
    
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        // Note: Key name maintained as "user_email" for backward compatibility with existing installations
        // This key now stores phone numbers instead of emails after migration to phone-based authentication
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }
    
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    // Returns phone number (stored in USER_EMAIL_KEY for backward compatibility)
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }
    
    // Saves phone number in USER_EMAIL_KEY for backward compatibility
    suspend fun saveUserInfo(userId: String, phoneNumber: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = phoneNumber
        }
    }
    
    suspend fun clearUserInfo() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
