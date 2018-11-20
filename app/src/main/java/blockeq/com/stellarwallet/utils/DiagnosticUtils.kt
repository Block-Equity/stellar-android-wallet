package blockeq.com.stellarwallet.utils

import android.os.Build

class DiagnosticUtils {

    companion object {
        fun getDeviceName(): String {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
                StringFormat.capitalize(model)
            } else {
                StringFormat.capitalize(manufacturer) + " " + model
            }
        }
    }
}