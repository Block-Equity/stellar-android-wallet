package blockeq.com.stellarwallet.interfaces

import java.io.Serializable

interface OnTradeCurrenciesChange : Serializable {
    fun onCurrencyChange(currencyCodeFrom: String?, currencyCodeTo: String?)
}