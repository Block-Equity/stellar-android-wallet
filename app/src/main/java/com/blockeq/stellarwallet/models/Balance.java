package com.blockeq.stellarwallet.models;

import org.stellar.sdk.responses.AccountResponse;

public class Balance {
    double baseReserve = 0.5;
    double baseFee = 0.00001;

    AccountResponse accountResponse;
    public Balance(AccountResponse accountResponse) {
        this.accountResponse = accountResponse;

        double signersValue = (accountResponse.getSigners().length-1);
        double baseAmount = 2;
        double trustlines = (accountResponse.getBalances().length -1) * baseAmount;

    }

    NativeAssetAvailability getNativeBalance(){

    }

    AssetAvailability getAssetAvailability(String assetCode){

    }
}
