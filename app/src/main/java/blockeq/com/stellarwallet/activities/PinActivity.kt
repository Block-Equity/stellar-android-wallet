package blockeq.com.stellarwallet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import blockeq.com.stellarwallet.utils.AccountUtils
import com.andrognito.pinlockview.PinLockListener
import kotlinx.android.synthetic.main.activity_pin.*

class PinActivity : BaseActivity(), PinLockListener {

    companion object {
        const val PIN_REQUEST_CODE = 0
        const val RESULT_FAIL = 2

        const val MAX_ATTEMPTS = 3
        const val KEY_SECRET_SEED = "kDecryptedPhrase"
    }

    private var needConfirm = true
    private lateinit var PIN : String
    private lateinit var phrase : String
    private lateinit var pinViewState: PinViewState
    private var numAttempts = 0
    private lateinit var context : Context
    private var cipherWrapper : CipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        pinLockView.setPinLockListener(this)
        pinLockView.attachIndicatorDots(indicatorDots)

        pinViewState = getPinState()
        phrase = pinViewState.phrase
        val message = pinViewState.message
        PIN = pinViewState.pin

        if (!message.isEmpty()) {
            tv_custom_message.text = message
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

                            tv_custom_message.text = getString(R.string.please_reenter_your_pin)
                            needConfirm = false
                        }
                        pin != PIN -> onIncorrectPin()
                        else -> {
                            setResult(Activity.RESULT_OK)

                            val keyStoreWrapper = KeyStoreWrapper(applicationContext)
                            keyStoreWrapper.createAndroidKeyStoreAsymmetricKey(pin)

                            val masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin)
                            val encryptedData = cipherWrapper.encrypt(pinViewState.phrase, masterKey?.public)

                            WalletApplication.localStore.encryptedPhrase = encryptedData

                            val keyPair = AccountUtils.getKeyPair(pinViewState.phrase)

                            WalletApplication.localStore.publicKey = keyPair.accountId
                            WalletApplication.userSession.pin = pin

                            launchWallet()
                        }
                    }
                }
                else -> {
                    val encryptedPhrase = getEncryptedPhrase(pinViewState.type)
                    val masterKey = AccountUtils.getPinMasterKey(context, pin)

                    if (masterKey != null) {
                        val decryptedData = cipherWrapper.decrypt(encryptedPhrase, masterKey.private)

                        when {
                            pinViewState.type == PinType.LOGIN -> {
                                WalletApplication.userSession.pin = pin
                                launchWallet()
                            }
                            pinViewState.type == PinType.CHECK -> {
                                val keyPair = AccountUtils.getKeyPair(decryptedData)
                                val intent = Intent()
                                intent.putExtra(KEY_SECRET_SEED, keyPair.secretSeed)
                                setResult(Activity.RESULT_OK, intent)
                                finishActivity()
                            }
                            pinViewState.type == PinType.CLEAR_WALLET -> wipeAndRestart()

                            pinViewState.type == PinType.VIEW_PHRASE -> {
                                val intent = Intent(this, ShowMnemonicActivity::class.java)
                                intent.putExtra(ShowMnemonicActivity.INTENT_DISPLAY_PHRASE, true)
                                intent.putExtra(ShowMnemonicActivity.DECRYPTED_PHRASE, decryptedData)
                                startActivity(intent)
                                finish()
                            }

                            pinViewState.type == PinType.VIEW_SEED -> {
                                val keyPair = AccountUtils.getKeyPair(decryptedData)
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
        //TODO: this delay is to prevent the freeze in the last digit animation.
        //TODO: the work should be done not in the ui thread.
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
                tv_custom_message.text = resources.getQuantityString(R.plurals.attempts_template,
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
        //TODO: throw an exception here if parcelable is null
        return bundle.getParcelable(PinFlowController.OBJECT)
    }

    //endregion


    //region Encryption and Decryption

    private fun getEncryptedPhrase(pinType: PinType) : String {
        return if (pinType == PinType.CHECK || pinType == PinType.LOGIN) {
            WalletApplication.localStore.encryptedPhrase!!
        } else {
            pinViewState.phrase
        }
    }

    private fun wipeAndRestart() {
        WalletApplication.localStore.clearUserData()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
    //endregion
}
