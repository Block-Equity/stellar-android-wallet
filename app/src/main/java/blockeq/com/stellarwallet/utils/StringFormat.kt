package blockeq.com.stellarwallet.utils

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
    }
}