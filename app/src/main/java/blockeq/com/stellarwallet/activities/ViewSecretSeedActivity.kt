package blockeq.com.stellarwallet.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import blockeq.com.stellarwallet.R
import kotlinx.android.synthetic.main.activity_view_secret_seed.*

class ViewSecretSeedActivity : AppCompatActivity() {

    companion object {
        const val SECRET_SEED = "kSecretSeed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_secret_seed)

        val secretSeed = intent.extras.getString(SECRET_SEED)

        secretSeedTextView.text = secretSeed
        copy_image_button.setOnClickListener { copyAddressToClipBoard(secretSeed)  }
    }

    private fun copyAddressToClipBoard(data: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("BlockEQ Address", data)
        clipboard.primaryClip = clip

        Toast.makeText(this, getString(R.string.address_copied_message), Toast.LENGTH_LONG).show()
    }
}
