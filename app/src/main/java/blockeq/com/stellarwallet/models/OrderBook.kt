package blockeq.com.stellarwallet.models

import java.util.*

open class OrderBook(var id: Int? = null, var date: Date? = null, var fromPrice: Float? = null,
                var toAmount: Float? = null, var fromValue: Float? = null, var type: OrderBookAdapterTypes)