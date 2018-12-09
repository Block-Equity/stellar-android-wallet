package com.blockeq.stellarwallet.fragments

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat

import com.blockeq.stellarwallet.R

class DebugFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(bundle: Bundle, s: String) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.debug_preferences)
    }
}