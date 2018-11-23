package com.blockeq.stellarwallet.helpers

/**
 * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki">BIP-0039 Standard</a>
 */
enum class Bip0039(val numberOfWords: Int) {
    TWELVE_WORDS(12),
    FIFTEEN_WORDS(15),
    EIGHTEEN_WORDS(18),
    TWENTY_ONE_WORDS(21),
    TWENTY_FOUR_WORDS(24);

    companion object {
        fun isValidNumberOfWords(number: Int): Boolean {
            Bip0039.values().forEach {
                if (number == it.numberOfWords) {
                    return true
                }
            }
            return false
        }
    }
}