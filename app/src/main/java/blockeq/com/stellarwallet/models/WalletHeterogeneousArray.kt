package blockeq.com.stellarwallet.models

import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import org.stellar.sdk.AssetTypeCreditAlphaNum
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.responses.effects.*

class WalletHeterogeneousArray(totalBalance: TotalBalance, availableBalance: AvailableBalance,
                               pair: Pair<*, *>, effectsList: ArrayList<EffectResponse>?) : ArrayList<Any>() {

    companion object {
        const val TOTAL_INDEX = 0
        const val AVAILABLE_INDEX = 1
        const val PAIR_INDEX = 2
        const val EFFECTS_LIST_INDEX = 3
    }


    var array: ArrayList<Any> = ArrayList()
    var availableBalanceOffset = 0

    init {
        array.add(totalBalance)
        array.add(availableBalance)
        array.add(pair)
        addFilteredEffects(effectsList)
    }

    //region Update methods

    fun updateTotalBalance(balance: TotalBalance) {
        array.removeAt(TOTAL_INDEX)
        array.add(TOTAL_INDEX, balance)
    }

    fun updateAvailableBalance(balance: AvailableBalance) {
        if (WalletApplication.userSession.currAssetCode == Constants.LUMENS_ASSET_TYPE) {
            array.removeAt(AVAILABLE_INDEX)
            array.add(AVAILABLE_INDEX, balance)
        }
    }

    fun updatePair(p: Pair<*, *>) {
        array.removeAt(PAIR_INDEX - availableBalanceOffset)
        array.add(PAIR_INDEX - availableBalanceOffset, p)
    }

    fun updateEffectsList(list: ArrayList<EffectResponse>?) {
        array.subList(EFFECTS_LIST_INDEX - availableBalanceOffset, array.size).clear()
        addFilteredEffects(list)
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

    private fun addFilteredEffects(list: ArrayList<EffectResponse>?) {
        val filteredEffects = getFilteredEffects(list, WalletApplication.userSession.currAssetCode)
        if (filteredEffects != null) {
            array.addAll(convertEffectsToAccountEffects(filteredEffects))
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

    private fun convertEffectsToAccountEffects(list: ArrayList<EffectResponse>) : ArrayList<Any> {
        return list.map {
            if (it is TradeEffectResponse) {
                return@map TradeEffect(it.type, it.createdAt, getBoughtAsset(it), getSoldAsset(it),
                        it.boughtAmount, it.soldAmount)
            } else {
                return@map AccountEffect(it.type, it.createdAt, getAssetCode(it), getAmount(it))
            }
        } as ArrayList
    }

    private fun getAssetCode(effect: EffectResponse): String? {
       if (effect is AccountCreditedEffectResponse) {
           if (effect.asset is AssetTypeCreditAlphaNum) {
               return (effect.asset as AssetTypeCreditAlphaNum).code
           } else {
               return (effect.asset as AssetTypeNative).type
           }
       } else if (effect is AccountDebitedEffectResponse) {
           if (effect.asset is AssetTypeCreditAlphaNum) {
               return (effect.asset as AssetTypeCreditAlphaNum).code
           } else {
               return (effect.asset as AssetTypeNative).type
           }
       } else if (effect is TrustlineCreatedEffectResponse) {
           return (effect.asset as AssetTypeCreditAlphaNum).code
       } else if (effect is TrustlineRemovedEffectResponse) {
           return (effect.asset as AssetTypeCreditAlphaNum).code
       } else if (effect is TrustlineUpdatedEffectResponse) {
           return (effect.asset as AssetTypeCreditAlphaNum).code
       } else {
           return null
       }
    }

    private fun getAmount(effectResponse: EffectResponse) : String? {
        if (effectResponse is AccountCreditedEffectResponse) {
            return effectResponse.amount
        } else if (effectResponse is AccountDebitedEffectResponse) {
            return effectResponse.amount
        } else if (effectResponse is AccountCreatedEffectResponse) {
            return effectResponse.startingBalance
        } else {
            return null
        }
    }

    private fun getBoughtAsset(trade: TradeEffectResponse) : String {
        if (trade.boughtAsset is AssetTypeCreditAlphaNum) {
            return (trade.boughtAsset as AssetTypeCreditAlphaNum).code
        } else {
            return (trade.boughtAsset as AssetTypeNative).type
        }
    }

    private fun getSoldAsset(trade: TradeEffectResponse) : String {
        if (trade.soldAsset is AssetTypeCreditAlphaNum) {
            return (trade.soldAsset as AssetTypeCreditAlphaNum).code
        } else {
            return (trade.soldAsset as AssetTypeNative).type
        }
    }

}
