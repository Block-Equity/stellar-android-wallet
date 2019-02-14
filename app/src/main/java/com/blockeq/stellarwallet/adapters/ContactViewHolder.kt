package com.blockeq.stellarwallet.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.activities.SendActivity
import com.blockeq.stellarwallet.activities.StellarAddressActivity
import com.blockeq.stellarwallet.models.Contact
import com.github.abdularis.civ.CircleImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

/**
 * Contains a Contact List Item
 */
class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val image: CircleImageView = itemView.findViewById<View>(R.id.rounded_iv_profile) as CircleImageView
    private val label: TextView = itemView.findViewById<View>(R.id.tv_label) as TextView
    private val letter: TextView = itemView.findViewById<View>(R.id.contact_letter) as TextView
    private val button: TextView = itemView.findViewById<View>(R.id.button_add_address) as TextView

    private var mBoundContact: Contact? = null // Can be null
    private val colors: IntArray = intArrayOf(R.color.terracotta, R.color.lightBlue, R.color.puce, R.color.mantis, R.color.brown,
            R.color.purple, R.color.pink, R.color.lightBlue, R.color.paleSky, R.color.apricot, R.color.cornflowerBlue)

    fun bind(contact: Contact) {
        mBoundContact = contact
        val appContext = label.context.applicationContext
        label.text = contact.name
        image.visibility = View.INVISIBLE
        Picasso.get().load(contact.profilePic).into(image, object : Callback {
            override fun onSuccess() {
                letter.text = null
                image.visibility = View.VISIBLE
            }

            override fun onError(e: Exception?) {
                if (contact.name.length > 1) {
                    val firstLetter = contact.name[0]
                    letter.text = firstLetter.toString()
                    val width = appContext.resources.getDimension(R.dimen.badge_width).toInt()
                    val height = appContext.resources.getDimension(R.dimen.badge_height).toInt()
                    image.setImageBitmap(createImage(width, height, getColor(appContext, firstLetter)))
                    image.visibility = View.VISIBLE
                } else {
                    letter.text = null
                }
            }
        })

        val stellarAddress = contact.stellarAddress
        if (stellarAddress.isNullOrBlank()) {
            button.text = appContext.getString(R.string.add_stellar_address)
            button.setBackgroundColor(ContextCompat.getColor(appContext, R.color.blue1))
        } else {
            button.text = appContext.getString(R.string.send_payment)
            button.setBackgroundColor(ContextCompat.getColor(appContext, R.color.mantis))
        }

        button.setOnClickListener {
            val context = it.context
            stellarAddress?.let { that ->
                context.startActivity(SendActivity.newIntent(context, that))
            } ?: run {
                context.startActivity(StellarAddressActivity.updateContact(context, contact))
            }
        }

        itemView.setOnClickListener {
            val context = it.context
            if (mBoundContact != null) {
                context.startActivity(StellarAddressActivity.updateContact(context, contact))
            }
        }
    }

    fun getColor(context : Context, char : Char) : Int {
        val index = char.toInt() % colors.size
        return ContextCompat.getColor(context, colors[index])
    }

    fun createImage(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = color
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }
}
