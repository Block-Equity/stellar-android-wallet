package com.blockeq.stellarwallet.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.models.Contact

class ContactsAdapter(private val contacts: ArrayList<Contact>) : RecyclerView.Adapter<ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ContactViewHolder {

        val listItemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(listItemView)
    }

    override fun onBindViewHolder(contactViewHolder: ContactViewHolder, pos: Int) {
        // Extract info from cursor
        contactViewHolder.bind(contacts[pos])
    }

    override fun getItemCount(): Int {
        return contacts.size
    }
}