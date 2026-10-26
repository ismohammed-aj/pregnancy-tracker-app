package com.pfe.mamacareplus.notification

import android.content.Context

class NotificationStore(context: Context) {

    private val prefs = context.getSharedPreferences("mamacare_notifications", Context.MODE_PRIVATE)

    fun isRead(id: String): Boolean = readIds().contains(id)

    fun isCleared(id: String): Boolean = clearedIds().contains(id)

    fun markRead(id: String) {
        prefs.edit().putStringSet(KEY_READ, readIds() + id).apply()
    }

    fun markAllRead(ids: Collection<String>) {
        if (ids.isEmpty()) return
        prefs.edit().putStringSet(KEY_READ, readIds() + ids).apply()
    }

    fun clear(id: String) {
        prefs.edit()
            .putStringSet(KEY_CLEARED, clearedIds() + id)
            .putStringSet(KEY_READ, readIds() + id)
            .apply()
    }

    private fun readIds(): Set<String> = prefs.getStringSet(KEY_READ, emptySet()).orEmpty()

    private fun clearedIds(): Set<String> = prefs.getStringSet(KEY_CLEARED, emptySet()).orEmpty()

    companion object {
        private const val KEY_READ = "read_ids"
        private const val KEY_CLEARED = "cleared_ids"
    }
}
