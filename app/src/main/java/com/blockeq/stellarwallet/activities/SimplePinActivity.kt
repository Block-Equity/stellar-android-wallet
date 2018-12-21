package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.andrognito.pinlockview.PinLockListener
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.utils.AccountUtils
import kotlinx.android.synthetic.main.activity_pin.*
import timber.log.Timber
import java.lang.IllegalStateException

class SimplePinActivity : BaseActivity(), PinLockListener {

    private var numAttempts = 0
    private val MAX_ATTEMPTS = 3
    private lateinit var appContext : Context

    companion object {
        private const val INTENT_ARG_MESSAGE: String = "INTENT_ARG_MESSAGE"
        private const val INTENT_ARG_PIN: String = "INTENT_ARG_PIN"

        fun newInstance(context: Context, message: String = context.getString(R.string.please_enter_your_pin)): Intent {
            val intent = Intent(context, SimplePinActivity::class.java)
            intent.putExtra(INTENT_ARG_MESSAGE, message)
            return intent
        }

        fun getPinFromIntent(intent:Intent) : String? {
            return intent.getStringExtra(INTENT_ARG_PIN)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        appContext = applicationContext

        pinLockView.setPinLockListener(this)
        pinLockView.attachIndicatorDots(indicatorDots)

        if (!intent.hasExtra(INTENT_ARG_MESSAGE)) throw IllegalStateException("missing argument {$INTENT_ARG_MESSAGE}, did you use #newInstance(..)?")
        val message = intent.getStringExtra(INTENT_ARG_MESSAGE)
        customMessageTextView.text = message
    }

    override fun onEmpty() {}

    override fun onComplete(pin: String) {
        Timber.d("OnComplete")
        val handler = Handler()
        val runnableCode = Runnable {
            val foundMasterKey = AccountUtils.getPinMasterKey(appContext, pin)
            if (foundMasterKey != null) {
                val intent = Intent()
                intent.putExtra(INTENT_ARG_PIN, pin)
                setResult(Activity.RESULT_OK, intent)
                finishPinActivity()
            } else {
                onIncorrectPin()
            }
        }
        //TODO move the work to non ui Thread, this delay is to not freeze the animation of the last pin dot.
        handler.postDelayed(runnableCode, 200)
    }

    //region User Interface

    override fun onPinChange(pinLength: Int, intermediatePin: String?) {}

    private fun onIncorrectPin() {
        showWrongPinDots(true)
        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
        shakeAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                showWrongPinDots(false)
                pinLockView.resetPinLockView()
                numAttempts++
                customMessageTextView.text = resources.getQuantityString(R.plurals.attempts_template,
                        MAX_ATTEMPTS - numAttempts, MAX_ATTEMPTS - numAttempts)
                if (numAttempts == MAX_ATTEMPTS) {
                    wipeAndRestart()
                }
            }
        })
        wrongPinDots.startAnimation(shakeAnimation)
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

    private fun wipeAndRestart() {
        AccountUtils.wipe(this)
        val intent = Intent(this, LaunchActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun finishPinActivity() {
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
        finish()
    }
    //endregion
}