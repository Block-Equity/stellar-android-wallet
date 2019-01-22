package com.blockeq.stellarwallet

import android.content.Context
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isRoot

object RecoveryWalletPage : BasePage() {

    override fun onPageLoaded(): RecoveryWalletPage {
        onView(ViewMatchers.withId(R.id.bottomButton))
        return this
    }

    fun next() : RecoveryWalletPage {
        onView(ViewMatchers.withId(R.id.bottomButton)).perform(ViewActions.click())
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
        onView(ViewMatchers.withText(context.getString(R.string.ok))).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.passphraseEditText)).perform(ViewActions.typeText(phrase))
        onView(ViewMatchers.withText(context.getString(R.string.ok))).perform(ViewActions.click())
        return this
    }
}
