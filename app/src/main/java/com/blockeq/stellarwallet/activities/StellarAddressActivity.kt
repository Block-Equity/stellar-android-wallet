package com.blockeq.stellarwallet.activities

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.blockeq.stellarwallet.BuildConfig
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants.Companion.STELLAR_ADDRESS_LENGTH
import com.blockeq.stellarwallet.interfaces.ContactsRepository.ContactOperationStatus
import com.blockeq.stellarwallet.models.Contact
import com.blockeq.stellarwallet.mvvm.balance.BalanceRepository
import com.blockeq.stellarwallet.utils.StringFormat
import com.blockeq.stellarwallet.vmodels.ContactsRepositoryImpl
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE
import kotlinx.android.synthetic.main.activity_stellar_address.*
import org.jetbrains.anko.db.StringParser
import timber.log.Timber

class StellarAddressActivity : BaseActivity(), View.OnClickListener {
    enum class Mode {
        SEND_TO, UPDATE_CONTACT, CREATE_CONTACT
    }
    private lateinit var mode : Mode
    /**
     * it will be set only in {@link Mode.UPDATE_CONTACT}
     */
    private lateinit var contact: Contact

    companion object {
        private const val ARG_MODE = "ARG_MODE"
        private const val ARG_CONTACT = "ARG_CONTACT"

        private const val SEND_MONEY_REQUEST = 0x33


        fun toSend(context: Context): Intent {
            val intent = Intent(context, StellarAddressActivity::class.java)
            intent.putExtra(ARG_MODE, Mode.SEND_TO)
            return intent
        }

        fun updateContact(context: Context, contactId: Contact): Intent {
            val intent = Intent(context, StellarAddressActivity::class.java)
            intent.putExtra(ARG_MODE, Mode.UPDATE_CONTACT)
            intent.putExtra(ARG_CONTACT, contactId)
            return intent
        }

        fun createContact(context: Context): Intent {
            val intent = Intent(context, StellarAddressActivity::class.java)
            intent.putExtra(ARG_MODE, Mode.CREATE_CONTACT)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stellar_address)

        if (intent.hasExtra(ARG_MODE) && intent.getSerializableExtra(ARG_MODE) != null) {
            mode = intent.getSerializableExtra(ARG_MODE) as Mode
        } else {
            throw IllegalStateException("Missing intent extra {$ARG_MODE}")
        }

        if (mode == Mode.UPDATE_CONTACT) {
            contact = intent.getParcelableExtra(ARG_CONTACT) ?: throw IllegalStateException("Missing intent extra {$ARG_CONTACT}")
        }

        setupUI()
    }

    private fun initiateScan() {
        IntentIntegrator(this).setBeepEnabled(false).setDesiredBarcodeFormats(IntentIntegrator.QR_CODE).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_CODE -> {
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result != null) {
                    if (result.contents == null) {
                        Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show()
                    } else {
                        addressEditText.setText(result.contents)
                        bottomButton.isEnabled = true
                    }
                }
            }
            SEND_MONEY_REQUEST -> {
                finish()
            } else -> super.onActivityResult(requestCode, resultCode, data)

        }
    }

    //region User Interface
    private fun setupUI() {
        setSupportActionBar(toolBar)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            when(mode) {
                Mode.SEND_TO -> it.title = getString(R.string.button_send)
                Mode.UPDATE_CONTACT -> it.title = getString(R.string.update_contact_title, contact.name)
                Mode.CREATE_CONTACT -> it.title = getString(R.string.add_contact_title)
            }
        }

        when(mode) {
            Mode.SEND_TO -> {
                BalanceRepository.loadBalance().observe(this, Observer {
                    if(it!=null) {
                        val asset = it.getActiveAssetAvailability()
                        @SuppressLint("SetTextI18n")
                        val amount = asset.totalAvailable-0.0001
                        if (amount < 0) {
                            titleBalance.text = "< 0.0001"
                        } else {
                            titleBalance.text = "${StringFormat.truncateDecimalPlaces(amount.toString())} ${asset.assetCode}"
                        }
                    }
                })
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
                addressTitleText.text = getString(R.string.stellar_address_title)
                addressEditText.setText(contact.stellarAddress)
            }
            Mode.CREATE_CONTACT -> {
                titleBalance.visibility = View.GONE
                bottomButton.text = getString(R.string.create_button)
                addressTitleText.text = getString(R.string.stellar_address_title)
            }
        }

        cameraImageButton.setOnClickListener(this)
        bottomButton.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu : Menu) : Boolean {
        // This feature is disabled in release since it is not useful for the user.
        if (mode == Mode.UPDATE_CONTACT && BuildConfig.DEBUG) {
            menuInflater.inflate(R.menu.contact_details, menu)
        }
        return true
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
                        if (address.length == STELLAR_ADDRESS_LENGTH && address != WalletApplication.wallet.getStellarAccountId()) {
                            startActivityForResult(SendActivity.newIntent(this, address), SEND_MONEY_REQUEST)
                            overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                        } else {
                            // Shake animation on the text
                            val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                            addressEditText.startAnimation(shakeAnimation)
                        }
                    }
                    Mode.UPDATE_CONTACT -> {
                        val status = ContactsRepositoryImpl(applicationContext).createOrUpdateStellarAddress(contact.name, address)
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
            if (item.itemId == R.id.nav_open_contact) {
                val intent = Intent(Intent.ACTION_VIEW)
                val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.id.toString())
                intent.data = uri
                startActivity(intent)
                finish()
                return true
            }
        }
        return false
    }


    //endregion
}
