package com.blockeq.stellarwallet.interfaces

import com.blockeq.stellarwallet.models.SelectionModel

interface OnTradeCurrenciesChanged {
    fun onCurrencyChange(selling: SelectionModel, buying: SelectionModel)
}