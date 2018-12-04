package com.blockeq.stellarwallet

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.blockeq.stellarwallet.activities.LaunchActivity
import org.junit.After
import org.junit.Before
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

    private data class MnemonicWalletUT(val mnemonic:String, val passPhrase: String?,
                                        val expectedAccountId : String,
                                        val expectedSecretKey : String)

    private val pin = "1234"
    private val passphrase = "passphrase"
    private val mnemonic12 = "level seminar then wrist obscure use normal soldier nephew frequent resemble return"
    private val mnemonic24 = "slender job catalog super settle stool renew stomach lonely deputy notable dice evolve snap nature tell rally fine visa donate stay have devote liquid"

    private lateinit var appContext : Context

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule<LaunchActivity>(LaunchActivity::class.java)

    @Before
    fun onBefore(){
        appContext = InstrumentationRegistry.getTargetContext().applicationContext
    }

    @Test
    fun testRecoverWalletOption12Words() {
        executeMnemonicWalletTest(MnemonicWalletUT(mnemonic12, null,
                "GCCG2BX3L4S4F6HP3MP7BECCCE5H3NNMRA6W4ROHT3WID3QEG2K2CQK4",
                "SA4JW3ACFZIMZSYUNTH47TYRRRTANTE3ALFCHKLELFB7BF55DUY7KXWH"))
    }

    @Test
    fun testRecoverWalletOption24Words() {
        executeMnemonicWalletTest(MnemonicWalletUT(mnemonic24, null,
                "GCTZMUXHQ65ZXCOLFH7MEHIWEDCPA3M4HI7G7HHXSF64WZ56PIMHEMV2",
                "SDNRILYU7VO44PV6HLGS3HXUMUAHPNRPAVOVWO7L2KDXYH6QEOSTIP24"))
    }


    @Test
    fun testRecoverWalletOption12WordsWithPassphrase() {
        executeMnemonicWalletTest(MnemonicWalletUT(mnemonic12, passphrase,
                "GDWWYBFVH5YJAZ6WSTLAWT4BGK6YDEDT772KRAGDUJRVQEJKCIMIM5HH",
                "SDODL2YXHYKVMZDGCJORWXRQDYKRRXYQPVQYEY7JZVBYWAAE2XGA7MCQ"))
    }

    @Test
    fun testRecoverWalletOption24WordsWithPassphrase() {
        executeMnemonicWalletTest(MnemonicWalletUT(mnemonic24, passphrase,
                "GBZKPBFWSOW772JCUUS7RPNRZ5ATTWL453HUYHKN2OVFZNLDV33IU7EH",
                "SBVZWWIIMOVBMJ4OAEUOCLZA6ARSONYSQ3ZDQ7OS64EFKSJRLQA7GPIX"))
    }

    @Test
    fun testRecoverSecretSeed() {
        val secretKey = "SDLOPMAX6BPWTDVQZZAR47JCVKQM4EI52LP4XLDO75M7OA2C2XZ7Z3UZ"
        val expectedAccountId = "GDRAR4QYEGCR7ON2E2QWUFITC56LQGDI7RZ67MIQYQASVSMYNQSRCTL6"

        LaunchPage.onPageLoaded().clickRecoverFromSecretKey()
        RecoveryWalletPage.onPageLoaded().putSecretKey(secretKey)
        RecoveryWalletPage.onPageLoaded().next()
        PinPage.onPageLoaded().proceedWithPin(pin).proceedWithPin(pin)

        // assert expected accountId
        WalletPage.onPageLoaded().pressReceive()
        ReceivePage.onPageLoaded().assertAccount(expectedAccountId).goBack()

        assertSecretKeyFromWalletPage(secretKey)
    }

    private fun executeMnemonicWalletTest(wallet: MnemonicWalletUT){
        LaunchPage.onPageLoaded().clickRecoverFromPhrase()
        RecoveryWalletPage.onPageLoaded().putPhrase(wallet.mnemonic)

        if (wallet.passPhrase != null) {
            RecoveryWalletPage.onPageLoaded()
                    .clickPassphrase()
                    .proceedWithPassphrase(appContext, passphrase)
        }

        RecoveryWalletPage.onPageLoaded().next()
        PinPage.onPageLoaded().proceedWithPin(pin).proceedWithPin(pin)

        // assert expected accountId
        WalletPage.onPageLoaded().pressReceive()
        ReceivePage.onPageLoaded().assertAccount(wallet.expectedAccountId).goBack()
    }

    private fun assertSecretKeyFromWalletPage(secretKey: String) {
        WalletPage.onPageLoaded().pressSettings()
        SettingsPage.onPageLoaded().pressViewSecretKey()
        PinPage.onPageLoaded().proceedWithPin(pin)
        ViewSecretSeedPage.onPageLoaded().assertSecretSeed(secretKey).goBack()
    }

    private fun clearWallet() {
        WalletPage.onPageLoaded().pressSettings()
        SettingsPage.onPageLoaded().clearWallet()
        PinPage.onPageLoaded().proceedWithPin(pin)
        //restart
        LaunchPage.onPageLoaded()
    }

    @After
    fun tearDown(){
        clearWallet()
    }
}
