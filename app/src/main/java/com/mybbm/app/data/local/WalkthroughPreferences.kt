package com.mybbm.app.data.local

import android.content.Context

/**
 * Menyimpan status apakah pengguna sudah pernah melihat App Walkthrough (1x pada pemakaian pertama).
 */
object WalkthroughPreferences {
    private const val PREFS_NAME = "mybbm_prefs"
    private const val KEY_HAS_SEEN_WALKTHROUGH = "has_seen_walkthrough"

    fun hasSeenWalkthrough(context: Context): Boolean {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_SEEN_WALKTHROUGH, false)
    }

    fun setHasSeenWalkthrough(context: Context, seen: Boolean) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_SEEN_WALKTHROUGH, seen)
            .apply()
    }
}
