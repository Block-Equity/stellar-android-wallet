package com.blockeq.stellarwallet.adapters

import android.content.Context
import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.vmodels.ContactsRepository

class ContactsAdapter(private val cursor: Cursor) : RecyclerView.Adapter<ContactViewHolder>() {

    private lateinit var appContext : Context

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ContactViewHolder {

        val listItemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.contact_item, parent, false)
        appContext = parent.context.applicationContext
        return ContactViewHolder(listItemView)
    }

    override fun onBindViewHolder(contactViewHolder: ContactViewHolder, pos: Int) {
        // Extract info from cursor
        cursor.moveToPosition(pos)
        val contact = ContactsRepository(appContext).toContact(cursor)
        contactViewHolder.bind(contact)
    }

    override fun getItemCount(): Int {
        return cursor.count
    }
}