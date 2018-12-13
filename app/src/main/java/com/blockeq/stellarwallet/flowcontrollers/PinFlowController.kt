package com.blockeq.stellarwallet.flowcontrollers

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.activities.PinActivity
import com.blockeq.stellarwallet.models.PinViewState

object PinFlowController {

    const val OBJECT = "object"

    fun launchPinActivity(activity: FragmentActivity, pinViewState: PinViewState) {
        val intent = Intent(activity, PinActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable(OBJECT, pinViewState)
        intent.putExtras(bundle)

        activity.startActivityForResult(intent, PinActivity.PIN_REQUEST_CODE)
        activity.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
    }
}
