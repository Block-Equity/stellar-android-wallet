package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import blockeq.com.stellarwallet.R
import com.andrognito.pinlockview.PinLockListener
import kotlinx.android.synthetic.main.activity_pin.*
import android.view.animation.Animation



class PinActivity : AppCompatActivity(), PinLockListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        pinLockView.setPinLockListener(this)
        pinLockView.attachIndicatorDots(indicatorDots)
    }

    override fun onEmpty() {
    }

    override fun onComplete(pin: String?) {
        if (pin != "1234") {
            indicatorDots.visibility = View.GONE
            wrongPinDots.visibility = View.VISIBLE
            val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
            shakeAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    wrongPinDots.visibility = View.GONE
                    indicatorDots.visibility = View.VISIBLE
                    pinLockView.resetPinLockView()
                }
            })
            wrongPinDots.startAnimation(shakeAnimation)
        } else {

        }
    }

    override fun onPinChange(pinLength: Int, intermediatePin: String?) {
    }
}