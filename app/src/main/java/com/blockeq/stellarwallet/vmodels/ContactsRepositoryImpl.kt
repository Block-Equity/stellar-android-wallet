package com.blockeq.stellarwallet.vmodels

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.ContactsContract.RawContacts
import com.blockeq.stellarwallet.interfaces.ContactsRepository
import com.blockeq.stellarwallet.interfaces.ContactsRepository.ContactOperationStatus
import com.blockeq.stellarwallet.models.Contact
import com.blockeq.stellarwallet.models.ContactsResult
import timber.log.Timber
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.concurrent.thread

@SuppressLint("StaticFieldLeak")
object ContactsRepositoryImpl : ContactsRepository {
    private lateinit var appContext : Context
    private const val mimeTypeStellarAddress = "vnd.android.cursor.item/stellarAccount"
    private var contactsLiveData : MutableLiveData<ContactsResult> = MutableLiveData()
    private var stellarContactList : ArrayList<Contact> = ArrayList()
    private var contactsList : ArrayList<Contact> = ArrayList()

    operator fun invoke(): ContactsRepositoryImpl {
        throw IllegalStateException("not valid constructor, use " + ContactsRepositoryImpl::class.java.canonicalName + "(context)")
    }

    operator fun invoke(context: Context): ContactsRepositoryImpl {
        appContext = context.applicationContext
        return this
    }

    //region Public Interface

