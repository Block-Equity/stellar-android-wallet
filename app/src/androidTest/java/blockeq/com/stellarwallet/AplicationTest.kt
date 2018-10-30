package blockeq.com.stellarwallet

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import blockeq.com.stellarwallet.activities.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationTest {
    @Rule
    @JvmField
    val activity = ActivityTestRule<LoginActivity>(LoginActivity::class.java)

    @Test
    fun testRestoreWallet() {


    }


    @Test
    fun testCreateWalletOption1() {
        onView(withId(R.id.createWalletButton)).perform(click())

        onView(withText(activity.activity.getString(R.string.create_word_option_1)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
        .perform(click())

        onView(withId(R.id.confirmButton)).perform(click())

        addPin("1","2","3","4")
        addPin("1","2","3","4")

        onView(withId(R.id.navigationView))

        onView(withId(R.id.nav_settings)).perform(click())

        onView(withId(R.id.clearWalletButton)).perform(click())

        addPin("1","2","3","4")

        onView(withId(R.id.createWalletButton))
    }

    @Test
    fun testCreateWalletOption2() {


    }

    private fun addPin(firstNumber : String, secondNumber : String, thirdNumber : String, forthNumber : String) {
        addIndividualPinNumber(firstNumber)
        addIndividualPinNumber(secondNumber)
        addIndividualPinNumber(thirdNumber)
        addIndividualPinNumber(forthNumber)
    }

    private fun addIndividualPinNumber(number : String) {
        onView(withId(R.id.pinLockView))
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder> (
                        hasDescendant(withText(number)), click()))
    }

}