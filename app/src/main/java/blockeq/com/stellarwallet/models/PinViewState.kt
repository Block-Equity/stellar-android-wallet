package blockeq.com.stellarwallet.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PinViewState (var message: String, var confirm: Boolean = false, var pin: String, var phrase: String): Parcelable
