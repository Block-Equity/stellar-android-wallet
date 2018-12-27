package com.blockeq.stellarwallet.adapters

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.activities.EnterAddressActivity
import com.blockeq.stellarwallet.models.Contact

class ContactsAdapter(private val cursor: Cursor) : RecyclerView.Adapter<ContactViewHolder>() {
    private val nameColIdx: Int = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
    private val idColIdx: Int = cursor.getColumnIndex(ContactsContract.Contacts._ID)

    private lateinit var appContext : Context

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ContactViewHolder {

        val listItemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.contact_item, parent, false)
        appContext = parent.context.applicationContext
        return ContactViewHolder(listItemView)
    }

    private fun getStellarAddress(context: Context, contactId: Long): String? {
        val uri = ContactsContract.Data.CONTENT_URI

        val RAW_PROJECTION = arrayOf(ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1)
        val cursor = context.contentResolver.query(uri, RAW_PROJECTION,
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

    override fun onBindViewHolder(contactViewHolder: ContactViewHolder, pos: Int) {
        // Extract info from cursor
        cursor.moveToPosition(pos)
        val contactName = cursor.getString(nameColIdx)
        val contactId = cursor.getLong(idColIdx)

        var stellarAddress : String? = getStellarAddress(appContext, contactId)


        val profilePic = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)

        // Create contact model and bind to viewholder
        val contact = Contact(contactId, contactName, profilePic, stellarAddress)

        contactViewHolder.bind(contact)
    }

    override fun getItemCount(): Int {
        return cursor.count
    }
}