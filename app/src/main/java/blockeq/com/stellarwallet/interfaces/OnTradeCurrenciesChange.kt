package blockeq.com.stellarwallet.interfaces

interface OnTradeCurrenciesChange {
    fun onCurrencyChange(currencyCodeFrom: String?, currencyCodeTo: String?)
}