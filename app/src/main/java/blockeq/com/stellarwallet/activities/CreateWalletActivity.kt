package blockeq.com.stellarwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
import blockeq.com.stellarwallet.activities.PinActivity.Companion.RESULT_CONFIRM_PIN
import blockeq.com.stellarwallet.encryption.CipherWrapper
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper
import com.soneso.stellarmnemonics.Wallet
import kotlinx.android.synthetic.main.activity_create_wallet.*


class CreateWalletActivity : AppCompatActivity() {

    private var mnemonicString : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wallet)

        setSupportActionBar(findViewById(R.id.createToolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val mnemonic = if (intent.getIntExtra("walletLength", 12) == 12) {
            Wallet.generate12WordMnemonic()
        } else {
            Wallet.generate24WordMnemonic()
        }

        mnemonicString = String(mnemonic)
        val words = String(mnemonic).split(" ".toRegex()).dropLastWhile { it.isEmpty() } as ArrayList

        for (i in words.indices) {
            val item_view = layoutInflater.inflate(R.layout.item_view_phrase_word, null)

            val numberTextView = item_view!!.findViewById<TextView>(R.id.numberItem)
            val wordTextView = item_view.findViewById<TextView>(R.id.wordItem)

            numberTextView.text = (i + 1).toString()
            wordTextView.text = words[i]

            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(16, 16, 16, 16)

            mnemonicGridView.addView(item_view, i,layoutParams)
        }


        confirmButton.setOnClickListener {
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("message", getString(R.string.please_create_a_pin))
            intent.putExtra("need_confirm", true)
            startActivityForResult(intent, PIN_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
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

                    val sharedPref = getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
                    with (sharedPref.edit()) {
                        putString(getString(R.string.encrypted_mnemonic), encryptedData)
                        apply()
                    }

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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            if (item.itemId == android.R.id.home) {
                finish()
                return true
            }
        }
        return false
    }
}
