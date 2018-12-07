package com.blockeq.stellarwallet.interfaces

import com.blockeq.stellarwallet.models.SelectionModel
import org.stellar.sdk.responses.OrderBookResponse

interface OnUpdateOrderBook {
   fun updateTradingCurrencies(currencyCodeFrom: SelectionModel, currencyCodeTo: SelectionModel)
   fun updateOrderBook(codeFrom: String, codeTo: String, asks: Array<OrderBookResponse.Row>, bids: Array<OrderBookResponse.Row>)
}
