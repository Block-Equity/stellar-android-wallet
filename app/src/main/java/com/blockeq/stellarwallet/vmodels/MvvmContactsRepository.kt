package com.blockeq.stellarwallet.vmodels

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.ContactsContract.RawContacts
import com.blockeq.stellarwallet.models.Contact
import com.blockeq.stellarwallet.models.ContactsResult
import timber.log.Timber
import kotlin.concurrent.thread

@SuppressLint("StaticFieldLeak")
object MvvmContactsRepository {
    private lateinit var appContext : Context
    private const val mimetypeStellarAddress = "vnd.android.cursor.item/sellarAccount"
    var allContactsLiveData : MutableLiveData<ContactsResult> = MutableLiveData()
    private var stellarContactList : ArrayList<Contact> = ArrayList()
    private var contactsList : ArrayList<Contact> = ArrayList()

    operator fun invoke(): MvvmContactsRepository {
        throw IllegalStateException("not valid constructor, use " + MvvmContactsRepository::class.java.canonicalName + "(context)")
    }

    operator fun invoke(context: Context): MvvmContactsRepository {
        appContext = context.applicationContext
        return this
    }

    private fun removeDuplicates(list1: ArrayList<Contact>, list2:ArrayList<Contact>) : ArrayList<Contact> {
        val output : ArrayList<Contact>
        val smallerList : ArrayList<Contact>
        val longerList : ArrayList<Contact>
        if (list1.size > list2.size) {
            smallerList = list2
            longerList = list1
        } else {
            smallerList = list1
            longerList = list2
        }

        output = longerList
        smallerList.forEach {
            if (longerList.contains(it)) {
               output.remove(it)
            }
        }
        return output
    }

    private fun notifyLiveData(){
        allContactsLiveData.postValue(ContactsResult(stellarContactList, contactsList))
    }

    fun getContactsListLiveData(forceRefresh:Boolean = false) : MutableLiveData<ContactsResult> {
        Timber.d("start getContactsListLiveData")
        if (!forceRefresh && !contactsList.isEmpty()) {
            notifyLiveData()
        } else {
            thread {
                stellarContactList = parseContactCursor(getStellarContactsList(), true)
                val parsedList = parseContactCursor(getContactsList(), false)
                val list = removeDuplicates(parsedList, stellarContactList)
                if (!list.isEmpty() || !stellarContactList.isEmpty()) {
                    Handler(Looper.getMainLooper()).run {
                        contactsList = list
                        Timber.d("all parsed (${list.size})")
                        notifyLiveData()
                    }
                }
            }
        }
        return allContactsLiveData
    }

    private fun parseContactCursor(cursor : Cursor?, populateStellarAddress:Boolean = true) : ArrayList<Contact> {
        if (cursor != null) {
            val list : ArrayList<Contact> = ArrayList()
            while (cursor.moveToNext()) {
                val contact = toContact(cursor, populateStellarAddress)
                list.add(contact)
            }
            cursor.close()
            return list
        }
        return ArrayList()
    }


    /**
     * it will create and return a new cursor, the cursor will be not closed by {@link ContactsRepository}
     */
    private fun getStellarContactsList() : Cursor? {
        val uri = ContactsContract.Data.CONTENT_URI
        val RAW_PROJECTION = arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1)
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
        val RAW_PROJECTION = arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        return appContext.contentResolver.query(uri, RAW_PROJECTION,
                null, null, null)
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
    fun toContact(cursor:Cursor, populateStellarAddress: Boolean = true) : Contact {
        val nameColIdx: Int = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val idColIdx: Int = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        val contactName = cursor.getString(nameColIdx)
        val contactId = cursor.getLong(idColIdx)

        val profilePic = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)

        val contact = Contact(contactId, contactName, profilePic)

        if (populateStellarAddress) {
            contact.stellarAddress = getStellarAddress(contactId)
        }
        return contact
    }

    /**
     *  Create contact with stellar address
     *  @return the long that represents the contactId otherwise -1 when the create operation has failed
     */
    fun createContact(name : String, stellarAddress : String) : Long
    {
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
            return ContentUris.parseId(res[0].uri)
        }
        return -1
    }
}