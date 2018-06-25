package blockeq.com.stellarwallet.fragments

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.activities.PinActivity
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_settings, container, false)

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
        const val PIN_REQUEST_CODE = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pinTest.setOnClickListener {
            startActivityForResult(Intent(activity, PinActivity::class.java), PIN_REQUEST_CODE)
            activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIN_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> pinResult.setText(R.string.pin_correct)
                RESULT_CANCELED -> pinResult.setText(R.string.pin_canceled)
                else -> pinResult.setText(R.string.pin_incorrect)
            }
        }
    }
}