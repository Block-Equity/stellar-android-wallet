package blockeq.com.stellarwallet.activities

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.models.SupportedAsset
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_login.*

class LaunchActivity : BaseActivity() {
    private lateinit var newAppDialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupUI()

        checkNewApp()
    }


    private fun checkNewApp() {
        val builder = AlertDialog.Builder(this@LaunchActivity)
        builder.setTitle("The Beta program has ended")
        builder.setMessage("The current app is not longer maintained, please install the new app")
        builder.setPositiveButton("Download") { _, _ ->
            val appPackageName = packageName // getPackageName() from Context or Activity object
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")));
            } catch (e : ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")));
            }
        }
        builder.setNegativeButton("Later", null)
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(Request.Method.GET, "https://play.google.com/store/apps/details?id=blockeq.com.stellarwallet", Response.Listener<String> {
            if (!isFinishing) {
                newAppDialog.show()
            }
        }, null)

        newAppDialog = builder.create()
        queue.add(request)
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

    override fun onDestroy() {
        super.onDestroy()
        if (newAppDialog.isShowing) {
            newAppDialog.dismiss()
        }
    }

    //endregion
}
