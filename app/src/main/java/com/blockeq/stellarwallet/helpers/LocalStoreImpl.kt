package com.blockeq.stellarwallet.helpers

import android.content.SharedPreferences
import com.blockeq.stellarwallet.interfaces.LocalStore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.stellar.sdk.responses.AccountResponse
import timber.log.Timber

class LocalStoreImpl(private val sharedPreferences: SharedPreferences, private val gson: Gson) : LocalStore {
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
        return getBoolean(KEY_IS_RECOVERY_PHRASE)
    }

    override fun setIsRecoveryPhrase(isRecoveryPhrase : Boolean) {
        set(KEY_IS_RECOVERY_PHRASE, isRecoveryPhrase)
    }

    override fun setShowPinOnSend(showPinOnSend: Boolean) {
        set(KEY_PIN_SETTINGS_SEND, showPinOnSend)
    }

    override fun getShowPinOnSend() : Boolean {
        return getBoolean(KEY_PIN_SETTINGS_SEND)
    }

    private companion object {
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

    private fun getBoolean(key: String): Boolean {
        //TODO: refactor this, the default value true is the expected behavior for isRecoveryPhrase and showPinOnSend.
        return sharedPreferences.getBoolean(key, true)
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

    override fun clearUserData() : Boolean {
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
