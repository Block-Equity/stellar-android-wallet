package blockeq.com.stellarwallet.interfaces

import org.stellar.sdk.responses.effects.EffectResponse
import java.util.ArrayList

interface OnLoadEffects {
    fun onLoadEffects(result: ArrayList<EffectResponse>?)
}
