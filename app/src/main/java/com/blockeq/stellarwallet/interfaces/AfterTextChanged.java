package com.blockeq.stellarwallet.interfaces;

import android.text.Editable;
import android.text.TextWatcher;

public abstract class AfterTextChanged implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // empty implementation
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // empty implementation
    }

    @Override
    public abstract void afterTextChanged(Editable s);
}
