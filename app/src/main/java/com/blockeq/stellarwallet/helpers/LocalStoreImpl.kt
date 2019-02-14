package com.blockeq.stellarwallet.helpers

import android.content.Context
import com.blockeq.stellarwallet.interfaces.LocalStore
import org.stellar.sdk.responses.AccountResponse
import shadow.com.google.gson.Gson
import shadow.com.google.gson.JsonSyntaxException
import timber.log.Timber

class LocalStoreImpl(context: Context) : LocalStore {
    private companion object {
        const val PRIVATE_MODE = 0
        const val PREF_NAME = "com.blockeq.stellarwallet.PREFERENCE_FILE_KEY"

        const val KEY_ENCRYPTED_PHRASE = "kEncryptedPhrase"
        const val KEY_ENCRYPTED_PASSPHRASE = "kEncryptedPassphrase"
        const val KEY_PIN_DATA = "kPinData"
        const val KEY_STELLAR_ACCOUNT_PUBLIC_KEY = "kStellarAccountPublicKey"
        const val KEY_STELLAR_BALANCES_KEY = "kStellarBalancesKey"
        const val KEY_STELLAR_AVAILABLE_BALANCE_KEY = "kAvailableBalanceKey"
        const val KEY_IS_RECOVERY_PHRASE = "kIsRecoveryPhrase"
        const val KEY_PIN_SETTINGS_SEND = "kPinSettingsSend"
        const val KEY_IS_PASSPHRASE_USED = "kIsPassphraseUsed"
    }

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    private val gson = Gson()

    override fun getEncryptedPhrase() : String? {
        return getString(KEY_ENCRYPTED_PHRASE)
    }

    override fun setEncryptedPhrase(encryptedPassphrase : String?) {
        set(KEY_ENCRYPTED_PHRASE, encryptedPassphrase)
    }

    override fun getEncryptedPassphrase(): String? {
        return getString(KEY_ENCRYPTED_PASSPHRASE)
    }

    override fun setEncryptedPassphrase(encryptedPassphrase : String) {
        set(KEY_ENCRYPTED_PASSPHRASE, encryptedPassphrase)
    }

    override fun getStellarAccountId() : String? {
        return getString(KEY_STELLAR_ACCOUNT_PUBLIC_KEY)
    }

    override fun setStellarAccountId(accountId : String) {
        return set(KEY_STELLAR_ACCOUNT_PUBLIC_KEY, accountId)
    }

    override fun getBalances() : Array<AccountResponse.Balance> {
        return get(KEY_STELLAR_BALANCES_KEY, Array<AccountResponse.Balance>::class.java) ?: return arrayOf()
    }

    override fun setBalances(balances : Array<AccountResponse.Balance>?) {
        set(KEY_STELLAR_BALANCES_KEY, balances)
    }

    override fun getAvailableBalance(): String {
        return getString(KEY_STELLAR_AVAILABLE_BALANCE_KEY) ?: Constants.DEFAULT_ACCOUNT_BALANCE
    }

    override fun setAvailableBalance(availableBalance:String?) {
        return set(KEY_STELLAR_AVAILABLE_BALANCE_KEY, availableBalance)
    }

    override fun getIsRecoveryPhrase() : Boolean {
        return if (contains(KEY_IS_RECOVERY_PHRASE)) {
            getBoolean(KEY_IS_RECOVERY_PHRASE)
        } else {
            //default recovery method is recovery phrase
            true
        }
    }

    override fun setIsRecoveryPhrase(isRecoveryPhrase : Boolean) {
        set(KEY_IS_RECOVERY_PHRASE, isRecoveryPhrase)
    }

    override fun setShowPinOnSend(showPinOnSend: Boolean) {
        set(KEY_PIN_SETTINGS_SEND, showPinOnSend)
    }

    override fun getShowPinOnSend() : Boolean {
        return if(contains(KEY_PIN_SETTINGS_SEND)) {
            getBoolean(KEY_PIN_SETTINGS_SEND)
        } else {
            //default logic is show pin on send
            true
        }
    }

    private operator fun set(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    private operator fun set(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    private operator fun <T> set(key: String, obj: T) {
        val json = gson.toJson(obj)
        set(key, json)
    }

    private fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    private fun contains(key:String) : Boolean {
        return sharedPreferences.contains(key)
    }

    private fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    private operator fun <T> get(key: String, klass: Class<T>): T? {
        val json = getString(key) ?: return null
        return try {
            gson.fromJson(json, klass)
        } catch (e: JsonSyntaxException) {
            Timber.w( "unable to convert json $e")
            null
        }
    }

    override fun clearLocalStore() : Boolean {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_ENCRYPTED_PHRASE)
        editor.remove(KEY_ENCRYPTED_PASSPHRASE)
        editor.remove(KEY_PIN_DATA)
        editor.remove(KEY_STELLAR_ACCOUNT_PUBLIC_KEY)
        editor.remove(KEY_STELLAR_BALANCES_KEY)
        editor.remove(KEY_STELLAR_AVAILABLE_BALANCE_KEY)
        editor.remove(KEY_IS_RECOVERY_PHRASE)
        editor.remove(KEY_IS_PASSPHRASE_USED)
        return editor.commit()
    }
}
