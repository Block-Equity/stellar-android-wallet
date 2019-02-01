package com.blockeq.stellarwallet.models;

public interface NativeAssetAvailability extends AssetAvailability {
    /**
     *    Native ASSET
     *    (base amount 2) * 0.5
     *    (default is one) additional signers * 1
     *    trustlines 2 * 0.5
     *    open offer * 0.5
     *
     * https://github.com/Block-Equity/stellar-ios-wallet/blob/ee2414061e309e2b97c4883541ca1cd335994487/StellarHub/Objects/StellarAccount.swift#L253
     *    posted for trade X
     */
    Long getBaseAmount();
    Long getAdditionalSigners();
    Long getTrustline();
    Long getOpenOffers();
}
