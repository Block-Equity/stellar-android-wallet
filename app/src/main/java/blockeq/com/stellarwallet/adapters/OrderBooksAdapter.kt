package blockeq.com.stellarwallet.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.models.OrderBook
import blockeq.com.stellarwallet.models.OrderBookAdapterTypes
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler
import kotlinx.android.synthetic.main.row_order_books.view.*
import kotlinx.android.synthetic.main.row_order_books_header.view.*
import kotlinx.android.synthetic.main.row_order_books_subheader.view.*
import kotlinx.android.synthetic.main.row_order_books_title.view.*

class OrderBooksAdapter(private val orderBooksList: MutableList<OrderBook>,
                        private var currencyCodeFrom: String?, private var currencyCodeTo: String?,
                        private val context: Context?)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyHeaderHandler {

    fun setCurrencies(currencyCodeFrom: String?, currencyCodeTo: String?) {
        this.currencyCodeTo = currencyCodeTo
        this.currencyCodeFrom = currencyCodeFrom
    }

    override fun getAdapterData(): MutableList<*> {
        return orderBooksList
    }

    override fun getItemViewType(position: Int): Int {
        return orderBooksList[position].type.value
    }

    override fun getItemCount(): Int {
        return orderBooksList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            OrderBookAdapterTypes.TITLE.value ->
                TitleViewHolder(LayoutInflater.from(context).inflate(R.layout.row_order_books_title, parent, false))
            OrderBookAdapterTypes.BUY_HEADER.value, OrderBookAdapterTypes.SELL_HEADER.value -> {
                val vh = HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.row_order_books_header, parent, false))
                if (viewType == OrderBookAdapterTypes.BUY_HEADER.value) {
                    if (context != null) {
                        vh.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.buySelectorBg))
                    }
                } else {
                    if (context != null) {
                        vh.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.sellSelectorBg))
                    }
                }
                vh
            }
            OrderBookAdapterTypes.SUBHEADER.value ->
                SubheaderViewHolder(LayoutInflater.from(context).inflate(R.layout.row_order_books_subheader, parent, false))
            else ->
                ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.row_order_books, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val orderBook = orderBooksList.get(position)
        if (holder is TitleViewHolder) {
            holder.title.text = context?.getString(R.string.orderBooksTitle, currencyCodeTo, currencyCodeFrom)
        } else if (holder is HeaderViewHolder) {
            if (orderBook.type == OrderBookAdapterTypes.BUY_HEADER) {
                holder.header.text = context?.getText(R.string.buyOffers)
            } else if (orderBook.type == OrderBookAdapterTypes.SELL_HEADER) {
                holder.header.text = context?.getText(R.string.sellOffers)
            }
        } else if (holder is SubheaderViewHolder) {
            holder.currencyFrom.text = context?.getString(R.string.offerPrice, currencyCodeFrom)
            holder.amountTo.text = context?.getString(R.string.offerAmount, currencyCodeTo)
            holder.amountFrom.text = context?.getString(R.string.offerValue, currencyCodeFrom)
        } else if (holder is ItemViewHolder) {
            holder.fromPrice.text = orderBook.fromPrice.toString()
            holder.toAmount.text = orderBook.toAmount.toString()
            holder.fromValue.text = orderBook.fromValue.toString()
        }
    }

    class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val header: TextView = view.header
    }

    class SubheaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val currencyFrom: TextView = view.currencyFrom
        val amountTo: TextView = view.amountTo
        val amountFrom: TextView = view.amountFrom
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fromPrice: TextView = view.fromPrice
        val toAmount: TextView = view.toAmount
        val fromValue: TextView = view.fromValue
    }
}