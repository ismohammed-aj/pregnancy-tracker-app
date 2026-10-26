package com.pfe.mamacareplus.utils.session

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mamacare_session", Context.MODE_PRIVATE)

    fun saveSession(
        token: String,
        userId: Int,
        email: String,
        role: String,
        femmeId: Int?
    ) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROLE, role)
            .putInt(KEY_FEMME_ID, femmeId ?: -1)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun getRole(): String? {
        return prefs.getString(KEY_ROLE, null)
    }

    fun getFemmeId(): Int? {
        val femmeId = prefs.getInt(KEY_FEMME_ID, -1)
        return if (femmeId == -1) null else femmeId
    }

    fun saveFemmeId(femmeId: Int) {
        prefs.edit()
            .putInt(KEY_FEMME_ID, femmeId)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_FEMME_ID = "femme_id"
    }
}
