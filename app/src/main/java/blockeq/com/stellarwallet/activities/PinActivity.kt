package blockeq.com.stellarwallet.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.encryption.CipherWrapper
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState
import com.andrognito.pinlockview.PinLockListener
import kotlinx.android.synthetic.main.activity_pin.*

class PinActivity : AppCompatActivity(), PinLockListener {

    // Correct PIN example. later on, this would be fetched from the Local Storage
    companion object {
        const val PIN_REQUEST_CODE = 0
        const val RESULT_FAIL = 2
    }

    private var needConfirm = true
    private var PIN : String? = null
    private var phrase : String? = null
    private var pinViewState: PinViewState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        pinLockView.setPinLockListener(this)
        pinLockView.attachIndicatorDots(indicatorDots)

        pinViewState = getPinState()
        phrase = pinViewState!!.phrase
        val message = pinViewState!!.message
        PIN = pinViewState!!.pin

        // Check if keychain contains a pin
        if (!message.isNullOrEmpty()) {
            tv_custom_message.text = message
        }
    }

    override fun onEmpty() {
    }

    override fun onComplete(pin: String) {
        when {
            (pinViewState!!.type == PinType.CREATE && needConfirm) -> {
                PIN = pin
                pinLockView.resetPinLockView()

                tv_custom_message.text = getString(R.string.please_reenter_your_pin)
                needConfirm = false
            }
            pin != PIN -> onIncorrectPin()
            pinViewState!!.type == PinType.CREATE -> {
                setResult(Activity.RESULT_OK)

                val keyStoreWrapper = KeyStoreWrapper(applicationContext)
                keyStoreWrapper.createAndroidKeyStoreAsymmetricKey(pin)

                val masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin)
                val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")

                val encryptedData = cipherWrapper.encrypt(pinViewState!!.phrase, masterKey?.public)

                // Wipe the mnemonic
                pinViewState!!.phrase = ""

                WalletApplication.localStore!![getString(R.string.encrypted_mnemonic)] = encryptedData

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            else -> {
                setResult(Activity.RESULT_OK)
                finishActivity()
            }

        }
    }

    override fun onPinChange(pinLength: Int, intermediatePin: String?) {
    }

    private fun onIncorrectPin() {
        showWrongPinDots(true)
        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
        shakeAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                showWrongPinDots(false)
                pinLockView.resetPinLockView()
                if (pinViewState!!.type != PinType.CREATE) {
                    setResult(RESULT_FAIL)
                    finishActivity()
                }
            }
        })
        wrongPinDots.startAnimation(shakeAnimation)
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
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

    private fun getPinState(): PinViewState {
        val intent = intent
        val bundle = intent.extras

        val state = bundle.getParcelable<PinViewState>(PinFlowController.OBJECT)
        return state
    }
}
