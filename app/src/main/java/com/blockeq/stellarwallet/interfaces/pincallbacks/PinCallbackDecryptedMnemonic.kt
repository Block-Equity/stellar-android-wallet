package com.blockeq.stellarwallet.interfaces.pincallbacks

abstract class PinCallbackDecryptedMnemonic : PinCallback() {
    abstract fun onSuccess(decryptedMnemonic : String)
}
