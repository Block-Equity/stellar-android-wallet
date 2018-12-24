package com.blockeq.stellarwallet.utils;

import android.support.annotation.Nullable;

import com.blockeq.stellarwallet.helpers.LocalStore;

public class Wallet {
    private LocalStore localStore = null;

    public Wallet(LocalStore localStore) {
        this.localStore = localStore;
    }

    public void createWallet(String pin, String secret, @Nullable String passphrase){

    }

    public String getMnemonic() {
        return "";
    }

    public String getSecretKey() {
        return "";
    }


}
