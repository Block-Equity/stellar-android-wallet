package com.blockeq.stellarwallet.mvvm.effects

import com.blockeq.stellarwallet.models.AvailableBalance
import com.blockeq.stellarwallet.models.TotalBalance
import org.stellar.sdk.responses.effects.EffectResponse

data class WalletViewState(var status: WalletStatus, var activeAssetCode : String, var availableBalance : AvailableBalance, var totalBalance : TotalBalance, var effectList: ArrayList<EffectResponse>?) {

    enum class WalletStatus {
        UNKNOWN,
        UNFUNDED,
        ACTIVE
    }
}