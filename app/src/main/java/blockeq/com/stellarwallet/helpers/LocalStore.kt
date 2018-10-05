package blockeq.com.stellarwallet.helpers

import android.content.SharedPreferences
import android.util.Log
import blockeq.com.stellarwallet.models.PinViewState
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.stellar.sdk.responses.AccountResponse


class LocalStore(private val sharedPreferences: SharedPreferences, private val gson: Gson) {

    var pinViewState: PinViewState?
        get() = get<PinViewState>(KEY_PIN_DATA, PinViewState::class.java)
        set(viewState) = set<PinViewState>(KEY_PIN_DATA, viewState!!)

    var encryptedPhrase: String?
        get() = get(KEY_ENCRYPTED_PHRASE)
        set(encryptedPhrase) = set(KEY_ENCRYPTED_PHRASE, encryptedPhrase)

    var publicKey: String?
        get() = get(KEY_STELLAR_ACCOUNT_PUBLIC_KEY)
        set(publicKey) = set(KEY_STELLAR_ACCOUNT_PUBLIC_KEY, publicKey)

    var balances: Array<AccountResponse.Balance>?
        get() = get(KEY_STELLAR_BALANCES_KEY, Array<AccountResponse.Balance>::class.java)
        set(balances) = set(KEY_STELLAR_BALANCES_KEY, balances)

    var availableBalance: String?
        get() = get(KEY_STELLAR_AVAILABLE_BALANCE_KEY)
        set(availableBalance) = set(KEY_STELLAR_AVAILABLE_BALANCE_KEY, availableBalance)

    init {
        balances = arrayOf()
        availableBalance = Constants.DEFAULT_ACCOUNT_BALANCE
    }

    private companion object {
        const val KEY_ENCRYPTED_PHRASE = "kEncryptedPhrase"
        const val KEY_PIN_DATA = "kPinData"
        const val KEY_STELLAR_ACCOUNT_PUBLIC_KEY = "kStellarAccountPublicKey"
        const val KEY_STELLAR_BALANCES_KEY = "kStellarBalancesKey"
        const val KEY_STELLAR_AVAILABLE_BALANCE_KEY = "kAvailableBalanceKey"
    }

    private operator fun set(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    private operator fun <T> set(key: String, obj: T) {
        val json = gson.toJson(obj)
        set(key, json)
    }

    private operator fun get(key: String): String? {
            return sharedPreferences.getString(key, "")
    }

    private operator fun <T> get(key: String, klass: Class<T>): T? {
        val json = get(key) ?: return null
        return try {
            gson.fromJson(json, klass)
        } catch (e: JsonSyntaxException) {
            Log.w("LocalStoreImpl", "unable to convert json", e)
            null
        }
    }

    fun clearUserData() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_ENCRYPTED_PHRASE)
        editor.remove(KEY_PIN_DATA)
        editor.remove(KEY_STELLAR_ACCOUNT_PUBLIC_KEY)
        editor.remove(KEY_STELLAR_BALANCES_KEY)
        editor.remove(KEY_STELLAR_AVAILABLE_BALANCE_KEY)
        editor.apply()

        balances = arrayOf()
        availableBalance = Constants.DEFAULT_ACCOUNT_BALANCE
    }

    fun clearPINData() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_PIN_DATA)
        editor.apply()
    }
}
