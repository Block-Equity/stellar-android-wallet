package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
import blockeq.com.stellarwallet.models.PinType
import com.soneso.stellarmnemonics.Wallet
import kotlinx.android.synthetic.main.activity_show_mnemonic.*


class ShowMnemonicActivity : BaseActivity() {

    companion object {
        const val INTENT_DISPLAY_PHRASE = "INTENT_DISPLAY_PHRASE"
        const val DECRYPTED_PHRASE = "DECRYPTED_PHRASE"
    }

    private var mnemonicString : String? = null
    private var isDisplayPhraseOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_mnemonic)

        loadIntent()
        setupUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIN_REQUEST_CODE) {
            finish()
        }
    }

    private fun setupUI() {
        if (isDisplayPhraseOnly) {
            confirmButton.visibility = View.GONE
            if (!WalletApplication.localStore.isRecoveryPhrase) {
                warningPhraseTextView.text = getString(R.string.no_mnemonic_set)
                mnemonicView.visibility = View.GONE
            }
        }
        setupActionBar()
        setupMnemonicView()
        confirmButton.setOnClickListener {
            launchPINView(PinType.CREATE, getString(R.string.please_create_a_pin), mnemonicString!!, false)
        }
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

    //region Helper functions
    private fun getMnemonic(): ArrayList<String> {
        if (isDisplayPhraseOnly) {
            return ArrayList(mnemonicString!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() })
        } else {
            val mnemonic = if (intent.getIntExtra("walletLength", 12) == 12) {
                Wallet.generate12WordMnemonic()
            } else {
                Wallet.generate24WordMnemonic()
            }

            mnemonicString = String(mnemonic)
            return String(mnemonic).split(" ".toRegex()).dropLastWhile { it.isEmpty() } as ArrayList
        }
    }

    private fun loadIntent() {
        isDisplayPhraseOnly = intent.getBooleanExtra(INTENT_DISPLAY_PHRASE, false)
        mnemonicString = intent.getStringExtra(DECRYPTED_PHRASE)
    }
    //endregion
}
