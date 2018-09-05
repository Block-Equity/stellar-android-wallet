package blockeq.com.stellarwallet.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import blockeq.com.stellarwallet.R
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        createWalletButton.setOnClickListener {
            val builder = AlertDialog.Builder(this@LoginActivity)
            val walletLengthList = listOf("Use a 12 word recovery phrase", "Use a 24 word recovery phrase").toTypedArray()
            builder.setTitle("Create Wallet")
                    .setItems(walletLengthList, DialogInterface.OnClickListener { dialog, which ->
                        // The 'which' argument contains the index position
                        // of the selected item

                        val walletLength = if (which == 0) {
                            12
                        } else {
                            24
                        }

                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("walletLength", walletLength)
                        startActivity(intent)
                    })
            val dialog = builder.create()
            dialog.show()
        }

        recoverWalletButton.setOnClickListener {

        }
    }
}
