package com.blockeq.stellarwallet.models

import org.stellar.sdk.Asset

data class NativeAssetAvailabilityImpl(override val baseCount: Int,
                                       override val baseAmount: Float,
                                       override val additionalSignersCount: Int,
                                       override val additionalSignersAmount: Float,
                                       override val trustLinesCount: Int,
                                       override val trustLinesAmount: Float,
                                       override val openOffersCount: Int,
                                       override val openOffersAmount: Float,
                                       override val postedForTradeAmount: Float,
                                       override val total: Float, override val assetCode: String, override val assetIssuer: String?, override val asset: Asset) : NativeAssetAvailability {
    override val totalAvailable: Float
        get() = total - baseAmount - additionalSignersAmount - trustLinesAmount - openOffersAmount - postedForTradeAmount
}
