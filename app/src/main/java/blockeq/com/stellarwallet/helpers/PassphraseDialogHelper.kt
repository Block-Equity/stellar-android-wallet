package blockeq.com.stellarwallet.helpers

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.EditText
import blockeq.com.stellarwallet.R

class PassphraseDialogHelper(activity: Activity, listener: PassphraseDialogListener) : AlertDialog.Builder(activity) {

    interface PassphraseDialogListener {
        fun onOK(phrase: String)
    }

    init {
        setTitle("Enter a passphrase")
        setMessage("Passphrase along with mnemonic phrase will be used to derive your address.")

        val viewInflated = LayoutInflater.from(activity).inflate(R.layout.passphrase_dialog, activity.findViewById(android.R.id.content), false)
        val input = viewInflated.findViewById(R.id.passphraseEditText) as EditText
        setView(viewInflated)

        setPositiveButton(context.getString(R.string.ok)) { _, _ -> listener.onOK(input.text.toString()) }
        setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
    }
}
