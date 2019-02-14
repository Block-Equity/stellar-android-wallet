package com.blockeq.stellarwallet.interfaces

import android.text.Editable
import android.text.TextWatcher

abstract class AfterTextChanged : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // empty implementation
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // empty implementation
    }

    abstract override fun afterTextChanged(editable: Editable)
}
