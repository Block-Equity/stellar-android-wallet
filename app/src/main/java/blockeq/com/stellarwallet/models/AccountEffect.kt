package blockeq.com.stellarwallet.models

enum class EffectType(val value : String) {
    CREATED("account_created"), REMOVED("account_removed"),
    SENT("account_debited"), RECEIVED("account_credited"),
    ACCOUNT_HOME_DOMAIN_UPDATED("account_home_domain_updated"),
    ACCOUNT_FLAGS_UPDATED("account_flags_updated"),
    ACCOUNT_INFLATION_DESTINATION_UPDATED("account_inflation_destination_updated"),
    SIGNER_CREATED("signer_created"),
    SIGNER_REMOVED("signer_removed"),
    SIGNER_UPDATED("signer_updated"),
    TRUSTLINE_CREATED("trustline_created"),
    TRUSTLINE_REMOVED("trustline_removed"),
    TRUSTLINE_UPDATED("trustline_updated"),
    TRUSTLINE_AUTHORIZED("trustline_authorized"),
    TRUSTLINE_DEAUTHORIZED("trustline_deauthorized"),
    OFFER_CREATED("offer_created"),
    OFFER_REMOVED("offer_removed"),
    OFFER_UPDATED("offer_updated"),
    TRADE("trade"),
    DATA_CREATED("data_created"),
    DATA_REMOVED("data_removed"),
    DATA_UPDATED("data_updated"),
    SEQUENCE_BUMPED("sequence_bumped")
}

/**
 * Class which provides a model for Session
 * @constructor Sets all properties of the Session
 * @property type the type of account effect as shown in EffectType
 * @property createdAt the time at which the effect was created
 * @property assetCode the asset symbol
 * @property amount the amount of asset transacted if there was a transaction
 */
data class AccountEffect (var type: String, var createdAt: String,
                          var assetCode: String?, var amount: String?)

