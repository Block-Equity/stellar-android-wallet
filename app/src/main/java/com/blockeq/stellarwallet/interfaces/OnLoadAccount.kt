package com.blockeq.stellarwallet.interfaces

import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse

interface OnLoadAccount {
    fun onLoadAccount(result: AccountResponse?)
    fun onError(error: ErrorResponse)
}