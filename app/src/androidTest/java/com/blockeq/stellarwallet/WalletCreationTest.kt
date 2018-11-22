package com.blockeq.stellarwallet

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.blockeq.stellarwallet.activities.LaunchActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * IMPORTANT: make sure that the app and previous tests are not installed in the device.
 * Run sh uninstallApk.sh first. This could be solved using orchestrator tests.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class WalletCreationTest {
    private val pin = "1234"

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule<LaunchActivity>(LaunchActivity::class.java)

    @Test
    fun testCreateWalletOption12Words() {
        LaunchPage.onPageLoaded().createWallet(MnemonicType.WORD_12, pin)
        WalletPage.onPageLoaded().pressSettings()
        SettingsPage.onPageLoaded().clearWallet()
        PinPage.onPageLoaded().proceedWithPin(pin)
        //restart
        LaunchPage.onPageLoaded()
    }

    @Test
    fun testCreateWalletOption24Words() {
        LaunchPage.onPageLoaded().createWallet(MnemonicType.WORD_24, pin)
        WalletPage.onPageLoaded().pressSettings()
        SettingsPage.onPageLoaded().clearWallet()
        PinPage.onPageLoaded().proceedWithPin(pin)
        //restart
        LaunchPage.onPageLoaded()
    }


    @Test
    fun test_cancelling_from_recovery_flow() {
        //TDD for GH-52
        LaunchPage.onPageLoaded().clickRecoverFromSecretKey()
        RecoveryWalletPage.onPageLoaded().next().goBack()

        testCreateWalletOption12Words()
    }
}
