package com.blockeq.stellarwallet

import android.support.test.espresso.matcher.ViewMatchers

import android.support.test.espresso.Espresso.onView

object MnemonicPage : BasePage() {
    override fun onPageLoaded(): MnemonicPage {
        onView(ViewMatchers.withId(R.id.pinLockView))
        return this
    }
}
