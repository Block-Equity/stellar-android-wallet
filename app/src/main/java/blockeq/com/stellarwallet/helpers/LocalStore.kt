package blockeq.com.stellarwallet.helpers

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException


class LocalStore(sharedPreferences: SharedPreferences, gson: Gson) {

    private val sharedPreferences: SharedPreferences
    private val gson: Gson

    companion object {
        const val KEY_ENCRYPTED_PHRASE = "kEncryptedPhrase"
        const val CUSTOM_FIELDS = "kCustomFields"
    }

    init {
        this.sharedPreferences = sharedPreferences
        this.gson = gson
    }

    private operator fun set(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    private operator fun <T> set(key: String, obj: T) {
        val json = gson.toJson(obj)
        set(key, json)
    }

    private operator fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    private operator fun <T> get(key: String, klass: Class<T>): T? {
        val json = get(key) ?: return null
        try {
            return gson.fromJson(json, klass)
        } catch (e: JsonSyntaxException) {
            Log.w("LocalStoreImpl", "unable to convert json", e)
            return null
        }

    }

    fun clearUserData() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_ENCRYPTED_PHRASE)
        editor.remove(CUSTOM_FIELDS)
        editor.apply()
    }

    /*
    Example of setting/getting an object with gson serialization
    var customFields: CustomField?
        get() = get<CustomField>(CUSTOM_FIELDS, CustomField::class.java)
        set(customField) = set<CustomField>(CUSTOM_FIELDS, customField)
     */

    var stripeCustomerId: String?
        get() = get(KEY_ENCRYPTED_PHRASE)
        set(stripeCustomerId) = set(KEY_ENCRYPTED_PHRASE, stripeCustomerId)
}