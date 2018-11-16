package blockeq.com.stellarwallet.helpers

import blockeq.com.stellarwallet.utils.AccountUtils

class StellarRecoveryString (string : String, val isRecoveryPhrase : Boolean, val passphrase : String? = null) {

    private var recoveryString: String = string.trim()

    class InvalidWordCountException(message: String): Exception(message)
    class InvalidStellarSecretSeedException(message: String): Exception(message)


    @Throws(InvalidWordCountException::class, InvalidStellarSecretSeedException::class)
    fun getString() : String {
        val wordCount = recoveryString.split(" ".toRegex()).size

        var invalidRecovery = false
        try {
            AccountUtils.getStellarKeyPair(recoveryString, passphrase)
        } catch (e : Exception) {
            invalidRecovery = true
        }

        if (isRecoveryPhrase) {
            if ((wordCount != 12 && wordCount != 24) || invalidRecovery) {
                throw InvalidWordCountException("Invalid Word Count: Please check the number of words in your phrase or any extra spaces between words")
            }
        } else {

            if (recoveryString.length != Constants.STELLAR_ADDRESS_LENGTH || recoveryString[0] != 'S' || invalidRecovery) {
                throw InvalidStellarSecretSeedException("Invalid Secret Seed: Length should be ${Constants.STELLAR_ADDRESS_LENGTH} characters. The first character should be 'S'")
            }
        }

        return recoveryString
    }
}
