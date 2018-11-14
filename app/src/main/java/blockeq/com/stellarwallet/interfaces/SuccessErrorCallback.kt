package blockeq.com.stellarwallet.interfaces

import blockeq.com.stellarwallet.models.HorizonException

interface SuccessErrorCallback {
    fun onSuccess()
    fun onError(error: HorizonException)
}