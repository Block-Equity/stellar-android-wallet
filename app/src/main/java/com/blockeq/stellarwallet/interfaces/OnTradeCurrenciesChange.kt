package com.blockeq.stellarwallet.interfaces

interface OnTradeCurrenciesChange {
    fun onCurrencyChange(currencyCodeFrom: String?, currencyCodeTo: String?)
}