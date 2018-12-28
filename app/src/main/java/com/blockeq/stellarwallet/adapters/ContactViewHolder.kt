package com.blockeq.stellarwallet.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.activities.EnterAddressActivity
import com.blockeq.stellarwallet.activities.SendActivity
import com.blockeq.stellarwallet.models.Contact
import com.squareup.picasso.Picasso

/**
 * Contains a Contact List Item
 */
class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val image: ImageView = itemView.findViewById<View>(R.id.rounded_iv_profile) as ImageView
    private val label: TextView = itemView.findViewById<View>(R.id.tv_label) as TextView
    private val button: TextView = itemView.findViewById<View>(R.id.button_add_address) as TextView

    private var mBoundContact: Contact? = null // Can be null

    fun bind(contact: Contact) {
        mBoundContact = contact
        label.text = contact.name + " " + contact.id
        Picasso.get().load(contact.profilePic).into(image)
        if (!contact.stellarAddress.isNullOrEmpty()) {
           button.setText("SEND FUNDS")
        } else {
            button.setText("ADD ADDRESS")
        }

        itemView.setOnClickListener {
            val context = it.context
            if (mBoundContact != null) {
                context.startActivity(EnterAddressActivity.updateContact(context, contact.id))
            }
        }

        button.setOnClickListener {
            val context = it.context
            //TODO refactor this with types
            if (button.text == "ADD ADDRESS") {
                context.startActivity(EnterAddressActivity.updateContact(context, contact.id))
            } else {
                context.startActivity(SendActivity.newIntent(context, contact.stellarAddress!!))
            }
        }
    }
}
