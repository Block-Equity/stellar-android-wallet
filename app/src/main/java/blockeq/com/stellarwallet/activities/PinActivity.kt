package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import blockeq.com.stellarwallet.R
import com.andrognito.pinlockview.PinLockListener
import kotlinx.android.synthetic.main.activity_pin.*

class PinActivity : AppCompatActivity(), PinLockListener {

    // Correct PIN example. later on, this would be fetched from the Local Storage
    companion object {
        const val CORRECT_PIN = "1234"
        const val RESULT_FAIL = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        pinLockView.setPinLockListener(this)
        pinLockView.attachIndicatorDots(indicatorDots)
    }

    override fun onEmpty() {
    }

    override fun onComplete(pin: String?) {
        if (pin != CORRECT_PIN) {
            showWrongPinDots(true)
            val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
            shakeAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    showWrongPinDots(false)
                    pinLockView.resetPinLockView()
                    setResult(RESULT_FAIL)
                    finish()
                    overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
                }
            })
            wrongPinDots.startAnimation(shakeAnimation)
        } else {
            setResult(RESULT_OK)
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
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