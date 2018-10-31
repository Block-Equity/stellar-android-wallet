package blockeq.com.stellarwallet.flowcontrollers

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.activities.PinActivity
import blockeq.com.stellarwallet.models.PinViewState

object PinFlowController {

    const val OBJECT = "object"

    fun launchPinActivity(activity: FragmentActivity, pinViewState: PinViewState, isLoginLaunch : Boolean) {
        val intent = Intent(activity, PinActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable(OBJECT, pinViewState)
        intent.putExtras(bundle)
        if (isLoginLaunch) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        activity.startActivityForResult(intent, PinActivity.PIN_REQUEST_CODE)
        activity.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
    }
}
