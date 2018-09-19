package blockeq.com.stellarwallet.models

data class TradeEffect (var type: String, var createdAt: String,
                        var boughtAsset: String, var soldAsset: String,
                        var boughtAmount: String?, var soldAmount: String?)
