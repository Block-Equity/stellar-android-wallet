package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.*
import com.blockeq.stellarwallet.utils.GlobalGraphHelper
import com.soneso.stellarmnemonics.mnemonic.WordList
import kotlinx.android.synthetic.main.activity_recover_wallet.*

class RecoverWalletActivity : BaseActivity() {
    private var isRecoveryPhrase = true
    private var passphrase : String? = null
    private lateinit var recoveryString : String
    private val RESTORE_REQUEST = 0x01

    companion object {
        private var INTENT_ARG_RECOVERY = "INTENT_ARG_RECOVERY"
        fun newInstance(context: Context, isRecovery: Boolean) : Intent {
            val intent = Intent(context, RecoverWalletActivity::class.java)
            intent.putExtra(INTENT_ARG_RECOVERY, isRecovery)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_wallet)

        isRecoveryPhrase = intent.getBooleanExtra(INTENT_ARG_RECOVERY, true)
        setupUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESTORE_REQUEST) {
            if (resultCode ==  Activity.RESULT_OK) {
                GlobalGraphHelper.launchWallet(this)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            if (item.itemId == android.R.id.home) {
                finish()
                return true
            }
        }
        return false
    }

    //region User Interface
    private fun setupUI() {
        setupToolbar()

        if (isRecoveryPhrase) {
            secretKeyEditText.visibility = View.GONE
            phraseEditText.visibility = View.VISIBLE
            invalidPhraseTextView.text = getString(R.string.invalid_input_for_phrase, Bip0039.values().joinToString(",") { it.numberOfWords.toString()})
        } else {
            secretKeyEditText.visibility = View.VISIBLE
            phraseEditText.visibility = View.GONE
            invalidPhraseTextView.text = getString(R.string.invalid_input_for_secret)
            passphraseButton.visibility = View.GONE
        }

        bottomButton.setOnClickListener {
            try {
                recoveryString = StellarRecoveryString(getMnemonicString(), isRecoveryPhrase, passphrase).getString()
                WalletApplication.wallet.setIsRecoveryPhrase(isRecoveryPhrase)
                startActivityForResult(WalletManagerActivity.restore(it.context, recoveryString, passphrase), RESTORE_REQUEST)
            } catch (e: Exception) {
                showErrorMessage(e.message)
            }
        }

        passphraseButton.setOnClickListener {
            val builder = PassphraseDialogHelper(this, object: PassphraseDialogHelper.PassphraseDialogListener {
                override fun onOK(phrase: String) {
                    passphrase = phrase
                    passphraseButton.text = getString(R.string.passphrase_applied)
                }
            })
            builder.show()
        }

        phraseEditText.addTextChangedListener(object : OnTextChanged() {
            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                highlightMnemonic()
            }
        })

        secretKeyEditText.addTextChangedListener(object : OnTextChanged() {
            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                highlightSeed()
            }
        })
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.recoverToolbar))
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = if (isRecoveryPhrase) {
                getString(R.string.enter_phrase)
            } else {
                getString(R.string.enter_secret_key)
            }
        }
    }

    private fun showErrorMessage(message : String?) {
        if (message != null) {
            invalidPhraseTextView.text = message
        }
        invalidPhraseTextView.visibility = View.VISIBLE
    }

    //endregion

    //region Helper functions
    private fun highlightMnemonic() {
        val wordListBIP39 = WordList.ENGLISH.words.toHashSet()

        val tokens = phraseEditText.text.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
        var startIndex = 0
        var endIndex = 0

        for (word in tokens) {

            // Color the last word
            endIndex += word.length

            val colorText = if (!wordListBIP39.contains(word)) {
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.apricot))
            } else {
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.regularTextColor))
            }

            phraseEditText.text.setSpan(colorText, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            startIndex += word.length + 1
            ++endIndex
        }
    }

    private fun highlightSeed() {
        val seedText = secretKeyEditText.text
        val colorText = if (seedText.length != Constants.STELLAR_ADDRESS_LENGTH || seedText[0] != 'S') {
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.apricot))
        } else {
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.regularTextColor))
        }

        secretKeyEditText.text.setSpan(colorText, 0, seedText.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    private fun getMnemonicString() : String {
        return if (isRecoveryPhrase) {
            phraseEditText.text.toString()
        } else {
            secretKeyEditText.text.toString()
        }
    }
    //endregion
}
