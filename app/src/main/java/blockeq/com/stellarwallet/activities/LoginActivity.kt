package blockeq.com.stellarwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.encryption.CipherWrapper
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val data = sharedPref.getString(getString(R.string.encrypted_mnemonic), "")

        text.text = data

        test.setOnClickListener {
            val keyStoreWrapper = KeyStoreWrapper(applicationContext, "pin_keystore")

            val masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair("1234")
            if (masterKey == null) {
                text.text = "Failed to decrypt!"
            } else {
                val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")

                if (!data.isNullOrEmpty()) {
                    val decryptedData = cipherWrapper.decrypt(data, masterKey?.private)
                    text.text = decryptedData
                }
            }
        }

        createWalletButton.setOnClickListener {
            val builder = AlertDialog.Builder(this@LoginActivity)
            val walletLengthList = listOf("Use a 12 word recovery phrase", "Use a 24 word recovery phrase").toTypedArray()
            builder.setTitle("Create Wallet")
                    .setItems(walletLengthList) { dialog, which ->
                        // The 'which' argument contains the index position
                        // of the selected item

                        val walletLength = if (which == 0) {
                            12
                        } else {
                            24
                        }

                        val intent = Intent(this, CreateWalletActivity::class.java)
                        intent.putExtra("walletLength", walletLength)
                        startActivity(intent)
                    }
            val dialog = builder.create()
            dialog.show()
        }

        recoverWalletButton.setOnClickListener {
            startActivity(Intent(this, RecoverWalletActivity::class.java))
        }
    }
}
