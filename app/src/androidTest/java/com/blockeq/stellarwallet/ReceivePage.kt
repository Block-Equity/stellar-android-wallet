package com.blockeq.stellarwallet

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.withText

object ReceivePage : BasePage() {
    override fun onPageLoaded(): ReceivePage {
        onView(ViewMatchers.withId(R.id.addressEditText))
        return this
    }

    fun assertAccount(accountId : String): ReceivePage {
        // compare with account id on page
        onView(ViewMatchers.withId(R.id.addressEditText)).check(matches(withText(accountId)))
        return this
    }

    fun goBack() : ReceivePage {
        onView(ViewMatchers.isRoot()).perform(ViewActions.pressBack())
        return this
    }
}