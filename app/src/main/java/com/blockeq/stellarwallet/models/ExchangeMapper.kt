package com.blockeq.stellarwallet.models

import com.blockeq.stellarwallet.mvvm.exchange.ExchangeEntity

class ExchangeMapper {
    companion object {
        fun toExchangeEntities(exchanges : List<ExchangeApiModel>): List<ExchangeEntity> {
            return exchanges.map { toExchangeEntity(it) }
        }

        fun toExchangeEntity(exchange : ExchangeApiModel) : ExchangeEntity {
            return ExchangeEntity(exchange.name, exchange.address, exchange.memo)
        }
    }
}