    /**
     *  Create contact with stellar address
     *  @return the long that represents the contactId otherwise -1 when the create operation has failed
     */
    override fun createContact(name : String, stellarAddress : String) : Long
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
                .withValue(ContactsContract.Data.MIMETYPE, mimeTypeStellarAddress)
                .withValue(ContactsContract.Data.DATA1, stellarAddress).build())

        val res = appContext.contentResolver.applyBatch(
                ContactsContract.AUTHORITY, ops)
        if (res.size > 1) {
            //TODO: this is too heavy, there is enough info to update the local list and notifyLiveData()
            refreshContacts()
            return ContentUris.parseId(res[0].uri)
        }
        return -1
    }

    override fun createOrUpdateStellarAddress(name:String, address:String) : ContactOperationStatus {
        var operation: ContactOperationStatus
//        val encodedName = URLEncoder.encode(name, "utf-8")

        try {
            val values = ContentValues()
            values.put(ContactsContract.Data.DATA1, address)
            val contentResolver = appContext.contentResolver
            val rowsUpdated = contentResolver.update(ContactsContract.Data.CONTENT_URI, values,
                    "${ContactsContract.Data.DATA2} = '$name' AND ${ContactsContract.Data.MIMETYPE} = '$mimeTypeStellarAddress'", null)

            if (rowsUpdated == 0) {

                val resolver = appContext.contentResolver
                val ops = java.util.ArrayList<ContentProviderOperation>()

                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
                                "blockeq")
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "default")
                        .build())

                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, mimeTypeStellarAddress)
                        .withValue(ContactsContract.Data.DATA1, address)
                        .withValue(ContactsContract.Data.DATA2, name)
                        .build())

                operation = try {
                    val contentProviderResult = resolver.applyBatch(ContactsContract.AUTHORITY, ops)
                    ContactOperationStatus.INSERTED
                } catch (e: Exception) {
                    e.printStackTrace()
                    ContactOperationStatus.FAILED
                }
            } else {
                operation = ContactOperationStatus.UPDATED
            }
        } catch (e: Exception) {
            operation = ContactOperationStatus.FAILED
        }
        //TODO: this is too heavy, there is enough info to update the local list and notifyLiveData()
        refreshContacts()
        return operation
    }

    override fun getStellarAddress(contactId: Long): String? {
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1)
        val cursor = appContext.contentResolver.query(uri, projection,
                "${ContactsContract.Data.CONTACT_ID} =? AND ${ContactsContract.Data.MIMETYPE} =?",
                arrayOf(contactId.toString(), mimeTypeStellarAddress), null)

        var stellarAddress : String? = null
        if (cursor !== null) {
            //TODO: check for duplicated rows
            while (cursor.moveToNext()) {
                stellarAddress = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1))
            }
            cursor.close()
        }
        return stellarAddress
    }

    private fun addCallerIsSyncAdapterParameter(uri: Uri,
                                                isSyncOperation: Boolean): Uri {
        return if (isSyncOperation) {
            uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                            "true").build()
        } else uri
    }

    override fun getContactsListLiveData(forceRefresh:Boolean) : MutableLiveData<ContactsResult> {
        Timber.d("start getContactsListLiveData")
        if (!forceRefresh && !contactsList.isEmpty()) {
            notifyLiveData()
        } else {
            refreshContacts()
        }
        return contactsLiveData
    }

    //endregion Public Interface

    //region Private Methods

    private fun refreshContacts2() {
        thread {
            val stellarAddresses = getStellarContacts()
            val contactList = parseContactCursor(getContactsList(), false)

            val stellarList : ArrayList<Contact> = arrayListOf()
            stellarAddresses.forEach {
                val found = contactList.find { that -> that.name == it.name }
                contactList.remove(found)

                val contact = Contact(0, it.name, null )
                contact.stellarAddress = it.address
                stellarList.add(contact)
            }


            Handler(Looper.getMainLooper()).run {
                contactsList = contactList
                stellarContactList = stellarList
                Timber.d("all parsed (${contactList.size})")
                notifyLiveData()
            }
        }
    }

    private fun refreshContacts() {
        refreshContacts2()
        return

        thread {
            val stellarList = getStellarContacts()
            stellarContactList = parseContactCursor(getStellarContactsList(), true)
            val parsedList = parseContactCursor(getContactsList(), false)
            val list = removeDuplicates(parsedList, stellarContactList)
            Handler(Looper.getMainLooper()).run {
                contactsList = list
                Timber.d("all parsed (${list.size})")
                notifyLiveData()
            }
        }
    }

    private fun removeDuplicates(contacts: ArrayList<Contact>, stellarContacts:ArrayList<Contact>) : ArrayList<Contact> {
        val output : ArrayList<Contact> = contacts
        stellarContacts.forEach {
            if (contacts.contains(it)) {
                output.remove(it)
            }
        }

        return output
    }

    private fun notifyLiveData() {
        Timber.d("observer notifyLiveData ${stellarContactList.size}")
        contactsLiveData.postValue(ContactsResult(stellarContactList, contactsList))
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
        val projection = arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1)
        return  appContext.contentResolver.query(uri, projection,
                ContactsContract.Data.MIMETYPE + " =? AND ${ContactsContract.Data.DATA1} != ''",
                arrayOf(mimeTypeStellarAddress), null)
    }


    data class StellarContact(var name:String, var address: String)

    /**
     * it will create and return a new cursor, the cursor will be not closed by {@link ContactsRepository}
     */
    private fun getStellarContacts() : List<StellarContact> {
        val list: ArrayList<StellarContact> = arrayListOf()

        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1, ContactsContract.Data.DATA2)
        val cursor = appContext.contentResolver.query(uri, projection,
                ContactsContract.Data.MIMETYPE + " =? AND ${ContactsContract.Data.DATA1} != ''",
                arrayOf(mimeTypeStellarAddress), null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val address = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1))
                //should not be null, but during development I messed up some phones
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA2)) ?: continue

                list.add(StellarContact(name, address))
            }
            cursor.close()
        }

        return list
    }

    /**
     * it will create and return a new cursor, the cursor will be not closed by {@link ContactsRepository}
     */
    private fun getContactsList() : Cursor? {
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        return appContext.contentResolver.query(uri, projection,
                null, null, null)
    }

    /**
     * Move the cursor before calling this.
     */
    private fun toContact(cursor:Cursor, populateStellarAddress: Boolean = true) : Contact {
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
    //endregion Private Methods
}