package com.blockeq.stellarwallet.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.blockeq.stellarwallet.R

class DebugPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val appContext: Context = context.applicationContext

    init {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    val isLeakCanaryEnabled: Boolean
        get() = sharedPreferences.getBoolean(appContext.getString(R.string.preference_debug_leak_canary), false)

    val isPinDisabled: Boolean
        get() = sharedPreferences.getBoolean(appContext.getString(R.string.preference_debug_pin_disabled), false)

    val isTradeTooltipEnabled: Boolean
        get() = sharedPreferences.getBoolean(appContext.getString(R.string.preference_debug_trade_tooltip), false)

    val isTestNetServerEnabled: Boolean
        get() = sharedPreferences.getBoolean(appContext.getString(R.string.preference_debug_test_server), false)
}