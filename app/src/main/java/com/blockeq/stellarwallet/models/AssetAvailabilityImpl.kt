package com.blockeq.stellarwallet.models

import org.stellar.sdk.Asset

class AssetAvailabilityImpl(override val assetCode: String, override val assetIssuer: String, override val postedForTradeAmount: Float,
                            override val total: Float, override val asset: Asset) : AssetAvailability {
    override val totalAvailable: Float
        get() = total - postedForTradeAmount
}
