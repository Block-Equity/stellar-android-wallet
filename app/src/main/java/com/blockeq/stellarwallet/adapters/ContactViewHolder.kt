package com.blockeq.stellarwallet.adapters

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.activities.StellarAddressActivity
import com.blockeq.stellarwallet.activities.SendActivity
import com.blockeq.stellarwallet.models.Contact
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

/**
 * Contains a Contact List Item
 */
class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val image: CircleImageView = itemView.findViewById<View>(R.id.rounded_iv_profile) as CircleImageView
    private val label: TextView = itemView.findViewById<View>(R.id.tv_label) as TextView
    private val button: TextView = itemView.findViewById<View>(R.id.button_add_address) as TextView

    private var mBoundContact: Contact? = null // Can be null
    val colors: IntArray = intArrayOf(R.color.terracotta, R.color.lightBlue, R.color.puce, R.color.mantis, R.color.purple, R.color.pink)
    fun bind(contact: Contact) {
        mBoundContact = contact
        val appContext = label.context.applicationContext
        label.text = contact.name
        if (contact.profilePic != null) {
            Picasso.get().load(contact.profilePic).into(image)
        } else {
            image.circleBackgroundColor = ContextCompat.getColor(appContext, colors[getRandomNumber(0, colors.size)])
        }
        val stellarAddress = contact.stellarAddress
        if (stellarAddress == null) {
            button.text = "Add Address"
            button.setBackgroundColor(ContextCompat.getColor(appContext, R.color.blue1))
        } else {
            button.text = "Send Payment"
            button.setBackgroundColor(ContextCompat.getColor(appContext, R.color.mantis))
        }

        button.setOnClickListener {
            val context = it.context
            stellarAddress?.let { that ->
                context.startActivity(SendActivity.newIntent(context, that))
            } ?: run {
                context.startActivity(StellarAddressActivity.updateContact(context, contact.id))
            }
        }

        itemView.setOnClickListener {
            val context = it.context
            if (mBoundContact != null) {
                context.startActivity(StellarAddressActivity.updateContact(context, contact.id))
            }
        }
    }


    private fun getRandomNumber(min: Int, max: Int): Int {
        return Random().nextInt(max - min + 1) + min
    }


}
