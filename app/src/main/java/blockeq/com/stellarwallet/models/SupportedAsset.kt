package blockeq.com.stellarwallet.models

/**
 * Class which provides a model for SupportedAsset
 * @constructor Sets all properties of the SupportedAsset
 * @property id the unique identifier of this asset
 * @property code the asset code
 * @property image the link to icon of asset
 * @property issuer the public address of the issuer of this asset
 * @property createdAt the iso instant time stamp of when the asset was created
 * @property updatedAt the iso instant time stamp of when the asset was updated
 */

data class SupportedAsset (var id: Int, var code: String, var image: String, var issuer: String,
                           var limit: String, var name: String, var createdAt: String,
                           var updatedAt: String)
