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
import com.blockeq.stellarwallet.utils.GlobalGraphHelper
import java.lang.IllegalStateException

class WalletManagerActivity : AppCompatActivity() {
    private enum class ActionType {
        NEW_WALLET,
        RESTORE_WALLET,
        VERIFY_PIN,
        DECRYPT_SECRET_SEED,
        DISPLAY_MNEMONIC,
        /**
         * These are interim action types used in the actions NEW_WALLET & RESTORE_WALLET
         */
        ENTER_PIN,
        REENTER_PIN,
    }

    private lateinit var actionType : ActionType

    companion object {
        private const val INTENT_ARG_TYPE: String = "INTENT_ARG_TYPE"
        private const val INTENT_PHRASE: String = "INTENT_PHRASE"
        private const val INTENT_PASSPHRASE: String = "INTENT_PASSPHRASE"

        private const val INTENT_RESULT_DATA: String = "INTENT_RESULT_DATA"
        private const val INTENT_RESULT_EXTRA_DATA: String = "INTENT_RESULT_EXTRA_DATA"

        fun restore(context: Context, recoveryString: String, passphrase: String?): Intent {
          return createWallet(context, recoveryString, passphrase)
        }

        fun createWallet(context: Context, mnemonicString: String, passphrase: String?): Intent {
            val intent = Intent(context, WalletManagerActivity::class.java)
            intent.putExtra(INTENT_ARG_TYPE, ActionType.NEW_WALLET)
            intent.putExtra(INTENT_PHRASE, mnemonicString)
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
            intent.putExtra(INTENT_ARG_TYPE, ActionType.DISPLAY_MNEMONIC)
            return intent
        }

        fun getResultDataString(intent:Intent?) : String? {
            if (intent == null) return null
            return intent.getStringExtra(INTENT_RESULT_DATA)
        }

        fun getResultExtraDataString(intent:Intent?) : String? {
            if (intent == null) return null
            return intent.getStringExtra(INTENT_RESULT_EXTRA_DATA)
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
                startActivityForResult(PinActivity.newInstance(this, null, getString(R.string.please_create_a_pin)), ActionType.ENTER_PIN.ordinal)
            }
            ActionType.DISPLAY_MNEMONIC -> {
                startActivityForResult(PinActivity.newInstance(this, getPinFromKeyStore(), getString(R.string.please_enter_your_pin)), ActionType.DISPLAY_MNEMONIC.ordinal)
            }
            ActionType.DECRYPT_SECRET_SEED -> {
                startActivityForResult(PinActivity.newInstance(this, getPinFromKeyStore(), getString(R.string.please_enter_your_pin)), ActionType.DECRYPT_SECRET_SEED.ordinal)
            }
            ActionType.VERIFY_PIN -> {
                startActivityForResult(PinActivity.newInstance(this, getPinFromKeyStore(), getString(R.string.please_enter_your_pin)), ActionType.VERIFY_PIN.ordinal)
            } else -> {
                throw IllegalStateException("invalid action type $actionType")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == PinActivity.RESULT_MAX_ATTEMPT_REACH) {
            GlobalGraphHelper.wipeAndRestart(this)
        } else {
            when (requestCode) {
                ActionType.ENTER_PIN.ordinal -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val pin = PinActivity.getPinFromIntent(data)
                        startActivityForResult(PinActivity.newInstance(this, pin, getString(R.string.please_reenter_your_pin)), ActionType.REENTER_PIN.ordinal)
                        return
                    }
                }
                ActionType.REENTER_PIN.ordinal -> {
                    if (resultCode == Activity.RESULT_OK) {
                        if (actionType == ActionType.NEW_WALLET || actionType ==  ActionType.RESTORE_WALLET) {
                            val phrase = intent.getStringExtra(INTENT_PHRASE)
                            if (generateWallet(data, phrase)) {
                                setResult(Activity.RESULT_OK)
                                finish()
                                return
                            }
                        }
                    }
                }
                ActionType.VERIFY_PIN.ordinal -> {
                    val pin = PinActivity.getPinFromIntent(data)
                    if (resultCode == Activity.RESULT_OK && data != null && pin != null) {
                        WalletApplication.userSession.setPin(pin)
                        setResult(Activity.RESULT_OK)
                        finish()
                        return
                    }
                }
                ActionType.DISPLAY_MNEMONIC.ordinal -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val pin = PinActivity.getPinFromIntent(data)
                        if (pin != null) {
                            val masterKey = AccountUtils.getPinMasterKey(applicationContext, pin)
                            if (masterKey != null) {
                                val encryptedPhrase = WalletApplication.wallet.getEncryptedPhrase()!!
                                WalletApplication.wallet.getEncryptedPassphrase()
                                val encryptedPassphrase = WalletApplication.wallet.getEncryptedPassphrase()
                                var passphrase: String?= null
                                if (encryptedPassphrase != null) {
                                    passphrase = AccountUtils.getDecryptedString(encryptedPassphrase, masterKey)
                                }
                                val decryptedPhrase = AccountUtils.getDecryptedString(encryptedPhrase, masterKey)
                                setResultData(decryptedPhrase, passphrase)
                                return
                            }
                        }
                    }
                }
                ActionType.DECRYPT_SECRET_SEED.ordinal -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val pin = PinActivity.getPinFromIntent(data)
                        if (pin != null) {
                            val masterKey = AccountUtils.getPinMasterKey(applicationContext, pin)
                            if (masterKey != null) {
                                val encryptedPhrase = WalletApplication.wallet.getEncryptedPhrase()!!
                                val phrase = AccountUtils.getDecryptedString(encryptedPhrase, masterKey)

                                val encryptedPassphrase = WalletApplication.wallet.getEncryptedPassphrase()
                                var passphrase: String?= null
                                if (encryptedPassphrase != null) {
                                    passphrase = AccountUtils.getDecryptedString(encryptedPassphrase, masterKey)
                                }
                                val keyPair = AccountUtils.getStellarKeyPair(phrase, passphrase)
                                val secretSeed = keyPair.secretSeed.joinToString("")

                                setResultData(secretSeed)
                                return
                            }
                        }
                    }
                }
            }

            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun setResultData(resultData : String, resultExtraData : String? = null) {
        val intent = Intent()
        intent.putExtra(INTENT_RESULT_DATA, resultData)
        if (resultExtraData != null) {
           intent.putExtra(INTENT_RESULT_EXTRA_DATA, resultExtraData)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
    private fun getPinFromKeyStore() : String {
        return KeyStoreWrapper(applicationContext).getAliases().first()
    }

    private fun generateWallet(data:Intent?, secret: String) : Boolean {
        data?.let {
            val pin = PinActivity.getPinFromIntent(it)
            val passphrase = intent.getStringExtra(INTENT_PASSPHRASE)

            pin?.let { that ->
                AccountUtils.generateWallet(applicationContext, secret, passphrase, that)
                return true
            }
        }
        return false
    }
}

