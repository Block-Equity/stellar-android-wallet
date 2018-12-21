package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
import com.blockeq.stellarwallet.fragments.SettingsFragment
import com.blockeq.stellarwallet.utils.AccountUtils
import java.lang.IllegalStateException

class WalletManagerActivity : AppCompatActivity() {
    private enum class ActionType {
        CREATE,
        RESTORE,
        VERIFY_PIN,
        DECRYPT_SECRET_SEED,
        DECRYPT_MNEMONIC
    }

    companion object {
        private const val INTENT_ARG_TYPE: String = "INTENT_ARG_TYPE"

        private const val INTENT_MNEMONIC_TYPE: String = "INTENT_MNEMONIC_TYPE"
        private const val INTENT_PASSPHRASE: String = "INTENT_PASSPHRASE"
        private const val INTENT_RECOVERY: String = "INTENT_RECOVERY"

        fun createWallet(context: Context, mnemonicString: String, passphrase: String?): Intent {
            val intent = Intent(context, WalletManagerActivity::class.java)
            intent.putExtra(INTENT_MNEMONIC_TYPE, mnemonicString)
            if (passphrase != null) {
                intent.putExtra(INTENT_PASSPHRASE, passphrase)
            }
            return intent
        }

        fun restore(context: Context, recoveryString: String, passphrase: String?): Intent {
            val intent = Intent(context, WalletManagerActivity::class.java)
            intent.putExtra(INTENT_ARG_TYPE, ActionType.RESTORE)
            intent.putExtra(INTENT_RECOVERY, recoveryString)
            if (passphrase != null) {
                intent.putExtra(INTENT_PASSPHRASE, passphrase)
            }
            return intent
        }

        fun verifyPin(context: Context) : Intent {
            val intent = Intent(context, WalletManagerActivity::class.java)
            intent.putExtra(INTENT_ARG_TYPE, ActionType.VERIFY_PIN)
            return intent
        }

        fun showSecretSeed(context: Context) : Intent {
            val intent = Intent(context, WalletManagerActivity::class.java)
            intent.putExtra(INTENT_ARG_TYPE, ActionType.DECRYPT_SECRET_SEED)
            return intent
        }

        fun showMnemonic(context: Context) : Intent {
            val intent = Intent(context, WalletManagerActivity::class.java)
            intent.putExtra(INTENT_ARG_TYPE, ActionType.DECRYPT_MNEMONIC)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!intent.hasExtra(INTENT_ARG_TYPE)) throw IllegalStateException("missing bundle extra {$INTENT_ARG_TYPE}")
        val type = intent.getSerializableExtra(INTENT_ARG_TYPE)
        when(type) {
            ActionType.CREATE -> {

            }

            ActionType.DECRYPT_MNEMONIC -> {
                startActivityForResult(SimplePinActivity.newInstance(this, getString(R.string.please_enter_your_pin)), ActionType.DECRYPT_MNEMONIC.ordinal)
            }
            ActionType.DECRYPT_SECRET_SEED -> {
                startActivityForResult(SimplePinActivity.newInstance(this, getString(R.string.please_enter_your_pin)), ActionType.DECRYPT_SECRET_SEED.ordinal)
            }
            ActionType.VERIFY_PIN -> startActivityForResult(SimplePinActivity.newInstance(this, getString(R.string.please_enter_your_pin)), ActionType.VERIFY_PIN.ordinal)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ActionType.VERIFY_PIN.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
            ActionType.DECRYPT_MNEMONIC.ordinal -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val pin = SimplePinActivity.getPinFromIntent(data)
                    if (pin != null) {
                        val foundMasterKey = AccountUtils.getPinMasterKey(applicationContext, pin)
                        if (foundMasterKey != null) {
                            val encryptedPhrase = WalletApplication.localStore.encryptedPhrase!!
                            val decryptedPhrase = AccountUtils.getDecryptedString(encryptedPhrase, foundMasterKey)
                            startActivity(MnemonicActivity.newDisplayMnemonicIntent(this, decryptedPhrase))
                            finish()
                        }
                    }
                }
            }
            ActionType.DECRYPT_SECRET_SEED.ordinal -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val pin = SimplePinActivity.getPinFromIntent(data)
                    if (pin != null) {
                        val foundMasterKey = AccountUtils.getPinMasterKey(applicationContext, pin)
                        if (foundMasterKey != null) {
                            val encryptedPhrase = WalletApplication.localStore.encryptedPhrase!!
                            val encryptedPassphrase = WalletApplication.localStore.encryptedPassphrase
                            val decryptedPhrase = AccountUtils.getDecryptedString(encryptedPhrase, foundMasterKey)
                            var decryptedPassphrase : String? = null
                            if (encryptedPassphrase != null) {
                                decryptedPassphrase = AccountUtils.getDecryptedString(encryptedPassphrase, foundMasterKey)
                            }

                            val keyPair = AccountUtils.getStellarKeyPair(decryptedPhrase, decryptedPassphrase)
                            val secretSeed = keyPair.secretSeed.joinToString("")
                            val intent = Intent(this, ViewSecretSeedActivity::class.java)

                            intent.putExtra(ViewSecretSeedActivity.SECRET_SEED, secretSeed)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
}

