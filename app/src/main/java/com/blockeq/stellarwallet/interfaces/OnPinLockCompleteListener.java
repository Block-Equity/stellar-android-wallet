package com.blockeq.stellarwallet.interfaces;

import com.andrognito.pinlockview.PinLockListener;

public abstract class OnPinLockCompleteListener implements PinLockListener {
    @Override
    public void onEmpty() {
        // empty
    }

    @Override
    public void onPinChange(int pinLength, String intermediatePin) {
        // empty
    }
}
