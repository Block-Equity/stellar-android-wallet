package com.blockeq.stellarwallet.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.models.OrderBook
import com.blockeq.stellarwallet.models.OrderBookAdapterTypes
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler
import kotlinx.android.synthetic.main.row_order_books.view.*
import kotlinx.android.synthetic.main.row_order_books_header.view.*
import kotlinx.android.synthetic.main.row_order_books_subheader.view.*
import kotlinx.android.synthetic.main.row_order_books_title.view.*
import java.lang.IllegalStateException
import java.text.DecimalFormat

class OrderBooksAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyHeaderHandler {
    override fun getAdapterData(): MutableList<*>? {
        return orderBooksList
    }

    private var orderBooksList: MutableList<OrderBook>? = null
    private var currencyCodeTo: String? = null
    private var currencyCodeFrom: String? = null

    fun setCurrencies(currencyCodeTo: String, currencyCodeFrom: String) {
        this.currencyCodeTo = currencyCodeTo
        this.currencyCodeFrom = currencyCodeFrom
    }

    fun setOrderBookList(list: MutableList<OrderBook>) {
        orderBooksList = list
    }

    override fun getItemViewType(position: Int): Int {
        orderBooksList?.let {
            return it[position].type.value
        }
        return 0
    }

    override fun getItemCount(): Int {
        orderBooksList?.let {
            return it.size
        }
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            OrderBookAdapterTypes.TITLE.value ->
                TitleViewHolder(LayoutInflater.from(context).inflate(R.layout.row_order_books_title, parent, false))
            OrderBookAdapterTypes.BUY_HEADER.value, OrderBookAdapterTypes.SELL_HEADER.value -> {
                val viewHolder = HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.row_order_books_header, parent, false))
                if (viewType == OrderBookAdapterTypes.BUY_HEADER.value) {
                    viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.buySelectorBg))
                } else {
                     viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.sellSelectorBg))
                }
                viewHolder
            }
            OrderBookAdapterTypes.SUBHEADER.value ->
                SubHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.row_order_books_subheader, parent, false))
            OrderBookAdapterTypes.ITEM.value ->
                ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.row_order_books, parent, false))
            OrderBookAdapterTypes.EMPTY.value ->
                EmptyViewHolder(LayoutInflater.from(context).inflate(R.layout.row_order_books_empty, parent, false))
            else -> {
                throw IllegalStateException("unknown view type {$viewType}")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        orderBooksList?.let {
            val orderBook = it[position]
            when(holder){
                is TitleViewHolder -> holder.title.text = context.getString(R.string.orderBooksTitle, currencyCodeTo, currencyCodeFrom)
                is HeaderViewHolder -> {
                    if (orderBook.type == OrderBookAdapterTypes.BUY_HEADER) {
                        holder.header.text = context.getText(R.string.buyOffers)
                    } else if (orderBook.type == OrderBookAdapterTypes.SELL_HEADER) {
                        holder.header.text = context.getText(R.string.sellOffers)
                    }
                }
                is SubHeaderViewHolder -> {
                    holder.currencyFrom.text = context.getString(R.string.offerPrice, currencyCodeFrom)
                    holder.amountTo.text = context.getString(R.string.offerAmount, currencyCodeTo)
                    holder.amountFrom.text = context.getString(R.string.offerValue, currencyCodeFrom)
                }
                is ItemViewHolder -> {
                    val format = DecimalFormat("0.#######")
                    holder.fromPrice.text = format.format(orderBook.fromPrice)
                    holder.toAmount.text =format.format(orderBook.toAmount)
                    holder.fromValue.text = format.format(orderBook.fromValue)
                }
                is EmptyViewHolder -> {
                    // nothing
                }
            }
        }
    }

    class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val header: TextView = view.header
    }

    class SubHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val currencyFrom: TextView = view.currencyFrom
        val amountTo: TextView = view.amountTo
        val amountFrom: TextView = view.amountFrom
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fromPrice: TextView = view.fromPrice
        val toAmount: TextView = view.toAmount
        val fromValue: TextView = view.fromValue
    }

    class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}