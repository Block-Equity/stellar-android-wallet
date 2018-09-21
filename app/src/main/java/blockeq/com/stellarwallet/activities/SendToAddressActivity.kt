package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.utils.StringFormat
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_base_popup.*
import kotlinx.android.synthetic.main.content_send_to_address.*


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
        IntentIntegrator(this).setBeepEnabled(false).setDesiredBarcodeFormats(IntentIntegrator.QR_CODE).initiateScan()
    }

    private fun loadBalance() {
        WalletApplication.localStore!!.balances?.forEach {
            if (it.assetType == Constants.LUMENS_ASSET_TYPE) {
                titleText.text = StringFormat.truncateDecimalPlaces(it.balance)
            }
        }
    }
}
