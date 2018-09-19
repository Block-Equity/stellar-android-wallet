package blockeq.com.stellarwallet.models

enum class EffectType(val value : String) {
    CREATED("account_created"), REMOVED("account_removed"),
    SENT("account_debited"), RECEIVED("account_credited"),
    TRADE("trade")
}

data class AccountEffect (var type: EffectType, var createdAt: String,
                          var assetType: String?, var assetCode: String?,
                          var amount: String?)
