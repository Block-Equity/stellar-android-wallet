package blockeq.com.stellarwallet

import android.content.Context
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalStateException

@RunWith(AndroidJUnit4::class)
class ApplicationTest {
    private lateinit var context : Context

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule<LoginActivity>(LoginActivity::class.java)

    @Before
    fun before(){
        context = activityTestRule.activity.applicationContext
    }

    @Test
    fun testRestoreWallet() {


    }


    @Test
    fun testCreateWalletOption12Words() {
        onView(withId(R.id.createWalletButton)).perform(click())

        createWalletFlow(MnemonicType.WORD_12, "1234")

        onView(withId(R.id.nav_settings)).perform(click())

        onView(withId(R.id.clearWalletButton)).perform(click())

        writePin("1234")

        onView(withId(R.id.createWalletButton))
    }

    @Test
    fun testCreateWalletOption24Words() {
        onView(withId(R.id.createWalletButton)).perform(click())

        createWalletFlow(MnemonicType.WORD_24, "1234")

        onView(withId(R.id.nav_settings)).perform(click())

        onView(withId(R.id.clearWalletButton)).perform(click())

        writePin("1234")

        onView(withId(R.id.createWalletButton))
    }

    enum class MnemonicType {
        WORD_12,
        WORD_24
    }

    private fun createWalletFlow(option : MnemonicType, PIN : String) {
        val optionString : String = when (option) {
            MnemonicType.WORD_12 -> context.getString(R.string.create_word_option_1)
            MnemonicType.WORD_24 -> context.getString(R.string.create_word_option_2)
        }

        onView(withText(optionString))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click())

        onView(withId(R.id.confirmButton)).perform(click())

        writePin("1234")

        writePin("1234")

        onView(withId(R.id.navigationView))

    }

    private fun writePin(PIN : String) {
        if (PIN.length != 4) {
           throw IllegalStateException("PIN has to have 4 characters, now it has " + PIN.length)
        }

        writeIndividualPinNumber(PIN[0])
        writeIndividualPinNumber(PIN[1])
        writeIndividualPinNumber(PIN[2])
        writeIndividualPinNumber(PIN[3])
    }

    private fun writeIndividualPinNumber(number : Char) {
        onView(withId(R.id.pinLockView))
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder> (
                        hasDescendant(withText(number.toString())), click()))
    }

}