package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.helpers.Constants.Companion.STELLAR_ADDRESS_LENGTH
import blockeq.com.stellarwallet.services.networking.Horizon.Companion.getBalance
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.content_send_to_address.*


class SendToAddressActivity : BasePopupActivity() {

    val ADDRESS_DATA = "ADDRESS"

    override fun setContent(): Int {
        return R.layout.content_send_to_address
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        titleText.text = "Available: " + getBalance() + " XLM"

        camera_image_button.setOnClickListener { initiateScan() }

        next_button.setOnClickListener {
            val address = addressEditText.text.toString()
            if (address.length == STELLAR_ADDRESS_LENGTH) {
                val intent = Intent(this, SendActivity::class.java).apply {
                    putExtra(ADDRESS_DATA, address)
                }
                startActivity(intent)

                this.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            } else {
                // Shake animation on the text
                val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                addressLayout.startAnimation(shakeAnimation)
            }
        }
    }

    private fun initiateScan() {
        IntentIntegrator(this).setBeepEnabled(false).setDesiredBarcodeFormats(IntentIntegrator.QR_CODE).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show()
            } else {
                addressEditText.setText(result.contents)
                next_button.isEnabled = true
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
