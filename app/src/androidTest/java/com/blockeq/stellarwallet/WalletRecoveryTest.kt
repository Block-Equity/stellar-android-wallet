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
class WalletRecoveryTest {

    private val pin = "1234"
    private val passphrase = "passphrase"
    private val secretKey = "SDLOPMAX6BPWTDVQZZAR47JCVKQM4EI52LP4XLDO75M7OA2C2XZ7Z3UZ"

    private val mnemonic12 = "level seminar then wrist obscure use normal soldier nephew frequent resemble return"
    private val mnemonic24 = "slender job catalog super settle stool renew stomach lonely deputy notable dice evolve snap nature tell rally fine visa donate stay have devote liquid"

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule<LaunchActivity>(LaunchActivity::class.java)

    private fun checkAccountId(accountId: String) {
        WalletPage.onPageLoaded().pressReceive()
        ReceivePage.onPageLoaded().checkAccount(accountId)
        ReceivePage.onPageLoaded().goBack()
    }

    private fun clearWallet() {
        WalletPage.onPageLoaded().pressSettings()
        SettingsPage.onPageLoaded().clearWallet()
        PinPage.onPageLoaded().proceedWithPin(pin)
        //restart
        LaunchPage.onPageLoaded()
    }

    @Test
    fun testRecoverWalletOption12Words() {
        val accountId = "GCCG2BX3L4S4F6HP3MP7BECCCE5H3NNMRA6W4ROHT3WID3QEG2K2CQK4"

        LaunchPage.onPageLoaded().clickRecoverFromPhrase()
        RecoveryWalletPage.onPageLoaded().putPhrase(mnemonic12)
        RecoveryWalletPage.onPageLoaded().next()
        PinPage.onPageLoaded().proceedWithPin(pin).proceedWithPin(pin)
        checkAccountId(accountId)
        clearWallet()
    }

    @Test
    fun testRecoverWalletOption24Words() {
        val accountId = "GCTZMUXHQ65ZXCOLFH7MEHIWEDCPA3M4HI7G7HHXSF64WZ56PIMHEMV2"

        LaunchPage.onPageLoaded().clickRecoverFromPhrase()
        RecoveryWalletPage.onPageLoaded().putPhrase(mnemonic24)
        RecoveryWalletPage.onPageLoaded().next()
        PinPage.onPageLoaded().proceedWithPin(pin).proceedWithPin(pin)
        checkAccountId(accountId)
        clearWallet()
    }


    @Test
    fun testRecoverWalletOption12WordsWithPassphrase() {
        val accountId = "GDWWYBFVH5YJAZ6WSTLAWT4BGK6YDEDT772KRAGDUJRVQEJKCIMIM5HH"

        LaunchPage.onPageLoaded().clickRecoverFromPhrase()
        RecoveryWalletPage.onPageLoaded().putPhrase(mnemonic12)
        RecoveryWalletPage.onPageLoaded().clickPassphrase()
        RecoveryWalletPage.onPageLoaded().proceedWithPassphrase(activityTestRule.activity.applicationContext, passphrase)

        RecoveryWalletPage.onPageLoaded().next()
        PinPage.onPageLoaded().proceedWithPin(pin).proceedWithPin(pin)
        checkAccountId(accountId)
        clearWallet()
    }

    @Test
    fun testRecoverWalletOption24WordsWithPassphrase() {
        val accountId = "GBZKPBFWSOW772JCUUS7RPNRZ5ATTWL453HUYHKN2OVFZNLDV33IU7EH"

        LaunchPage.onPageLoaded().clickRecoverFromPhrase()
        RecoveryWalletPage.onPageLoaded().putPhrase(mnemonic24)
        RecoveryWalletPage.onPageLoaded().clickPassphrase()
        RecoveryWalletPage.onPageLoaded().proceedWithPassphrase(activityTestRule.activity.applicationContext, passphrase)

        RecoveryWalletPage.onPageLoaded().next()
        PinPage.onPageLoaded().proceedWithPin(pin).proceedWithPin(pin)
        checkAccountId(accountId)
        clearWallet()
    }

    @Test
    fun testRecoverSecretSeed() {
        val accountId = "GDRAR4QYEGCR7ON2E2QWUFITC56LQGDI7RZ67MIQYQASVSMYNQSRCTL6"

        LaunchPage.onPageLoaded().clickRecoverFromSecretKey()
        RecoveryWalletPage.onPageLoaded().putSecretKey(secretKey)
        RecoveryWalletPage.onPageLoaded().next()
        PinPage.onPageLoaded().proceedWithPin(pin).proceedWithPin(pin)
        checkAccountId(accountId)
        clearWallet()
    }
}
