package com.blockeq.stellarwallet.mvvm.effects.remote

import org.stellar.sdk.responses.effects.EffectResponse
import java.util.ArrayList

interface OnLoadEffects {
    fun onLoadEffects(result: ArrayList<EffectResponse>?)
    fun onError(errorMessage:String)
}
