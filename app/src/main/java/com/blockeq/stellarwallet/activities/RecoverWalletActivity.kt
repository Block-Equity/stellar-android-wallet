package com.blockeq.stellarwallet.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
import com.blockeq.stellarwallet.helpers.Bip0039
import com.blockeq.stellarwallet.helpers.PassphraseDialogHelper
import com.blockeq.stellarwallet.helpers.StellarRecoveryString
import com.blockeq.stellarwallet.models.PinType
import kotlinx.android.synthetic.main.activity_recover_wallet.*
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan


class RecoverWalletActivity : BaseActivity() {

    private var isRecoveryPhrase = true
    private var passphrase : String? = null
    private var flag = false
    var cursorPosStart = 0
    var cursorPosEnd = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_wallet)

        isRecoveryPhrase = intent.getBooleanExtra("isPhraseRecovery", true)
        setupUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIN_REQUEST_CODE) {
            finish()
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

        nextButton.setOnClickListener {
            try {
                WalletApplication.localStore.isRecoveryPhrase = isRecoveryPhrase

                val recoveryString = StellarRecoveryString(getMnemonicString(), isRecoveryPhrase, passphrase).getString()

                launchPINView(PinType.CREATE,
                        getString(R.string.please_create_a_pin),
                        recoveryString,
                        passphrase,
                        false)
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

        phraseEditText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(spannable: Editable) {
                // Tokenize, get last word. check to see if it is in the word list
                // color the string properly

            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (flag) {
                    flag = false
                    return
                } else {
                    val spannable = SpannableString(phraseEditText.text)
                    val hashset = hashSetOf<String>("hello")

                    val tokens = spannable.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    if (tokens.isNotEmpty()) {
                        val word = tokens.last()

                        flag = true
                        // Color the last word
                        val startIndex = spannable.length - word.length
                        val endIndex = spannable.length

                        cursorPosStart = phraseEditText.selectionStart
                        cursorPosEnd = phraseEditText.selectionEnd

                        val colorText = if (!hashset.contains(word)) {
                            ForegroundColorSpan(Color.RED)
                        } else {
                            ForegroundColorSpan(Color.BLACK)
                        }

                        spannable.setSpan(colorText, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                        phraseEditText.setText(spannable, TextView.BufferType.SPANNABLE)
                        phraseEditText.setSelection(cursorPosStart, cursorPosEnd)

                    }
                }
            }
        })
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.recoverToolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportActionBar!!.title = if (isRecoveryPhrase) {
            getString(R.string.enter_phrase)
        } else {
            getString(R.string.enter_secret_key)
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
    private fun getMnemonicString() : String {
        return if (isRecoveryPhrase) {
            phraseEditText.text.toString()
        } else {
            secretKeyEditText.text.toString()
        }
    }
    //endregion
}