package com.example.data

import android.content.Context
import android.content.SharedPreferences

class VaultPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("dialer_vault_prefs", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_PERMISSIONS_COMPLETED = "permissions_completed"
    private const val KEY_VAULT_PIN = "vault_pin"
    private const val KEY_ALL_FILES_HANDLED = "all_files_handled"
  }

  var isPermissionsCompleted: Boolean
    get() = prefs.getBoolean(KEY_PERMISSIONS_COMPLETED, false)
    set(value) = prefs.edit().putBoolean(KEY_PERMISSIONS_COMPLETED, value).apply()

  var isAllFilesHandled: Boolean
    get() = prefs.getBoolean(KEY_ALL_FILES_HANDLED, false)
    set(value) = prefs.edit().putBoolean(KEY_ALL_FILES_HANDLED, value).apply()

  var vaultPin: String?
    get() = prefs.getString(KEY_VAULT_PIN, null)
    set(value) = prefs.edit().putString(KEY_VAULT_PIN, value).apply()

  fun hasPin(): Boolean {
    return !vaultPin.isNullOrEmpty()
  }

  fun verifyPin(pin: String): Boolean {
    val savedPin = vaultPin
    return savedPin != null && savedPin == pin
  }

  fun clearAll() {
    prefs.edit().clear().apply()
  }
}
