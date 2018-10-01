package blockeq.com.stellarwallet.utils

import blockeq.com.stellarwallet.WalletApplication
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class StringFormat {
    companion object {
        fun getFormattedDate(str: String):String {
            val formatter = DateTimeFormatter.ofPattern("MMM dd, uuuu", Locale.ENGLISH)
                    .withZone(ZoneId.of("UTC"))
            return formatter.format(Instant.parse(str))
        }

        fun truncateDecimalPlaces(string: String?): String {
            if (string == null) return ""
            return String.format("%.4f", string.toDouble())
        }

        /**
         * Calculate the number of decimals of a string.
         */
        fun getNumDecimals(num: String): Int {
            return if (!hasDecimalPoint(num)) {
                0
            } else {
                num.substring(num.indexOf('.') + 1, num.length).length
            }
        }

        /**
         * Checks whether a string has a comma.
         */
        fun hasDecimalPoint(text: String): Boolean {
            for (i in 0 until text.length) {
                if (text[i] == '.') {
                    return true
                }
            }
            return false
        }

        fun getAvailableBalance(): String {
            return "Available: " + AccountUtils.getBalance(WalletApplication.currAsset) + " XLM"
        }
    }
}