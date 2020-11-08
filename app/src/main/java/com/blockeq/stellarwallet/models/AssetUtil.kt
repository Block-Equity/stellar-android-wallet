package com.blockeq.stellarwallet.models

import org.stellar.sdk.Asset
import org.stellar.sdk.AssetTypeCreditAlphaNum12
import org.stellar.sdk.AssetTypeCreditAlphaNum4
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.xdr.AssetType

class AssetUtil {
    companion object {
        val NATIVE_ASSET_CODE = "XLM"

        fun toAssetFrom(dataAsset: DataAsset) : Asset {
            val buying : Asset = if (dataAsset.type == "native") {
                AssetTypeNative()
            } else {
                Asset.create(dataAsset.type, dataAsset.code, dataAsset.issuer)
            }
            return buying
        }

        fun toDataAssetFrom(selection: SelectionModel): DataAsset? {
            val asset = selection.asset
            if (asset != null) {
                return toDataAssetFrom(asset)
            }
            return null
        }

        private fun toDataAssetFrom(asset: Asset): DataAsset? {
            var dataAsset: DataAsset? = null
            val assetType = asset.toXdr().discriminant
            if (assetType != null) {
                dataAsset = when (assetType) {
                    AssetType.ASSET_TYPE_CREDIT_ALPHANUM12 -> {
                        val concreteAsset = asset as AssetTypeCreditAlphaNum12
                        DataAsset(concreteAsset.type, concreteAsset.code, concreteAsset.issuer)
                    }
                    AssetType.ASSET_TYPE_CREDIT_ALPHANUM4 -> {
                        val concreteAsset = asset as AssetTypeCreditAlphaNum4
                        DataAsset(concreteAsset.type, concreteAsset.code, concreteAsset.issuer)
                    }
                    AssetType.ASSET_TYPE_NATIVE -> {
                        val concreteAsset = asset as AssetTypeNative
                        DataAsset(concreteAsset.type, NATIVE_ASSET_CODE, "null")
                    }
                }
            }
            return dataAsset
        }

        fun getCode(asset: Asset): String? {
            var code: String? = null
            val assetType = asset.toXdr().discriminant
            if (assetType != null) {
                code = when (assetType) {
                    AssetType.ASSET_TYPE_CREDIT_ALPHANUM12 -> {
                        val concreteAsset = asset as AssetTypeCreditAlphaNum12
                        concreteAsset.code
                    }
                    AssetType.ASSET_TYPE_CREDIT_ALPHANUM4 -> {
                        val concreteAsset = asset as AssetTypeCreditAlphaNum4
                        concreteAsset.code
                    }
                    AssetType.ASSET_TYPE_NATIVE -> {
                        NATIVE_ASSET_CODE
                    }
                }
            }
            return code
        }
    }
}
