package blockeq.com.stellarwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
import blockeq.com.stellarwallet.helpers.PassphraseDialogHelper
import blockeq.com.stellarwallet.models.MnemonicType
import blockeq.com.stellarwallet.models.PinType
import com.soneso.stellarmnemonics.Wallet
import kotlinx.android.synthetic.main.activity_mnemonic.*
import java.lang.IllegalStateException

class MnemonicActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val MNEMONIC_PHRASE = "MNEMONIC_PHRASE"
        private const val WALLET_LENGTH = "WALLET_LENGTH"

        fun newCreateMnemonicIntent(context: Context, type : MnemonicType): Intent {
            val intent = Intent(context, MnemonicActivity::class.java)
            intent.putExtra(WALLET_LENGTH, type)
            return intent
        }

        fun newDisplayMnemonicIntent(context: Context, mnemonic: String): Intent {
            val intent = Intent(context, MnemonicActivity::class.java)
            intent.putExtra(MnemonicActivity.MNEMONIC_PHRASE, mnemonic)
            return intent
        }
    }

    private var mnemonicString : String = String()
    private var passphrase : String = String()
    private var walletLength : MnemonicType = MnemonicType.WORD_12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mnemonic)

        loadIntent()
        setupUI()
        setOnClickListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIN_REQUEST_CODE) {
            finish()
        }
    }

    //region User Interface
    override fun onClick(v: View) {
        val itemId = v.id
        when (itemId) {
            R.id.confirmButton -> launchPINView(PinType.CREATE, getString(R.string.please_create_a_pin), mnemonicString, passphrase, false)
            R.id.passphraseButton -> {
                val builder = PassphraseDialogHelper(this, object: PassphraseDialogHelper.PassphraseDialogListener {
                    override fun onOK(phrase: String) {
                        passphrase = phrase
                        passphraseButton.text = getString(R.string.passphrase_applied)
                    }
                })
                builder.show()
            }
        }
    }

    private fun setupUI() {
        if (!mnemonicString.isEmpty()) {
        // Show mnemonic UI
            confirmButton.visibility = View.GONE
            passphraseButton.visibility = View.GONE
            if (!WalletApplication.localStore.isRecoveryPhrase) {
                warningPhraseTextView.text = getString(R.string.no_mnemonic_set)
                mnemonicView.visibility = View.GONE
            }
        } else {
            // Create mnemonic UI

            // TODO: Problem linked to setting isRecoveryPhrase before it is confirmed in
            // RecoveryWalletActivity.kt for a secret seed, so that needs to be refactored to
            // after the account is created in PinActivity.kt
            WalletApplication.localStore.isRecoveryPhrase = true
        }
        setupActionBar()
        setupMnemonicView()
    }

    private fun setupActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupMnemonicView() {
        mnemonicView.mnemonic = getMnemonic()
        mnemonicView.loadMnemonic()
    }

    //endregion

    //region Set onClick interfaces
    private fun setOnClickListeners() {
        confirmButton.setOnClickListener(this)
        passphraseButton.setOnClickListener(this)
    }
    //endregion

    //region Helper functions
    private fun getMnemonic(): List<String> {
        if (mnemonicString.isEmpty()) {
            val mnemonic = if(walletLength == MnemonicType.WORD_12) {
                Wallet.generate12WordMnemonic()
            } else {
                Wallet.generate24WordMnemonic()
            }
            mnemonicString = String(mnemonic)
        }

        return getMnemonicList(mnemonicString)
    }

    private fun loadIntent() {
        if (!intent.hasExtra(MNEMONIC_PHRASE) && !intent.hasExtra(WALLET_LENGTH)) {
           throw IllegalStateException("inconsistent intent extras, please use companion methods to create the intent")
        }

        if (intent.hasExtra(MNEMONIC_PHRASE)) {
            mnemonicString = intent.getStringExtra(MNEMONIC_PHRASE)
        }

        if (intent.hasExtra(WALLET_LENGTH)) {
            walletLength = intent.getSerializableExtra(WALLET_LENGTH) as MnemonicType
        }
    }

    private fun getMnemonicList(mnemonic : String) : List<String> {
        return mnemonic.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
    }
    //endregion
}