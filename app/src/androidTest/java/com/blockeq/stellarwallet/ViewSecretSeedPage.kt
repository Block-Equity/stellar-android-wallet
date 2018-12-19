package com.blockeq.stellarwallet

import android.support.test.espresso.matcher.ViewMatchers

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions

object ViewSecretSeedPage : BasePage() {
    override fun onPageLoaded(): ViewSecretSeedPage {
        onView(ViewMatchers.withId(R.id.toolBar))
        return this
    }

    fun assertSecretSeed(secretSeed : String): ViewSecretSeedPage {
        // compare with account id on page
        onView(ViewMatchers.withId(R.id.secretSeedTextView)).check(ViewAssertions.matches(ViewMatchers.withText(secretSeed)))
        return this
    }

    fun goBack() : ViewSecretSeedPage {
        onView(ViewMatchers.isRoot()).perform(ViewActions.pressBack())
        return this
    }
}
