package com.blockeq.stellarwallet.interfaces;

import android.view.View;
import android.widget.AdapterView;

public abstract class OnItemSelected implements AdapterView.OnItemSelectedListener {

    @Override
    public abstract void onItemSelected(AdapterView<?> parent, View view, int position, long id);

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // empty implementation
    }
}
