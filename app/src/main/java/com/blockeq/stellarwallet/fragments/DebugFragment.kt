package com.blockeq.stellarwallet.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.utils.GlobalGraphHelper

class DebugFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.debug_preferences)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key?.let {
           if (getString(R.string.preference_debug_test_server) == key) {
               activity?.let { activity ->
                   GlobalGraphHelper.wipe(activity)
                   view?.postDelayed({
                       android.os.Process.killProcess(android.os.Process.myPid())
                   }, 400)

               }
           }
        }
    }
}