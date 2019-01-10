package com.blockeq.stellarwallet.interfaces

import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.models.ContactsResult

interface ContactsRepository {
    enum class ContactOperationStatus {
        INSERTED,
        UPDATED,
        FAILED
    }
    fun createContact(name : String, stellarAddress : String) : Long
    fun getContactsListLiveData(forceRefresh:Boolean = false) : MutableLiveData<ContactsResult>
    fun createOrUpdateContact(contactId:Long, address:String) : ContactOperationStatus
    fun getStellarAddress(contactId: Long): String?

}