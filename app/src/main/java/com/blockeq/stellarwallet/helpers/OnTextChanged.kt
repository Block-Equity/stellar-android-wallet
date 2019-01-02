package com.blockeq.stellarwallet.helpers

import android.text.Editable
import android.text.TextWatcher

abstract class OnTextChanged : TextWatcher {
    override fun afterTextChanged(p0: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    abstract override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
}