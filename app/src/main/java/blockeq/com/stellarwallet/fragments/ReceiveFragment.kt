package blockeq.com.stellarwallet.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import blockeq.com.stellarwallet.R
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.fragment_receive.*


class ReceiveFragment : BaseFragment() {

    private var address = "GD5HQAPT5KOIKMY35QREYSS34BC3O4FFNTE2DTXUZI4YSJSUXP5QRQS3"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_receive, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        address_text.text = address

        generateQRCode(address, address_qrcode, 500)

        copy_image_button.setOnClickListener { copyDataToClipBoard(address)  }

    }

    private fun generateQRCode(data: String, imageView: ImageView, size: Int) {
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)
        imageView.setImageBitmap(bitmap)
    }

    private fun copyDataToClipBoard(data: String) {
        val clipboard = this.context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("BlockEq Address", data)
        clipboard.primaryClip = clip;

        Toast.makeText(this.context, getString(R.string.address_copied_message), Toast.LENGTH_LONG).show()

    }

    companion object {
        fun newInstance(): ReceiveFragment = ReceiveFragment()
    }
}