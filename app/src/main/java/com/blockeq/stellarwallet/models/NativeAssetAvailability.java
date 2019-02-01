package com.blockeq.stellarwallet.models;

public interface NativeAssetAvailability extends AssetAvailability {
    /**
     *    Native ASSET
     *    (base amount 2) * 0.5
     *    (default is one) additional signers * 1
     *    trustlines 2 * 0.5
     *    open offer * 0.5
     *
     *
     *    posted for trade X
     */
    Long getBaseAmount();
    Long getAdditionalSigners();
    Long getTrustline();
    Long getOpenOffers();
}
