package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants.Companion.STELLAR_ADDRESS_LENGTH
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.content_enter_address.*

class EnterAddressActivity : BasePopupActivity(), View.OnClickListener {

    val ADDRESS_DATA = "ADDRESS"

    override fun setContent(): Int {
        return R.layout.content_enter_address
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
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
                nextButton.isEnabled = true
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //region User Interface
    private fun setupUI() {

        titleText.text = WalletApplication.userSession.getFormattedCurrentAvailableBalance(applicationContext)

        cameraImageButton.setOnClickListener(this)
        nextButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.nextButton -> {
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
                    addressEditText.startAnimation(shakeAnimation)
                }
            }
            R.id.cameraImageButton -> {
                initiateScan()
            }
        }
    }

    //endregion
}
