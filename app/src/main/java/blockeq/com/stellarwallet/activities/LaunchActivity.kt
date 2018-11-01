package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import blockeq.com.stellarwallet.R
import kotlinx.android.synthetic.main.activity_login.*

class LaunchActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinActivity.PIN_REQUEST_CODE) {
            finish()
        }
    }

    //region User Interface
    private fun setupUI() {

        createWalletButton.setOnClickListener {
            showCreateDialog()
        }

        recoverWalletButton.setOnClickListener {
            showRecoverDialog()
        }
    }

    private fun showCreateDialog() {
        val builder = AlertDialog.Builder(this@LaunchActivity)
        val walletLengthList = listOf(getString(R.string.create_word_option_1), getString(R.string.create_word_option_2)).toTypedArray()
        builder.setTitle(getString(R.string.create_wallet))
                .setItems(walletLengthList) { _, which ->
                    // The 'which' argument contains the index position
                    // of the selected item

                    val walletLength = if (which == 0) {
                        12
                    } else {
                        24
                    }

                    val intent = Intent(this, ShowMnemonicActivity::class.java)
                    intent.putExtra("walletLength", walletLength)
                    startActivity(intent)
                }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showRecoverDialog() {
        val builder = AlertDialog.Builder(this@LaunchActivity)
        val walletLengthList = listOf(getString(R.string.recover_from_phrase), getString(R.string.recover_from_seed)).toTypedArray()
        builder.setTitle(getString(R.string.recover_wallet))
                .setItems(walletLengthList) { _, which ->
                    // The 'which' argument contains the index position
                    // of the selected item

                    val isPhraseRecovery = (which == 0)

                    val intent = Intent(this, RecoverWalletActivity::class.java)
                    intent.putExtra("isPhraseRecovery", isPhraseRecovery)
                    startActivity(intent)
                }
        val dialog = builder.create()
        dialog.show()
    }

    //endregion
}
