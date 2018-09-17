package blockeq.com.stellarwallet.models

enum class ActivityType {
    SEND, RECEIVE
}

data class AccountActivityItem (var activityType: ActivityType, var amount: String)
