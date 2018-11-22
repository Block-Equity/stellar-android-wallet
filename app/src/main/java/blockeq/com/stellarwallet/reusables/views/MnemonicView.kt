package blockeq.com.stellarwallet.reusables.views

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import blockeq.com.stellarwallet.R
import kotlinx.android.synthetic.main.mnemonic_custon_view.view.*

class MnemonicView(context: Context?, attrs: AttributeSet?) : ScrollView(context, attrs) {

    var mnemonic : List<String>? = null

    init {
        inflate(getContext(), R.layout.mnemonic_custon_view, this)
    }

    fun loadMnemonic () {
        val mnemonicPhrase = mnemonic
        val LAYOUT_MARGINS = 16

        if (mnemonicPhrase != null) {
            for (i in mnemonicPhrase.indices) {
                val item_view = (context as Activity).layoutInflater.inflate(R.layout.item_view_phrase_word, null)

                val numberTextView = item_view!!.findViewById<TextView>(R.id.numberItem)
                val wordTextView = item_view.findViewById<TextView>(R.id.wordItem)

                numberTextView.text = (i + 1).toString()
                wordTextView.text = mnemonicPhrase[i]

                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(LAYOUT_MARGINS, LAYOUT_MARGINS, LAYOUT_MARGINS, LAYOUT_MARGINS)

                mnemonicGridView.addView(item_view, i, layoutParams)
            }
        }
    }
}