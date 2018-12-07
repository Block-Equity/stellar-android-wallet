package com.blockeq.stellarwallet.interfaces

import org.stellar.sdk.responses.OrderBookResponse

interface OnUpdateTradeTab {
   fun onLastOrderBookUpdated(asks: Array<OrderBookResponse.Row>, bids: Array<OrderBookResponse.Row>)
}
