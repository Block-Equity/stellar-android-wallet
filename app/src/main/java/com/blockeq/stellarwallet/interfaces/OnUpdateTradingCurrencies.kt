package com.blockeq.stellarwallet.interfaces

import com.blockeq.stellarwallet.models.SelectionModel

interface OnUpdateTradingCurrencies {
   fun updateTradingCurrencies(currencyCodeFrom: SelectionModel, currencyCodeTo: SelectionModel)
}
