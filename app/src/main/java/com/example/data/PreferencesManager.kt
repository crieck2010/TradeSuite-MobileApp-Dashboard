package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ConnectionConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "trade_suite_prefs")

class PreferencesManager(private val context: Context) {

  companion object {
    val KEY_SERVER_URL = stringPreferencesKey("server_url")
    val KEY_OPEND_HOST = stringPreferencesKey("opend_host")
    val KEY_OPEND_PORT = intPreferencesKey("opend_port")
    val KEY_API_KEY = stringPreferencesKey("api_key")
    val KEY_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
    val KEY_POLL_INTERVAL = intPreferencesKey("poll_interval")
  }

  val connectionConfigFlow: Flow<ConnectionConfig> =
    context.dataStore.data.map { preferences ->
      ConnectionConfig(
        serverUrl = preferences[KEY_SERVER_URL] ?: "http://10.0.2.2:8000",
        openDHost = preferences[KEY_OPEND_HOST] ?: "127.0.0.1",
        openDPort = preferences[KEY_OPEND_PORT] ?: 11111,
        apiKey = preferences[KEY_API_KEY] ?: "",
        isDemoMode = preferences[KEY_DEMO_MODE] ?: true,
        pollIntervalSec = preferences[KEY_POLL_INTERVAL] ?: 3
      )
    }

  suspend fun updateConfig(config: ConnectionConfig) {
    context.dataStore.edit { preferences ->
      preferences[KEY_SERVER_URL] = config.serverUrl
      preferences[KEY_OPEND_HOST] = config.openDHost
      preferences[KEY_OPEND_PORT] = config.openDPort
      preferences[KEY_API_KEY] = config.apiKey
      preferences[KEY_DEMO_MODE] = config.isDemoMode
      preferences[KEY_POLL_INTERVAL] = config.pollIntervalSec
    }
  }

  suspend fun setDemoMode(isDemo: Boolean) {
    context.dataStore.edit { preferences ->
      preferences[KEY_DEMO_MODE] = isDemo
    }
  }
}
