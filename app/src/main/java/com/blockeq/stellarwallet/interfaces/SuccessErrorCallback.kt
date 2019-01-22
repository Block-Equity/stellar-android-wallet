package com.blockeq.stellarwallet.interfaces

import com.blockeq.stellarwallet.models.HorizonException

interface SuccessErrorCallback {
    fun onSuccess()
    fun onError(error: HorizonException)
}