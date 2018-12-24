package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.interfaces.OnPinLockCompleteListener
import com.blockeq.stellarwallet.utils.AccountUtils
import kotlinx.android.synthetic.main.activity_pin.*
import timber.log.Timber

class PinActivity : BaseActivity() {

    private var numAttempts = 0
    private val MAX_ATTEMPTS = 3
    private var PIN : String? = null
    private lateinit var appContext : Context

    companion object {
        private const val INTENT_ARG_MESSAGE: String = "INTENT_ARG_MESSAGE"
        private const val INTENT_ARG_PIN: String = "INTENT_ARG_PIN"
        private const val INTENT_ARG_PIN_RESULT: String = "INTENT_ARG_PIN_RESULT"

        /**
         * New Instance of Intent to launch a {@link PinActivity}
         * @param context the activityContext of the requestor
         * @param PIN pin to verified otherwise it will simple return the inserted pin.
         * @param message message to show on the top of the pinlock.
         */
        fun newInstance(context: Context, PIN : String?, message: String = context.getString(R.string.please_enter_your_pin)): Intent {
            val intent = Intent(context, PinActivity::class.java)
                intent.putExtra(INTENT_ARG_MESSAGE, message)
            if (PIN != null) {
                intent.putExtra(INTENT_ARG_PIN, PIN)
            }
            return intent
        }

        fun getPinFromIntent(intent:Intent) : String? {
            return intent.getStringExtra(INTENT_ARG_PIN_RESULT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        appContext = applicationContext

        pinLockView.setPinLockListener(object : OnPinLockCompleteListener() {
            override fun onComplete(pin: String?) {
                Timber.d("OnComplete")
                if (PIN == null || PIN == pin) {
                    val intent = Intent()
                    intent.putExtra(INTENT_ARG_PIN_RESULT, pin)
                    setResult(Activity.RESULT_OK, intent)
                    overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
                    finish()
                } else {
                    processIncorrectPin()
                }
            }
        })

        pinLockView.attachIndicatorDots(indicatorDots)

        if (!intent.hasExtra(INTENT_ARG_MESSAGE)) throw IllegalStateException("missing argument {$INTENT_ARG_MESSAGE}, did you use #newInstance(..)?")

        val message = intent.getStringExtra(INTENT_ARG_MESSAGE)
        customMessageTextView.text = message

        PIN = intent.getStringExtra(INTENT_ARG_PIN_RESULT)
    }

    //region User Interface

    private fun processIncorrectPin() {
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
    //endregion
}