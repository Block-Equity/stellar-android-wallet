package com.blockeq.stellarwallet.models

import com.blockeq.stellarwallet.interfaces.StellarAccount
import org.stellar.sdk.responses.AccountResponse

data class StellarAccountImpl(private val accountResponse: AccountResponse) : StellarAccount {
    override fun getAccountId() : String {
        return accountResponse.accountId
    }

    override  fun getInflationDestination() : String? {
        return accountResponse.inflationDestination
    }

    override  fun getSequenceNumber() : Long {
        return accountResponse.sequenceNumber
    }

    override fun getSubEntryCount() : Int? {
        return accountResponse.subentryCount
    }

    override fun getAccountResponse(): AccountResponse {
        return accountResponse
    }
}
