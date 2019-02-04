package com.blockeq.stellarwallet.models

class AssetAvailabilityImpl(override val postedForTradeAmount: Float,
                                 override val total: Float) : AssetAvailability {
    override val totalAvailable: Float
        get() = total - postedForTradeAmount
}
