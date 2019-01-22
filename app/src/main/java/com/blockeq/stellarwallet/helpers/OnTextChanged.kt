package com.blockeq.stellarwallet.helpers

import android.text.Editable
import android.text.TextWatcher

abstract class OnTextChanged : TextWatcher {
    override fun afterTextChanged(editable: Editable) {}

    override fun beforeTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}

    abstract override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int)
}