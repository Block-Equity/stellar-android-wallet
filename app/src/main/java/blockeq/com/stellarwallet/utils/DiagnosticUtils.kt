package blockeq.com.stellarwallet.utils

import android.os.Build
import blockeq.com.stellarwallet.BuildConfig
import java.util.*

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

        fun getAndroidVersion() : String {
            return android.os.Build.VERSION.SDK_INT.toString()
        }

        fun getLocale() : String {
            return Locale.getDefault().language
        }

        fun getAppVersion() : String {
            return BuildConfig.VERSION_NAME + " " + BuildConfig.VERSION_CODE
        }
    }
}