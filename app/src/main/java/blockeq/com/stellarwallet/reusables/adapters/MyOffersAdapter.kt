package blockeq.com.stellarwallet.reusables.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.reusables.models.MyOffer
import kotlinx.android.synthetic.main.row_my_offers.view.*

class MyOffersAdapter(private val myOffersList: MutableList<MyOffer>, private val context: Context?)
    : RecyclerView.Adapter<MyOffersAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return myOffersList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_my_offers, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val myOffer = myOffersList.get(position)
        holder.description.text = context?.getString(R.string.rowDescription, myOffer.amountFrom,
                myOffer.currencyFrom.code, myOffer.amountTo, myOffer.currencyTo.code,
                (myOffer.amountTo / myOffer.amountFrom))
        holder.delete.setOnClickListener {
//            onDeleteConfirmationDialog()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.description
        val delete: ImageButton = view.delete
    }
}