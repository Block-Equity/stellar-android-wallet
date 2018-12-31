package com.blockeq.stellarwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants.Companion.STELLAR_ADDRESS_LENGTH
import com.blockeq.stellarwallet.interfaces.ContactsRepository.ContactOperationStatus
import com.blockeq.stellarwallet.vmodels.ContactsRepositoryImpl
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_enter_address.*
import java.lang.IllegalStateException
import timber.log.Timber

class EnterAddressActivity : BaseActivity(), View.OnClickListener {
    enum class Mode {
        SEND_TO, UPDATE_CONTACT, CREATE_CONTACT
    }
    private lateinit var mode : Mode
    private var contactID: Long = 0

    companion object {
        private const val ARG_MODE = "ARG_MODE"
        private const val ARG_CONTACT_ID = "ARG_CONTACT_ID"

        fun toSend(context: Context): Intent {
            val intent = Intent(context, EnterAddressActivity::class.java)
            intent.putExtra(ARG_MODE, Mode.SEND_TO)
            return intent
        }

        fun updateContact(context: Context, contactId: Long): Intent {
            val intent = Intent(context, EnterAddressActivity::class.java)
            intent.putExtra(ARG_MODE, Mode.UPDATE_CONTACT)
            intent.putExtra(ARG_CONTACT_ID, contactId)
            return intent
        }

        fun createContact(context: Context): Intent {
            val intent = Intent(context, EnterAddressActivity::class.java)
            intent.putExtra(ARG_MODE, Mode.CREATE_CONTACT)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_address)

        setupUI()
    }

    private fun initiateScan() {
        IntentIntegrator(this).setBeepEnabled(false).setDesiredBarcodeFormats(IntentIntegrator.QR_CODE).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show()
            } else {
                addressEditText.setText(result.contents)
                bottomButton.isEnabled = true
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //region User Interface
    private fun setupUI() {
        setSupportActionBar(toolBar)
        if (intent.hasExtra(ARG_MODE) && intent.getSerializableExtra(ARG_MODE) != null) {
            mode = intent.getSerializableExtra(ARG_MODE) as Mode
        } else {
            throw IllegalStateException("Missing intent extra {$ARG_MODE}")
        }

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            when(mode) {
                Mode.SEND_TO -> it.title = getString(R.string.button_send)
                Mode.UPDATE_CONTACT -> it.title = getString(R.string.setup_contact_title)
                Mode.CREATE_CONTACT -> it.title = getString(R.string.add_contact_title)
            }
        }

        when(mode) {
            Mode.SEND_TO -> {
                titleBalance.text = WalletApplication.userSession.getFormattedCurrentAvailableBalance(applicationContext)
                bottomButton.text = getString(R.string.next_button_text)
                ContactNameText.visibility = View.GONE
                ContactNameEditText.visibility = View.GONE
                addressTitleText.text = getString(R.string.send_to_text)
            }
            Mode.UPDATE_CONTACT -> {
                titleBalance.visibility = View.GONE
                bottomButton.text = getString(R.string.save_button)
                ContactNameText.visibility = View.GONE
                ContactNameEditText.visibility = View.GONE
                addressTitleText.text = "Stellar Address"
                val serializedValue = intent.getSerializableExtra(ARG_CONTACT_ID) ?: throw IllegalStateException("Missing intent extra {$ARG_CONTACT_ID}")
                contactID = serializedValue as Long
                //TODO: use contact objec in the bundle and remove this call
                addressEditText.setText(ContactsRepositoryImpl(applicationContext).getStellarAddress(contactID))
            }
            Mode.CREATE_CONTACT -> {
                titleBalance.visibility = View.GONE
                bottomButton.text = "CREATE"
                addressTitleText.text = "Stellar Address"
            }
        }

        cameraImageButton.setOnClickListener(this)
        bottomButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val address = addressEditText.text.toString()
        when(v.id) {
            R.id.cameraImageButton -> {
                initiateScan()
            }
            R.id.bottomButton -> {
                when(mode) {
                    Mode.SEND_TO -> {
                        if (address.length == STELLAR_ADDRESS_LENGTH && address != WalletApplication.localStore.stellarAccountId) {
                            startActivity(SendActivity.newIntent(this, address))
                            overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                        } else {
                            // Shake animation on the text
                            val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                            addressEditText.startAnimation(shakeAnimation)
                        }
                    }
                    Mode.UPDATE_CONTACT -> {
                        val status = ContactsRepositoryImpl(applicationContext).createOrUpdateContact(contactID, address)
                        when(status) {
                            ContactOperationStatus.UPDATED -> {
                                Timber.v("data updated")
                                Toast.makeText(applicationContext, "stellar address updated", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            ContactOperationStatus.INSERTED -> {
                                Timber.v("data inserted")
                                Toast.makeText(applicationContext, "stellar address inserted", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            ContactOperationStatus.FAILED -> {
                                Timber.v("failed to update contact")
                                Toast.makeText(applicationContext, "stellar address failed to be added", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
                    Mode.CREATE_CONTACT -> {
                        val name = ContactNameEditText.text.toString()
                        if (name.isBlank() || address.isBlank()) {
                            Toast.makeText(applicationContext, "one or more fields are empty", Toast.LENGTH_SHORT).show()
                        } else {
                            val contactId = ContactsRepositoryImpl(applicationContext).createContact(name, address)
                            if (contactId == -1L ){
                                Toast.makeText(applicationContext, "failed to create the new contact", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(applicationContext, "contact has been created", Toast.LENGTH_SHORT).show()
                            }
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            if (item.itemId == android.R.id.home) {
                finish()
                return true
            }
        }
        return false
    }


    //endregion
}
