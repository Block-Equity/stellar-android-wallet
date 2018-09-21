package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.helpers.Constants.Companion.STELLAR_ADDRESS_LENGTH
import blockeq.com.stellarwallet.utils.StringFormat
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_base_popup.*
import kotlinx.android.synthetic.main.content_send_to_address.*
import kotlinx.android.synthetic.main.view_custom_selector.*


class SendToAddressActivity : BasePopupActivity() {

    val ADDRESS_DATA = "ADDRESS"

    override fun setTitle(): Int {
        return R.string.title_activity_my_wallet
    }

    override fun setContent(): Int {
        return R.layout.content_send_to_address
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadBalance()

        camera_image_button.setOnClickListener { initiateScan() }

        next_button.setOnClickListener {
            val address = addressEditText.text.toString()
            if (address.length == STELLAR_ADDRESS_LENGTH) {
                val intent = Intent(this, SendActivity::class.java).apply {
                    putExtra(ADDRESS_DATA, addressEditText.text)
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

    private fun loadBalance() {
        WalletApplication.localStore!!.balances?.forEach {
            if (it.assetType == Constants.LUMENS_ASSET_TYPE) {
                //TODO: When switching assets, get the right balance for asset
                titleText.text = StringFormat.truncateDecimalPlaces(it.balance) + " XLM"
            }
        }
    }
}
