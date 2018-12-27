package com.blockeq.stellarwallet.vmodels

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract

class ContactsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val mimetypeStellarAddress = "vnd.android.cursor.item/sellarAccount"

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
        return getStellarContactsList()
    }
}