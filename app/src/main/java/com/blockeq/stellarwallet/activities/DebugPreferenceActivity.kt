package com.blockeq.stellarwallet.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.fragments.DebugFragment

class DebugPreferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_preference)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.root_container, DebugFragment())
                    .commit()
        }
    }
}
