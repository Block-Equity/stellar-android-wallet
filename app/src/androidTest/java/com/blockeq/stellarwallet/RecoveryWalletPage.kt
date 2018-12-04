package com.blockeq.stellarwallet

import android.content.Context
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.RootMatchers
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

    fun putPhrase(phrase : String) : RecoveryWalletPage {
        onView(ViewMatchers.withId(R.id.phraseEditText)).perform(ViewActions.typeText(phrase))
        return this
    }

    fun putSecretKey(key : String) : RecoveryWalletPage {
        onView(ViewMatchers.withId(R.id.secretKeyEditText)).perform(ViewActions.typeText(key))
        return this
    }

    fun clickPassphrase() : RecoveryWalletPage {
        onView(ViewMatchers.withId(R.id.passphraseButton)).perform(ViewActions.click())
        return this
    }

    fun proceedWithPassphrase(context : Context, phrase: String) : RecoveryWalletPage {
        onView(ViewMatchers.withId(R.id.passphraseEditText)).perform(ViewActions.typeText(phrase))

        Espresso.onView(ViewMatchers.withText(context.getString(R.string.ok)))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .perform(ViewActions.click())

        val string = context.getString(R.string.ok)

        Espresso.onView(ViewMatchers.withText(string))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .perform(ViewActions.click())

        Thread.sleep(1000)

        onView(ViewMatchers.withId(R.id.passphraseEditText)).perform(ViewActions.typeText(phrase))

        Espresso.onView(ViewMatchers.withText(context.getString(R.string.ok)))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())

        onView(ViewMatchers.withText(context.getString(R.string.ok))).perform(ViewActions.click())

        return this
    }
}
