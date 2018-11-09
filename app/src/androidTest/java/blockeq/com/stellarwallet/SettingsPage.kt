package blockeq.com.stellarwallet

import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers

import android.support.test.espresso.Espresso.onView

class SettingsPage : BasePage() {
    override fun onPageLoaded(): SettingsPage {
        onView(ViewMatchers.withId(R.id.clearWalletButton))
        return this
    }

    fun clearWallet() {
        onView(ViewMatchers.withId(R.id.clearWalletButton)).perform(ViewActions.click())
    }
}
