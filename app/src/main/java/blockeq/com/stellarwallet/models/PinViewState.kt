package blockeq.com.stellarwallet.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class PinType {
    CREATE, CHECK, VIEW_PHRASE, CLEAR_WALLET
}

@Parcelize
class PinViewState (var type: PinType, var message: String, var pin: String, var phrase: String): Parcelable
