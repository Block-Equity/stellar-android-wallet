package blockeq.com.stellarwallet.helpers

class StellarRecoveryString (string : String, isRecoveryPhrase : Boolean) {

    var recoveryString: String = string.trim()

    class InvalidWordCountException(message: String): Exception(message)
    class InvalidStellarSecretSeedException(message: String): Exception(message)

    init {
        val wordCount = recoveryString.split(" ".toRegex()).size

        if (isRecoveryPhrase) {
            if (wordCount != 12 || wordCount != 24) {
                throw InvalidWordCountException("InvalidWordCountException: Please check the number of words in your phrase or any extra spaces between words")
            }
        } else {
            if (recoveryString.length != Constants.STELLAR_ADDRESS_LENGTH || recoveryString[0] != 'S') {
                throw InvalidStellarSecretSeedException("InvalidStellarSecretSeedException: Length ${recoveryString.length} should be ${Constants.STELLAR_ADDRESS_LENGTH}. The first character (${recoveryString[0]} should be 'S')")
            }
        }
    }
}
