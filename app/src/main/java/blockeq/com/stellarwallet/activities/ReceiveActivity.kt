package blockeq.com.stellarwallet.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.content_receive.*

class ReceiveActivity : BasePopupActivity() {

    override fun setContent(): Int {
        return R.layout.content_receive
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titleText.text = getString(R.string.receive_title)

        val pubAddress = WalletApplication.localStore.stellarAccountId

        addressEditText.text = pubAddress
        generateQRCode(pubAddress!!, address_qrcode, 500)
        copy_image_button.setOnClickListener { copyAddressToClipBoard(pubAddress)  }
    }

    private fun generateQRCode(data: String, imageView: ImageView, size: Int) {
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)
        imageView.setImageBitmap(bitmap)
    }

    private fun copyAddressToClipBoard(data: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("BlockEQ Address", data)
        clipboard.primaryClip = clip

        Toast.makeText(this, getString(R.string.address_copied_message), Toast.LENGTH_LONG).show()
    }
}
