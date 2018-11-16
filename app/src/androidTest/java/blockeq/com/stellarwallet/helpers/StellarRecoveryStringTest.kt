package blockeq.com.stellarwallet.helpers

import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StellarRecoveryStringTest {
    private val emptyString = ""
    private val mnemonic12 = "senior buzz box spawn cool finish mistake walnut useful another enable spatial"
    private val mnemonic24 = "senior buzz box spawn cool finish mistake walnut useful another enable spatial senior buzz box spawn cool finish mistake walnut useful another enable spatial"
    private val secretSeed = "SDLOPMAX6BPWTDVQZZAR47JCVKQM4EI52LP4XLDO75M7OA2C2XZ7Z3UZ"
    private val lessWordsMnemonic12 = "senior buzz box spawn cool finish mistake walnut useful"
    private val moreWordsMnemonic12 = "senior buzz box spawn cool finish mistake walnut useful another enable spatial spatial cool box"
    private val lessWordsMnemonic24 = "senior buzz box spawn cool finish mistake walnut useful another enable spatial senior buzz box spawn cool finish mistake"
    private val moreWordsMnemonic24 = "senior buzz box spawn cool finish mistake walnut useful another enable spatial senior buzz box spawn cool finish mistake walnut useful another enable spatial useful another enable spatial"
    private val invalidStartingCharSecretSeed = "ADLOPMAX6BPWTDVQZZAR47JCVKQM4EI52LP4XLDO75M7OA2C2XZ7Z3UZ"
    private val lessCharsSecretSeed = "SDLOPMAX6BPWTDVQZZAR47JCVKQM4EI52LP4XLDO75M7OA2C2XZ7"
    private val moreCharsSecretSeed = "SDLOPMAX6BPWTDVQZZAR47JCVKQM4EI52LP4XLDO75M7OA2C2XZ7Z3UZ123"
    private val invalidSecretSeedWithValidLengthAndStartingChar = "SDLOPMAX6BPWTDVQZZAR47JCVKQM4EI52LP4XLDO75M7OA2C2XZ7Z3UL"

    @Test
    fun regular_mnemonic12() {
        var isError = false
        try {
            StellarRecoveryString(mnemonic12, null, true).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(!isError)
    }

    @Test
    fun regular_mnemonic24() {
        var isError = false
        try {
            StellarRecoveryString(mnemonic24, null, true).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(!isError)
    }

    @Test
    fun regular_secret_seed() {
        var isError = false
        try {
            StellarRecoveryString(secretSeed, null, true).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(!isError)
    }

    @Test
    fun empty_string_mnemonic() {
        var isError = false
        try {
            StellarRecoveryString(emptyString, null, true).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(isError)
    }

    @Test
    fun empty_string_secret_seed() {
        var isError = false
        try {
            StellarRecoveryString(emptyString, null, false).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(isError)
    }

    @Test
    fun less_words_mnemonic12() {
        var isError = false
        try {
            StellarRecoveryString(lessWordsMnemonic12, null, false).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(isError)
    }

    @Test
    fun more_words_mnemonic12() {
        var isError = false
        try {
            StellarRecoveryString(moreWordsMnemonic12, null, false).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(isError)
    }

    @Test
    fun less_words_mnemonic24() {
        var isError = false
        try {
            StellarRecoveryString(lessWordsMnemonic24, null, false).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(isError)
    }

    @Test
    fun more_words_mnemonic24() {
        var isError = false
        try {
            StellarRecoveryString(moreWordsMnemonic24, null, false).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(isError)
    }

    @Test
    fun invalid_starting_char_secret_seed() {
        var isError = false
        try {
            StellarRecoveryString(invalidStartingCharSecretSeed, null, false).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(isError)
    }

    @Test
    fun less_chars_secret_seed() {
        var isError = false
        try {
            StellarRecoveryString(lessCharsSecretSeed, null, false).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(isError)
    }

    @Test
    fun more_chars_secret_seed() {
        var isError = false
        try {
            StellarRecoveryString(moreCharsSecretSeed, null, false).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(isError)
    }

    @Test
    fun invalid_secret_seed_with_valid_length_and_starting_char() {
        var isError = false
        try {
            StellarRecoveryString(invalidSecretSeedWithValidLengthAndStartingChar, null, false).recoveryString
        } catch (e: Exception) {
            isError = true
        }
        assert(isError)
    }
}
