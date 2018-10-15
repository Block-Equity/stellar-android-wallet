package blockeq.com.stellarwallet.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class PinType {
    CREATE_WITH_PHRASE, CREATE_WITH_SECRET, LOGIN, VIEW_PHRASE, VIEW_SEED, CLEAR_WALLET, CHECK
}

@Parcelize
class PinViewState (var type: PinType, var message: String, var pin: String, var phrase: String): Parcelable
