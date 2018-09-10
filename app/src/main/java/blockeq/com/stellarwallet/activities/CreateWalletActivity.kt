package blockeq.com.stellarwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
import blockeq.com.stellarwallet.activities.PinActivity.Companion.RESULT_CONFIRM_PIN
import blockeq.com.stellarwallet.encryption.CipherWrapper
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.models.PinViewState
import com.soneso.stellarmnemonics.Wallet
import kotlinx.android.synthetic.main.activity_create_wallet.*


class CreateWalletActivity : AppCompatActivity(), View.OnClickListener {

    private var mnemonicString : String? = null
    private var pinViewState : PinViewState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wallet)

        setupUI()
        setOnClickListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIN_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    var pin = data!!.getStringExtra("pin")

                    val keyStoreWrapper = KeyStoreWrapper(applicationContext, "pin_keystore")
                    keyStoreWrapper.createAndroidKeyStoreAsymmetricKey(pin)

                    // Wipe the PIN
                    pin = ""

                    val masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin)
                    val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")

                    val encryptedData = cipherWrapper.encrypt(mnemonicString!!, masterKey?.public)

                    // Wipe the mnemonic
                    mnemonicString = ""

                    WalletApplication.localStore!![getString(R.string.encrypted_mnemonic)] = encryptedData

                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                RESULT_CONFIRM_PIN -> {
                    val pin = data!!.getStringExtra("pin")
                    val intent = Intent(this, PinActivity::class.java)
                    intent.putExtra("pin", pin)
                    startActivityForResult(intent, PIN_REQUEST_CODE)
                    overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                }
                RESULT_CANCELED -> finish()
                else -> finish()
            }
        }
    }

    override fun onClick(v: View?) {
        val item_id = v!!.id
        when (item_id) {
            R.id.confirmButton -> launchPINView()
        }
    }

    //region User Interface
    private fun setupUI() {
        setupActionBar()
        setupMnemonicView()
    }

    private fun setupActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    //TODO: Setup as a reusable view as we will have this in future settings for users
    private fun setupMnemonicView() {

        val mnemonicPhrase = getMnemonic()
        val LAYOUT_MARGINS = 16

        for (i in mnemonicPhrase.indices) {
            val item_view = layoutInflater.inflate(R.layout.item_view_phrase_word, null)

            val numberTextView = item_view!!.findViewById<TextView>(R.id.numberItem)
            val wordTextView = item_view.findViewById<TextView>(R.id.wordItem)

            numberTextView.text = (i + 1).toString()
            wordTextView.text = mnemonicPhrase[i]

            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(LAYOUT_MARGINS, LAYOUT_MARGINS, LAYOUT_MARGINS, LAYOUT_MARGINS)

            mnemonicGridView.addView(item_view, i, layoutParams)
        }
    }

    //endregion

    //region Set onClick interfaces
    private fun setOnClickListeners() {
        confirmButton.setOnClickListener(this)
    }
    //endregion

    //region Helper functions
    private fun getMnemonic(): ArrayList<String> {
        val mnemonic = if (intent.getIntExtra("walletLength", 12) == 12) {
            Wallet.generate12WordMnemonic()
        } else {
            Wallet.generate24WordMnemonic()
        }

        mnemonicString = String(mnemonic)
        val words = String(mnemonic).split(" ".toRegex()).dropLastWhile { it.isEmpty() } as ArrayList
        return words
    }

    private fun launchPINView() {
        /*val intent = Intent(this, PinActivity::class.java)
        intent.putExtra("message", getString(R.string.please_create_a_pin))
        intent.putExtra("need_confirm", true)
        startActivityForResult(intent, PIN_REQUEST_CODE)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay)*/
        PinFlowController.launchPinActivity(this, getPinViewState())
    }

    private fun getPinViewState(): PinViewState {
        return PinViewState(getString(R.string.please_create_a_pin), false, "", mnemonicString!!)
    }
    //endregion
}
