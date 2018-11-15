package blockeq.com.stellarwallet.helpers

import org.stellar.sdk.KeyPair

class StellarRecoveryString (string : String, isRecoveryPhrase : Boolean) {

    var recoveryString: String = string.trim()

    class InvalidWordCountException(message: String): Exception(message)
    class InvalidStellarSecretSeedException(message: String): Exception(message)

    init {
        val wordCount = recoveryString.split(" ".toRegex()).size

        if (isRecoveryPhrase) {
            if (wordCount != 12 && wordCount != 24) {
                throw InvalidWordCountException("Invalid Word Count: Please check the number of words in your phrase or any extra spaces between words")
            }
        } else {
            var invalidSecret = false
            try {
                KeyPair.fromSecretSeed(recoveryString)
            } catch (e : Exception) {
                invalidSecret = true
            }
            if (recoveryString.length != Constants.STELLAR_ADDRESS_LENGTH || recoveryString[0] != 'S' || invalidSecret) {
                throw InvalidStellarSecretSeedException("Invalid Secret Seed: Length should be ${Constants.STELLAR_ADDRESS_LENGTH} characters. The first character should be 'S'")
            }
        }
    }
}
