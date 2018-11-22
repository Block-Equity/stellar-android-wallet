package com.blockeq.stellarwallet

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isRoot

object RecoveryWalletPage : BasePage() {

    override fun onPageLoaded(): RecoveryWalletPage {
        onView(ViewMatchers.withId(R.id.nextButton))
        return this
    }

    fun next() : RecoveryWalletPage {
        onView(ViewMatchers.withId(R.id.nextButton)).perform(ViewActions.click())
        return this
    }

    fun goBack() : RecoveryWalletPage {
        onView(isRoot()).perform(ViewActions.pressBack())
        return this
    }
}