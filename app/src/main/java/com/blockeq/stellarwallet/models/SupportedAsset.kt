package com.blockeq.stellarwallet.models

import org.stellar.sdk.Asset

/**
 * Class which provides a model for SupportedAsset
 * @constructor Sets all properties of the SupportedAsset
 * @property id the unique identifier of this asset
 * @property code the asset code
 * @property image the link to icon of asset
 * @property issuer the public address of the issuer of this asset
 * @property createdAt the iso instant time stamp of when the asset was created
 * @property updatedAt the iso instant time stamp of when the asset was updated
 * @property amount the amount of balance of asset
 * @property type either added or not added asset
 */

enum class SupportedAssetType {
    ADDED, NOT_ADDED
}

data class SupportedAsset (var id: Int, var code: String, var image: String, var issuer: String,
                           var limit: String, var name: String, var createdAt: String,
                           var updatedAt: String, var amount: String?,
                           var type: SupportedAssetType? = SupportedAssetType.NOT_ADDED,
                           var asset: Asset?)

