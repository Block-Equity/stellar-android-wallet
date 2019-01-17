package com.blockeq.stellarwallet.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.blockeq.stellarwallet.R
import kotlinx.android.synthetic.main.mnemonic_custon_view.view.*

class MnemonicView(context: Context?, attrs: AttributeSet?) : ScrollView(context, attrs) {

    var mnemonic : List<String>? = null

    init {
        inflate(getContext(), R.layout.mnemonic_custon_view, this)
    }

    fun loadMnemonic (passPhrase:String?) {
        val mnemonicPhrase = mnemonic

        if (mnemonicPhrase != null) {
            for (i in mnemonicPhrase.indices) {
                renderView((i + 1).toString(), mnemonicPhrase[i])
            }
            if (passPhrase != null) {
                renderView("passPhrase", passPhrase)
            }
        }
    }

    private fun renderView(indexText:String, phrase : String) {
        val LAYOUT_MARGINS = 16

        @SuppressLint("InflateParams")
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_view_phrase_word, null)

        val numberTextView = itemView.findViewById<TextView>(R.id.numberItem)
        val wordTextView = itemView.findViewById<TextView>(R.id.wordItem)

        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(LAYOUT_MARGINS, LAYOUT_MARGINS, LAYOUT_MARGINS, LAYOUT_MARGINS)

        mnemonicGridView.addView(itemView, layoutParams)

        numberTextView.text = indexText
        wordTextView.text = phrase
    }
}