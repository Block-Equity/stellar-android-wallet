package com.blockeq.stellarwallet.vmodels

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.blockeq.stellarwallet.activities.EnterAddressActivity
import com.blockeq.stellarwallet.models.Contact

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
        val RAW_PROJECTION = arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Data.MIMETYPE)
        val cursor = appContext.contentResolver.query(uri, RAW_PROJECTION,
                ContactsContract.Data.MIMETYPE + " = ?",
                arrayOf(mimetypeStellarAddress), null)
        return cursor
    }

    interface OnContactListLoaded {
        fun onLoaded(cursor : Cursor)
    }


    private var PROJECTION = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

    fun getContactListAsync(fragment: Fragment, listener: OnContactListLoaded) {
        LoaderManager.getInstance(fragment).initLoader(0,
                null, object : LoaderManager.LoaderCallbacks<Cursor> {
            override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
                val cursorLoader = CursorLoader(appContext)
                cursorLoader.projection
                // Starts the query
                return CursorLoader(
                        appContext,
                        ContactsContract.Contacts.CONTENT_URI,
                        PROJECTION, null, null, null)
            }

            override fun onLoadFinished(objectLoader: Loader<Cursor>, c: Cursor) {
                listener.onLoaded(c)
            }

            override fun onLoaderReset(cursorLoader: Loader<Cursor>) { }
        })
    }

    /**
     * it will create and return a new cursor, the cursor will be not closed by {@link ContactsRepository}
     */
    fun getContactsList() : Cursor? {
        val uri = ContactsContract.Data.CONTENT_URI
        val RAW_PROJECTION = arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Data.MIMETYPE)
        val cursor = appContext.contentResolver.query(uri, RAW_PROJECTION,
                null, null, null)
        return cursor
    }


    fun getStellarAddress2(contactId: Long): String? {
        val uri = ContactsContract.Data.CONTENT_URI

        val RAW_PROJECTION = arrayOf(ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1)
        val cursor = appContext.contentResolver.query(uri, RAW_PROJECTION,
                ContactsContract.Data.RAW_CONTACT_ID + "=?", arrayOf(contactId.toString()), null)
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


    fun getStellarAddress(contactId: Long): String? {
        val uri = ContactsContract.Data.CONTENT_URI

        val RAW_PROJECTION = arrayOf(ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1)
        val cursor = appContext.contentResolver.query(uri, RAW_PROJECTION,
                ContactsContract.Data.RAW_CONTACT_ID + "=?", arrayOf(contactId.toString()), null)
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

    enum class Status {
        INSERTED,
        UPDATED,
        FAILED
    }

    fun updateContact(contactId:Long, value:String) : Status {
        try {
            val values = ContentValues()
            values.put(ContactsContract.Data.DATA1, value)
            val contentResolver = appContext.contentResolver
            val mod = contentResolver.update(
                    ContactsContract.Data.CONTENT_URI,
                    values,
                    ContactsContract.Data.RAW_CONTACT_ID + "='" + contactId + "' AND "
                            + ContactsContract.Data.MIMETYPE + "= '"
                            + EnterAddressActivity.mimetypeStellarAddress + "'", null)

            return if (mod == 0) {
                values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                values.put(ContactsContract.Data.MIMETYPE, EnterAddressActivity.mimetypeStellarAddress)
                contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
                Status.INSERTED
            } else {
                Status.UPDATED

            }
        } catch (e: Exception) {
            return Status.FAILED
        }
    }

    /**
     * Move the cursor before calling this.
     */
    fun toContact(cursor:Cursor) : Contact {
        val nameColIdx: Int = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val idColIdx: Int = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        val contactName = cursor.getString(nameColIdx)
        val contactId = cursor.getLong(idColIdx)

        val stellarAddress : String? = ContactsRepository(appContext).getStellarAddress(contactId)

        val profilePic = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)

        return Contact(contactId, contactName, profilePic, stellarAddress)
    }
}