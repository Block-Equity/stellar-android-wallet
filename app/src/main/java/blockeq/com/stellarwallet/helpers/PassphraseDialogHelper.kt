package blockeq.com.stellarwallet.helpers

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.EditText
import blockeq.com.stellarwallet.R

class PassphraseDialogHelper(private val activity: Activity, private val listener: PassphraseDialogListener) : AlertDialog.Builder(activity) {

    private var isConfirm = false
    private var inputEditText : EditText? = null
    private var originalPhrase = ""

    interface PassphraseDialogListener {
        fun onOK(phrase: String)
    }

    init {
        setTitle(context.getString(R.string.passphrase_dialog_title))
        setMessage(context.getString(R.string.passphrase_dialog_info))

        val viewInflated = LayoutInflater.from(activity).inflate(R.layout.passphrase_dialog, activity.findViewById(android.R.id.content), false)
        inputEditText = viewInflated.findViewById(R.id.passphraseEditText) as EditText
        setView(viewInflated)

        setPositiveButton(context.getString(R.string.ok)) { _, _ ->

            listener.onOK(inputEditText!!.text.toString())
        }
        setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
    }

    override fun show(): AlertDialog {
        val dialog = super.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val phrase = inputEditText!!.text.toString()
            if (isConfirm) {
                if (originalPhrase == phrase) {
                    listener.onOK(phrase)
                    dialog.cancel()
                } else {
                    dialog.setMessage(context.getString(R.string.passphrase_dialog_error))
                    val shakeAnimation = AnimationUtils.loadAnimation(activity, R.anim.shake)
                    inputEditText!!.startAnimation(shakeAnimation)
                    inputEditText!!.setText("")
                }
            } else {
                originalPhrase = phrase
                inputEditText!!.setText("")
                dialog.setMessage(context.getString(R.string.passphrase_dialog_confirm))
                isConfirm = true
            }
        }
        return dialog

    }
}
