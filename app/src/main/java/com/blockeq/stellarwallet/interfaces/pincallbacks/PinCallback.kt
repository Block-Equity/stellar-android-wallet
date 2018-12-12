package com.blockeq.stellarwallet.interfaces.pincallbacks

import java.lang.Exception

open class PinCallback {
    fun onError() {
        throw(Exception("Please override this method."))
    }
}
