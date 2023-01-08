package com.blockeq.stellarwallet.models

import com.blockeq.stellarwallet.interfaces.StellarAccount
import org.stellar.sdk.KeyPair
import org.stellar.sdk.responses.AccountResponse

open class BasicStellarAccount(private val accountId:String?, private val inflationDestination:String?,
                               private val sequenceNumber:Long, private val subEntryCount:Int?): StellarAccount {

    override fun getAccountResponse(): AccountResponse {
        return AccountResponse(KeyPair.fromAccountId(accountId).accountId, sequenceNumber)
    }

    override fun getAccountId(): String? {
        return accountId
    }

    override fun getInflationDestination(): String? {
        return inflationDestination
    }

    override fun getSequenceNumber(): Long {
        return sequenceNumber
    }

    override fun getSubEntryCount(): Int? {
        return subEntryCount
    }
}
