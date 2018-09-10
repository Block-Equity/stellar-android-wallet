package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.R.id.*
import com.andrognito.pinlockview.PinLockListener
import kotlinx.android.synthetic.main.activity_pin.*

class PinActivity : AppCompatActivity(), PinLockListener {

    // Correct PIN example. later on, this would be fetched from the Local Storage
    companion object {
        const val PIN_REQUEST_CODE = 0
        const val CORRECT_PIN = "1234"
        const val RESULT_FAIL = 2
        const val RESULT_CONFIRM_PIN = 3
    }

    private var needConfirm = false
    private var PIN : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        pinLockView.setPinLockListener(this)
        pinLockView.attachIndicatorDots(indicatorDots)

        val message = intent.getStringExtra("message")
        needConfirm = intent.getBooleanExtra("need_confirm", false)
        PIN = intent.getStringExtra("pin")

        // Check if keychain contains a pin
        if (!message.isNullOrEmpty()) {
            tv_custom_message.text = message
        }
    }

    override fun onEmpty() {
    }

    override fun onComplete(pin: String?) {
        when {
            needConfirm -> {
                setResult(RESULT_CONFIRM_PIN, Intent().putExtra("pin", pin))
                finish()
            }
            pin != PIN -> {
                showWrongPinDots(true)
                val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                shakeAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(arg0: Animation) {}
                    override fun onAnimationRepeat(arg0: Animation) {}
                    override fun onAnimationEnd(arg0: Animation) {
                        showWrongPinDots(false)
                        pinLockView.resetPinLockView()
                    }
                })
                wrongPinDots.startAnimation(shakeAnimation)
            }
            else -> {
                setResult(RESULT_OK, Intent().putExtra("pin", pin))
                finish()
                overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
            }
        }
    }

    override fun onPinChange(pinLength: Int, intermediatePin: String?) {
    }

    private fun showWrongPinDots(show: Boolean) {
        indicatorDots.visibility = if (show) View.GONE else View.VISIBLE
        wrongPinDots.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
    }
}