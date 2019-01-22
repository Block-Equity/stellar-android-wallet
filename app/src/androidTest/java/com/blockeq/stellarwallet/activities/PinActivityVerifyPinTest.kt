package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.contrib.ActivityResultMatchers.hasResultCode
import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.blockeq.stellarwallet.PinPage
import junit.framework.TestCase.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class PinActivityVerifyPinTest {
    val pin:String = "1234"

    @get:Rule
    val rule = object : ActivityTestRule<PinActivity>(PinActivity::class.java) {
        override fun getActivityIntent(): Intent {
            return PinActivity.newInstance(InstrumentationRegistry.getTargetContext(), pin)
        }
    }

    @Test
    fun verify_pin_result_ok() {
        PinPage.onPageLoaded().proceedWithPin(pin)
        assertThat<Instrumentation.ActivityResult>(rule.activityResult, hasResultCode(Activity.RESULT_OK))
        assertEquals(PinActivity.getPinFromIntent(rule.activityResult.resultData), pin)
    }

    @Test
    fun verify_pin_result_canceled() {
        PinPage.onPageLoaded().pressBack()
        assertThat<Instrumentation.ActivityResult>(rule.activityResult, hasResultCode(Activity.RESULT_CANCELED))
        assertEquals(PinActivity.getPinFromIntent(rule.activityResult.resultData), null)
    }
}