package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.encryption.CipherWrapper
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.helpers.LocalStore.Companion.KEY_ENCRYPTED_PHRASE
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinActivity.PIN_REQUEST_CODE) {
            when (resultCode) {
                PinActivity.RESULT_FAIL -> {
                    // Wipe the user data
                    
                }
                else -> finish()
            }
        }
    }

    //region User Interface
    override fun setupUI() {
        val data = WalletApplication.localStore!![KEY_ENCRYPTED_PHRASE]

        if (data != null && !data.isEmpty()) {
            launchPINView(data)
        }

        // TODO: For encryption testing purposes
        text.text = data

        test.setOnClickListener {
            val keyStoreWrapper = KeyStoreWrapper(applicationContext)

            val masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair("1234")
            if (masterKey == null) {
                text.text = "Failed to decrypt!"
            } else {
                val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")

                if (data != null && !data.isEmpty()) {
                    val decryptedData = cipherWrapper.decrypt(data, masterKey.private)
                    text.text = decryptedData
                }
            }
        }

        createWalletButton.setOnClickListener {
            showDialog()
        }

        recoverWalletButton.setOnClickListener {
            startActivity(Intent(this, RecoverWalletActivity::class.java))
        }
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(this@LoginActivity)
        val walletLengthList = listOf("Use a 12 word recovery phrase", "Use a 24 word recovery phrase").toTypedArray()
        builder.setTitle("Create Wallet")
                .setItems(walletLengthList) { _, which ->
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


    //endregion

    //region Helper functions
    private fun launchPINView(mnemonic : String) {
        val pinViewState = PinViewState(PinType.CHECK, "", "", mnemonic)
        PinFlowController.launchPinActivity(this, pinViewState)
    }
    //endregion
}
