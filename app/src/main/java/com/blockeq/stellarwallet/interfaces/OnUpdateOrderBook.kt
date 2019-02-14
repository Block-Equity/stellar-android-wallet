package com.blockeq.stellarwallet.interfaces

import com.blockeq.stellarwallet.models.SelectionModel
import org.stellar.sdk.responses.OrderBookResponse

interface OnUpdateOrderBook {
   fun updateTradingCurrencies(sellingModel: SelectionModel, buyingModel: SelectionModel)
   fun updateOrderBook(sellingCode: String, buyingCode: String, asks: Array<OrderBookResponse.Row>, bids: Array<OrderBookResponse.Row>)
   fun failedToUpdate()
}
