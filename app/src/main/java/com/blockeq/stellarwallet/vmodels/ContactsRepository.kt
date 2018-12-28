package com.blockeq.stellarwallet.vmodels

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.RawContacts
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
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
        val RAW_PROJECTION = arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1)
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

    fun getStellarAddress(contactId: Long): String? {
        val uri = ContactsContract.Data.CONTENT_URI

        val RAW_PROJECTION = arrayOf(ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1)
        val cursor = appContext.contentResolver.query(uri, RAW_PROJECTION,
                ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?", arrayOf(contactId.toString(), mimetypeStellarAddress), null)
        var stellarAddress : String? = null
        if (cursor !== null) {
            while (cursor.moveToNext()) {
                 stellarAddress = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1))
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

    fun updateContact(contactId:Long, address:String) : Status {
        try {
            val values = ContentValues()
            values.put(ContactsContract.Data.DATA1, address)
            val contentResolver = appContext.contentResolver
            val mod = contentResolver.update(
                    ContactsContract.Data.CONTENT_URI,
                    values,
                    ContactsContract.Data.RAW_CONTACT_ID + "='" + contactId + "' AND "
                            + ContactsContract.Data.MIMETYPE + "= '"
                            + mimetypeStellarAddress + "'", null)

            return if (mod == 0) {
                values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                values.put(ContactsContract.Data.MIMETYPE, mimetypeStellarAddress)
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

    /**
     *  Create contact with stellar address
     *  @return the long that represents the contactId otherwise -1 when the create operation has failed
     */
    fun createContact(name : String, stellarAddress : String) : Long
    {
//        val values = ContentValues()
//        values.put(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, name)
//        values.put(ContactsContract.RawContacts.DISPLAY_NAME_ALTERNATIVE, name)
//        values.put(ContactsContract.Data.RAW_CONTACT_ID, name)
//        val contactId = ContentUris.parseId(appContext.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values))
//        if (contactId != -1L) {
//            updateContact(contactId, stellarAddress)
//        }
//        return contactId

        val ops = ArrayList<ContentProviderOperation>()
        ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null).build())

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                        0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build())

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                        0)
                .withValue(ContactsContract.Data.MIMETYPE, mimetypeStellarAddress)
                .withValue(ContactsContract.Data.DATA1, stellarAddress).build())

        val res = appContext.contentResolver.applyBatch(
                ContactsContract.AUTHORITY, ops)
        if (res.size > 1) {
            return ContentUris.parseId(res.get(0).uri)
        }
        return -1
    }
}