package blockeq.com.stellarwallet

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.v7.widget.RecyclerView

object PinPage : BasePage() {
    override fun onPageLoaded(): PinPage {
        onView(ViewMatchers.withId(R.id.pinLockView))
        return this
    }

    fun proceedWithPin(pin : String): PinPage {
        writePin(pin)
        return this
    }

    private fun writePin(pin : String) {

        if (pin.length != 4) {
            throw IllegalStateException("PIN has to have 4 characters, now it has " + pin.length)
        }

        writeIndividualPinNumber(pin[0])
        writeIndividualPinNumber(pin[1])
        writeIndividualPinNumber(pin[2])
        writeIndividualPinNumber(pin[3])
    }

    private fun writeIndividualPinNumber(number : Char) {
        Espresso.onView(ViewMatchers.withId(R.id.pinLockView))
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder> (
                        ViewMatchers.hasDescendant(ViewMatchers.withText(number.toString())), ViewActions.click()))
    }
}
