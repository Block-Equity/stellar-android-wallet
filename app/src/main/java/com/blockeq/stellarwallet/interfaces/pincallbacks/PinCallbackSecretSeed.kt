package com.blockeq.stellarwallet.interfaces.pincallbacks

abstract class PinCallbackSecretSeed : PinCallback() {
    abstract fun onSuccess(secretSeed : String)
}
