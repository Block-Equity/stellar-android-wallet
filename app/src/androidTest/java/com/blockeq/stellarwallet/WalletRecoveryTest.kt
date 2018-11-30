package com.blockeq.stellarwallet

import android.support.test.runner.AndroidJUnit4
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
class WalletRecoveryTest {

    private val pin = "1234"

    private val mnemonic12 = "level seminar then wrist obscure use normal soldier nephew frequent resemble return"
    private val mnemonic24 = "slender job catalog super settle stool renew stomach lonely deputy notable dice evolve snap nature tell rally fine visa donate stay have devote liquid"

    @Test
    fun testRecoverWalletOption12Words() {
        LaunchPage.onPageLoaded().clickRecoverFromPhrase()
        RecoveryWalletPage.onPageLoaded().putPhrase(mnemonic12)
        RecoveryWalletPage.onPageLoaded().next()
        PinPage.onPageLoaded().proceedWithPin(pin).proceedWithPin(pin)
    }

    @Test
    fun testRecoverWalletOption24Words() {
        LaunchPage.onPageLoaded().clickRecoverFromPhrase()
        RecoveryWalletPage.onPageLoaded().putPhrase(mnemonic24)
        RecoveryWalletPage.onPageLoaded().next()
        PinPage.onPageLoaded().proceedWithPin(pin).proceedWithPin(pin)
    }
}
