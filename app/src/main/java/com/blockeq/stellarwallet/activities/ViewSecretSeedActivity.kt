package com.blockeq.stellarwallet.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.blockeq.stellarwallet.R
import kotlinx.android.synthetic.main.activity_view_secret_seed.*

class ViewSecretSeedActivity : BaseActivity() {

    companion object {
        private const val ARG_SECRET_SEED = "ARG_SECRET_SEED"
        fun newInstance(context: Context, secretSeed : String) : Intent {
            val intent = Intent(context, ViewSecretSeedActivity::class.java)
            intent.putExtra(ARG_SECRET_SEED, secretSeed)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_secret_seed)

         intent?.extras?.let{
            val secretSeed = it.getString(ARG_SECRET_SEED)
            secretSeed?.let {
                secretSeedTextView.text = secretSeed
                copyImageButton.setOnClickListener { copyAddressToClipBoard(secretSeed)  }
            }
        }
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun copyAddressToClipBoard(data: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("BlockEQ Address", data)
        clipboard.primaryClip = clip

        Toast.makeText(this, getString(R.string.address_copied_message), Toast.LENGTH_LONG).show()
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
