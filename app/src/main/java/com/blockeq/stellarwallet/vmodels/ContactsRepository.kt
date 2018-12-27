package com.blockeq.stellarwallet.vmodels

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.blockeq.stellarwallet.activities.EnterAddressActivity
import java.lang.IllegalStateException

@SuppressLint("StaticFieldLeak")
object ContactsRepository {
    private lateinit var appContext : Context
    private val mimetypeStellarAddress = "vnd.android.cursor.item/sellarAccount"

    operator fun invoke(): ContactsRepository {
            throw IllegalStateException("not valid constructor, use " + ContactsRepository::class.java.canonicalName + "(context)")
    }

    operator fun invoke(context: Context): ContactsRepository {
        appContext =  context.applicationContext
        return this
    }

    /**
     * it will create and return a new cursor, the cursor will be not closed by {@link ContactsRepository}
     */
    fun getStellarContactsList() : Cursor? {
        val uri = ContactsContract.Data.CONTENT_URI
        val RAW_PROJECTION = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Data.MIMETYPE)
        val cursor = appContext.contentResolver.query(uri, RAW_PROJECTION,
                ContactsContract.Data.MIMETYPE + " = ?",
                arrayOf(mimetypeStellarAddress), null)
        return cursor
    }

    /**
     * it will create and return a new cursor, the cursor will be not closed by {@link ContactsRepository}
     */
    fun getContactsList() : Cursor? {
        val uri = ContactsContract.Data.CONTENT_URI
        val RAW_PROJECTION = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Data.MIMETYPE)
        val cursor = appContext.contentResolver.query(uri, RAW_PROJECTION,
                null, null, null)
        return cursor
    }

    fun getStellarAddress(contactId: Long): String? {
        val uri = ContactsContract.Data.CONTENT_URI

        val RAW_PROJECTION = arrayOf(ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1)
        val cursor = appContext.contentResolver.query(uri, RAW_PROJECTION,
                ContactsContract.Data._ID + "=?", arrayOf(contactId.toString()), null)
        var stellarAddress : String? = null
        if (cursor !== null) {
            while (cursor.moveToNext()) {
                val mime = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE))

                if (EnterAddressActivity.mimetypeStellarAddress == mime) {
                    stellarAddress = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1))
                }
            }
            cursor.close()
        }

        return stellarAddress
    }
}