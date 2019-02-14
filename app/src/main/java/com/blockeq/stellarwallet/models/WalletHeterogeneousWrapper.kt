package com.blockeq.stellarwallet.models

import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants
import org.stellar.sdk.AssetTypeCreditAlphaNum
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.responses.effects.*

class WalletHeterogeneousWrapper {

    companion object {
        const val TOTAL_INDEX = 0
        const val AVAILABLE_INDEX = 1
        const val PAIR_INDEX = 2
        const val EFFECTS_LIST_INDEX = 3
    }

    var array: ArrayList<Any> = ArrayList()
    private var availableBalanceOffset = 0

    //region Update methods

    fun updateTotalBalance(balance: TotalBalance) {
        array.removeAt(TOTAL_INDEX)
        array.add(TOTAL_INDEX, balance)
    }

    fun updateAvailableBalance(balance: AvailableBalance) {
        if (WalletApplication.userSession.getSessionAsset().assetCode == Constants.LUMENS_ASSET_TYPE) {
            array.removeAt(AVAILABLE_INDEX)
            array.add(AVAILABLE_INDEX, balance)
        }
    }

    fun updatePair(p: Pair<*, *>) {
        array.removeAt(PAIR_INDEX - availableBalanceOffset)
        array.add(PAIR_INDEX - availableBalanceOffset, p)
    }

    fun hidePair() {
        if (array.size > PAIR_INDEX - availableBalanceOffset) {
            array.removeAt(PAIR_INDEX - availableBalanceOffset)
        }
    }

    fun updateEffectsList(activeAsset: String, list: ArrayList<EffectResponse>?) {
        array.subList(EFFECTS_LIST_INDEX - availableBalanceOffset, array.size).clear()
        addFilteredEffects(activeAsset, list)
    }

    //endregion

    fun hideAvailableBalance() {
        if (availableBalanceOffset == 0) {
            array.removeAt(AVAILABLE_INDEX)
            availableBalanceOffset = 1
        }
    }

    fun showAvailableBalance(balance: AvailableBalance) {
        if (availableBalanceOffset != 0) {
            availableBalanceOffset = 0
            array.add(AVAILABLE_INDEX, balance)
        }
    }

    private fun addFilteredEffects(activeAsset: String, list: ArrayList<EffectResponse>?) {
        val filteredEffects = getFilteredEffects(list, activeAsset)
        if (filteredEffects != null) {
            array.addAll(convertEffectsToAccountEffects(activeAsset, filteredEffects))
        }
    }

    private fun getFilteredEffects(list: ArrayList<EffectResponse>?, assetType: String) : ArrayList<EffectResponse>? {
        if (list == null) return null

        return (list.filter {
                (it.type == EffectType.RECEIVED.value && getAssetCode(it) == assetType) ||
                (it.type == EffectType.SENT.value && getAssetCode(it) == assetType) ||
                (it.type == EffectType.TRUSTLINE_CREATED.value && getAssetCode(it) == assetType) ||
                (it.type == EffectType.TRUSTLINE_REMOVED.value && getAssetCode(it) == assetType) ||
                (it.type == EffectType.TRUSTLINE_UPDATED.value && getAssetCode(it) == assetType) ||
                (it.type == EffectType.ACCOUNT_INFLATION_DESTINATION_UPDATED.value && assetType == Constants.LUMENS_ASSET_TYPE) ||
                (it.type == EffectType.CREATED.value && assetType == Constants.LUMENS_ASSET_TYPE) ||
                (it.type == EffectType.SIGNER_UPDATED.value && assetType == Constants.LUMENS_ASSET_TYPE) ||
                (it.type == EffectType.SIGNER_REMOVED.value && assetType == Constants.LUMENS_ASSET_TYPE) ||
                (it.type == EffectType.SIGNER_CREATED.value && assetType == Constants.LUMENS_ASSET_TYPE) ||
                (it.type == EffectType.TRADE.value && (getBoughtAsset(it as TradeEffectResponse) == assetType ||
                        getSoldAsset(it) == assetType))

        } as ArrayList)
    }

    private fun convertEffectsToAccountEffects(activeAsset: String, list: ArrayList<EffectResponse>) : ArrayList<Any> {
        return list.map {
            if (it is TradeEffectResponse) {
                return@map TradeEffect(activeAsset, it.type, it.createdAt, getBoughtAsset(it), getSoldAsset(it),
                        it.boughtAmount, it.soldAmount)
            } else {
                return@map AccountEffect(it.type, it.createdAt, getAssetCode(it), getAmount(it))
            }
        } as ArrayList
    }

    private fun getAssetCode(effect: EffectResponse): String? {
        when (effect) {
            is AccountCreditedEffectResponse -> return if (effect.asset is AssetTypeCreditAlphaNum) {
                (effect.asset as AssetTypeCreditAlphaNum).code
            } else {
                (effect.asset as AssetTypeNative).type
            }
            is AccountDebitedEffectResponse -> return if (effect.asset is AssetTypeCreditAlphaNum) {
                (effect.asset as AssetTypeCreditAlphaNum).code
            } else {
                (effect.asset as AssetTypeNative).type
            }
            is TrustlineCreatedEffectResponse -> return (effect.asset as AssetTypeCreditAlphaNum).code
            is TrustlineRemovedEffectResponse -> return (effect.asset as AssetTypeCreditAlphaNum).code
            is TrustlineUpdatedEffectResponse -> return (effect.asset as AssetTypeCreditAlphaNum).code
            else -> return null
        }
    }

    private fun getAmount(effectResponse: EffectResponse) : String? {
        return when (effectResponse) {
            is AccountCreditedEffectResponse -> effectResponse.amount
            is AccountDebitedEffectResponse -> effectResponse.amount
            is AccountCreatedEffectResponse -> effectResponse.startingBalance
            else -> null
        }
    }

    private fun getBoughtAsset(trade: TradeEffectResponse) : String {
        return if (trade.boughtAsset is AssetTypeCreditAlphaNum) {
            (trade.boughtAsset as AssetTypeCreditAlphaNum).code
        } else {
            (trade.boughtAsset as AssetTypeNative).type
        }
    }

    private fun getSoldAsset(trade: TradeEffectResponse) : String {
        return if (trade.soldAsset is AssetTypeCreditAlphaNum) {
            (trade.soldAsset as AssetTypeCreditAlphaNum).code
        } else {
            (trade.soldAsset as AssetTypeNative).type
        }
    }
}
