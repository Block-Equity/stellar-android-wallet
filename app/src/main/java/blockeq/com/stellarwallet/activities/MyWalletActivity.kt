package blockeq.com.stellarwallet.activities

import android.os.Bundle
import blockeq.com.stellarwallet.R
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_base_popup.*
import kotlinx.android.synthetic.main.content_my_wallet.*
import android.widget.Toast
import android.content.Intent



class MyWalletActivity : BasePopupActivity() {

    var availableBalance = "6.02 XLM"
    val ADDRESS_DATA = "ADDRESS"

    override fun setTitle(): Int {
        return R.string.title_activity_my_wallet
    }

    override fun setContent(): Int {
        return R.layout.content_my_wallet
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titleText.text = availableBalance
        camera_image_button.setOnClickListener { initiateScan() }

        next_button.isEnabled = false
        next_button.setOnClickListener {

            val intent = Intent(this, SendActivity::class.java).apply {
                putExtra(ADDRESS_DATA, address_text.text)
            }
            startActivity(intent)

            this.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)

        }
    }

    private fun initiateScan() {
        IntentIntegrator(this).setDesiredBarcodeFormats(IntentIntegrator.QR_CODE).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                address_text.text = result.contents
                next_button.isEnabled = true
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}
