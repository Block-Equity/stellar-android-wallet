package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.contrib.ActivityResultMatchers
import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import android.support.test.runner.AndroidJUnit4
import com.blockeq.stellarwallet.PinPage
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.GlobalGraphHelper
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class WalletManagerActivityTest {
    private val mnemonic = "fee dog ivory manual dash little train senior one hospital hobby cat"
    private val pin = "1234"

    private var context : Context = InstrumentationRegistry.getTargetContext()
    @get:Rule
    var rule = ActivityTestRule<WalletManagerActivity>(WalletManagerActivity::class.java, true, false)

    @Test
    fun createWalletPin() {
        rule.launchActivity(WalletManagerActivity.createWallet(context, mnemonic, null))
        PinPage.onPageLoaded().proceedWithPin(pin).proceedWithPin(pin)
        MatcherAssert.assertThat<Instrumentation.ActivityResult>(rule.activityResult, ActivityResultMatchers.hasResultCode(Activity.RESULT_OK))
    }

    @Test
    fun verifyPin() {
        createWalletPin()
        rule.launchActivity(WalletManagerActivity.verifyPin(context))
        PinPage.onPageLoaded().proceedWithPin(pin)
        MatcherAssert.assertThat<Instrumentation.ActivityResult>(rule.activityResult, ActivityResultMatchers.hasResultCode(Activity.RESULT_OK))
    }

    @Test
    fun showSecretSeed() {
        createWalletPin()
        rule.launchActivity(WalletManagerActivity.showSecretSeed(context))
        PinPage.onPageLoaded().proceedWithPin(pin)
        MatcherAssert.assertThat<Instrumentation.ActivityResult>(rule.activityResult, ActivityResultMatchers.hasResultCode(Activity.RESULT_OK))
        assert(WalletManagerActivity.getResultDataString(rule.activityResult.resultData) != null)
        assert(WalletManagerActivity.getResultDataString(rule.activityResult.resultData) != "")
    }

    @Test
    fun showMnemonic() {
        createWalletPin()
        rule.launchActivity(WalletManagerActivity.showSecretSeed(context))
        PinPage.onPageLoaded().proceedWithPin(pin)
        MatcherAssert.assertThat<Instrumentation.ActivityResult>(rule.activityResult, ActivityResultMatchers.hasResultCode(Activity.RESULT_OK))
        MatcherAssert.assertThat<Instrumentation.ActivityResult>(rule.activityResult, ActivityResultMatchers.hasResultCode(Activity.RESULT_OK))
        assert(WalletManagerActivity.getResultDataString(rule.activityResult.resultData) != null)
        assert(WalletManagerActivity.getResultDataString(rule.activityResult.resultData) != "")
    }

    @After
    fun tearDown() {
        GlobalGraphHelper.wipe(context.applicationContext)
    }
}