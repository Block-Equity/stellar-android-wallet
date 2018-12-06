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
import com.blockeq.stellarwallet.flowcontrollers.PinFlowController
import com.blockeq.stellarwallet.models.PinType
import com.blockeq.stellarwallet.models.PinViewState
import com.blockeq.stellarwallet.utils.AccountUtils
import kotlinx.android.synthetic.main.activity_pin.*

class PinActivity : BaseActivity(), PinLockListener {

    companion object {
        const val PIN_REQUEST_CODE = 0
        const val MAX_ATTEMPTS = 3
        const val KEY_SECRET_SEED = "kDecryptedPhrase"
    }

    private var needConfirm = true
    private lateinit var PIN : String
    private lateinit var mnemonic : String
    private lateinit var pinViewState: PinViewState
    private var numAttempts = 0
    private lateinit var context : Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        pinLockView.setPinLockListener(this)
        pinLockView.attachIndicatorDots(indicatorDots)

        pinViewState = getPinState()
        mnemonic = pinViewState.mnemonic
        val message = pinViewState.message
        //TODO: review pin since it looks like is always EMPTY here
        PIN = pinViewState.pin

        if (!message.isEmpty()) {
            customMessageTextView.text = message
        }

        context = applicationContext
    }

    override fun onEmpty() {
    }

    override fun onComplete(pin: String) {
        val handler = Handler()
        val runnableCode = Runnable {
            when (pinViewState.type) {
                PinType.CREATE -> {
                    when {
                        needConfirm -> {
                            PIN = pin
                            pinLockView.resetPinLockView()

                            customMessageTextView.text = getString(R.string.please_reenter_your_pin)
                            needConfirm = false
                        }
                        pin != PIN -> onIncorrectPin()
                        else -> {
                            setResult(Activity.RESULT_OK)

                            AccountUtils.encryptAndStoreWallet(applicationContext, pinViewState.mnemonic, pinViewState.passphrase, pin)

                            val stellarKeyPair = AccountUtils.getStellarKeyPair(pinViewState.mnemonic, pinViewState.passphrase)

                            WalletApplication.localStore.stellarAccountId = stellarKeyPair.accountId
                            WalletApplication.userSession.pin = pin

                            launchWallet()
                        }
                    }
                }
                else -> {
                    val encryptedPhrase = getEncryptedPhrase(pinViewState.type)
                    val encryptedPassphrase = WalletApplication.localStore.encryptedPassphrase
                    val masterKey = AccountUtils.getPinMasterKey(context, pin)

                    if (masterKey != null) {
                        val decryptedPhrase = AccountUtils.getDecryptedString(encryptedPhrase, masterKey)
                        var decryptedPassphrase : String? = null
                        if (encryptedPassphrase != null) {
                            decryptedPassphrase = AccountUtils.getDecryptedString(encryptedPassphrase, masterKey)
                        }

                        when {
                            pinViewState.type == PinType.LOGIN -> {
                                WalletApplication.userSession.pin = pin
                                launchWallet()
                            }
                            pinViewState.type == PinType.CHECK -> {
                                val keyPair = AccountUtils.getStellarKeyPair(decryptedPhrase, decryptedPassphrase)
                                val intent = Intent()
                                intent.putExtra(KEY_SECRET_SEED, keyPair.secretSeed)
                                setResult(Activity.RESULT_OK, intent)
                                finishActivity()
                            }
                            pinViewState.type == PinType.CLEAR_WALLET -> wipeAndRestart()

                            pinViewState.type == PinType.TOGGLE_PIN_ON_SENDING -> {
                                WalletApplication.localStore.showPinOnSend = !WalletApplication.localStore.showPinOnSend
                                finish()
                            }

                            pinViewState.type == PinType.VIEW_PHRASE -> {
                                startActivity(MnemonicActivity.newDisplayMnemonicIntent(this, decryptedPhrase))
                                finish()
                            }

                            pinViewState.type == PinType.VIEW_SEED -> {
                                val keyPair = AccountUtils.getStellarKeyPair(decryptedPhrase, decryptedPassphrase)
                                val secretSeed = keyPair.secretSeed.joinToString("")
                                val intent = Intent(this, ViewSecretSeedActivity::class.java)

                                intent.putExtra(ViewSecretSeedActivity.SECRET_SEED, secretSeed)
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        onIncorrectPin()
                    }
                }
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

        return bundle.getParcelable(PinFlowController.OBJECT)
    }

    //endregion


    //region Encryption and Decryption

    private fun getEncryptedPhrase(pinType: PinType) : String {
        return if (pinType == PinType.CHECK || pinType == PinType.LOGIN) {
            WalletApplication.localStore.encryptedPhrase!!
        } else {
            pinViewState.mnemonic
        }
    }

    private fun wipeAndRestart() {
        AccountUtils.wipe(this)
        val intent = Intent(this, LaunchActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}