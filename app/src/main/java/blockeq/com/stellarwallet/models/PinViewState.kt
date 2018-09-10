package blockeq.com.stellarwallet.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class PinType {
    CREATE, CHECK
}

@Parcelize
class PinViewState (var type: PinType, var message: String, var pin: String, var phrase: String): Parcelable
