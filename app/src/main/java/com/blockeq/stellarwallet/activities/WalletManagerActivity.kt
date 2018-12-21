package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.encryption.KeyStoreWrapper
import com.blockeq.stellarwallet.utils.AccountUtils
import java.lang.IllegalStateException

class WalletManagerActivity : AppCompatActivity() {
    private enum class ActionType {
        NEW_WALLET,
        RESTORE_WALLET,
        VERIFY_PIN,
        DECRYPT_SECRET_SEED,
        DECRYPT_MNEMONIC,
        /**
         * These are interim action types used in the actions NEW_WALLET & RESTORE_WALLET
         */
        ENTER_PIN,
        REENTER_PIN,
    }

    private lateinit var actionType : ActionType

    companion object {
        private const val INTENT_ARG_TYPE: String = "INTENT_ARG_TYPE"

        private const val INTENT_MNEMONIC_TYPE: String = "INTENT_MNEMONIC_TYPE"
        private const val INTENT_PASSPHRASE: String = "INTENT_PASSPHRASE"
        private const val INTENT_RECOVERY: String = "INTENT_RECOVERY"

        fun createWallet(context: Context, mnemonicString: String, passphrase: String?): Intent {
            val intent = Intent(context, WalletManagerActivity::class.java)
            intent.putExtra(INTENT_ARG_TYPE, ActionType.NEW_WALLET)
            intent.putExtra(INTENT_MNEMONIC_TYPE, mnemonicString)
            if (passphrase != null) {
                intent.putExtra(INTENT_PASSPHRASE, passphrase)
            }
            return intent
        }

        fun restore(context: Context, recoveryString: String, passphrase: String?): Intent {
            val intent = Intent(context, WalletManagerActivity::class.java)
            intent.putExtra(INTENT_ARG_TYPE, ActionType.RESTORE_WALLET)
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
        intent.getSerializableExtra(INTENT_ARG_TYPE)?.let {
            actionType = it as ActionType
        }

        when(actionType) {
            ActionType.RESTORE_WALLET,
            ActionType.NEW_WALLET -> {
                startActivityForResult(SimplePinActivity.newInstance(this, null, getString(R.string.please_create_a_pin)), ActionType.ENTER_PIN.ordinal)
            }
            ActionType.DECRYPT_MNEMONIC -> {
                startActivityForResult(SimplePinActivity.newInstance(this, getPinFromKeyStore(), getString(R.string.please_enter_your_pin)), ActionType.DECRYPT_MNEMONIC.ordinal)
            }
            ActionType.DECRYPT_SECRET_SEED -> {
                startActivityForResult(SimplePinActivity.newInstance(this, getPinFromKeyStore(), getString(R.string.please_enter_your_pin)), ActionType.DECRYPT_SECRET_SEED.ordinal)
            }
            ActionType.VERIFY_PIN -> {
                startActivityForResult(SimplePinActivity.newInstance(this, getPinFromKeyStore(), getString(R.string.please_enter_your_pin)), ActionType.VERIFY_PIN.ordinal)
            } else -> {
                throw IllegalStateException("invalid action type $actionType")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ActionType.ENTER_PIN.ordinal -> {
                startActivityForResult(SimplePinActivity.newInstance(this, getString(R.string.please_reenter_your_pin)), ActionType.REENTER_PIN.ordinal)
                return
            }
            ActionType.REENTER_PIN.ordinal -> {
                when(actionType) {
                    ActionType.NEW_WALLET -> {
                        val mnemonicString = intent.getStringExtra(INTENT_MNEMONIC_TYPE)
                        if (generateWallet(data, mnemonicString)) {
                            setResult(Activity.RESULT_OK)
                            finish()
                            return
                        }
                    }
                    ActionType.RESTORE_WALLET -> {
                        val recovery = intent.getStringExtra(INTENT_RECOVERY)
                        if (generateWallet(data, recovery)) {
                            setResult(Activity.RESULT_OK)
                            finish()
                            return
                        }
                    }
                    else -> {}
                }
            }
            ActionType.VERIFY_PIN.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK)
                    finish()
                    return
                }
            }
            ActionType.DECRYPT_MNEMONIC.ordinal -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val pin = SimplePinActivity.getPinFromIntent(data)
                    if (pin != null) {
                        val masterKey = AccountUtils.getPinMasterKey(applicationContext, pin)
                        if (masterKey != null) {
                            val encryptedPhrase = WalletApplication.localStore.encryptedPhrase!!
                            val decryptedPhrase = AccountUtils.getDecryptedString(encryptedPhrase, masterKey)
                            startActivity(MnemonicActivity.newDisplayMnemonicIntent(this, decryptedPhrase))
                            finish()
                            return
                        }
                    }
                }
            }
            ActionType.DECRYPT_SECRET_SEED.ordinal -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val pin = SimplePinActivity.getPinFromIntent(data)
                    if (pin != null) {
                        val masterKey = AccountUtils.getPinMasterKey(applicationContext, pin)
                        if (masterKey != null) {
                            val encryptedPhrase = WalletApplication.localStore.encryptedPhrase!!
                            val phrase = AccountUtils.getDecryptedString(encryptedPhrase, masterKey)

                            val encryptedPassphrase = WalletApplication.localStore.encryptedPassphrase
                            var passphrase: String?= null
                            if (encryptedPassphrase != null) {
                                passphrase = AccountUtils.getDecryptedString(encryptedPassphrase, masterKey)
                            }
                            val keyPair = AccountUtils.getStellarKeyPair(phrase, passphrase)
                            val secretSeed = keyPair.secretSeed.joinToString("")
                            startActivity(ViewSecretSeedActivity.newInstance(this, secretSeed))
                            finish()
                            return
                        }
                    }
                }
            }
        }

        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun getPinFromKeyStore() : String {
        return KeyStoreWrapper(applicationContext).getAliases().first()
    }

    private fun generateWallet(data:Intent?, secret: String) : Boolean {
        data?.let {
            val pin = SimplePinActivity.getPinFromIntent(it)
            val passphrase = intent.getStringExtra(INTENT_PASSPHRASE)

            pin?.let { that ->
                AccountUtils.generateWallet(applicationContext, secret, passphrase, that)
                return true
            }
        }
        return false
    }
}

