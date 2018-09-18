package blockeq.com.stellarwallet.models

enum class EffectType(val value : String) {
    SEND("Send"), RECEIVE("Receive")
}

data class AccountEffect (var type: EffectType, var createdAt: String,
                          var assetType: String?, var assetCode: String?,
                          var amount: String?)
