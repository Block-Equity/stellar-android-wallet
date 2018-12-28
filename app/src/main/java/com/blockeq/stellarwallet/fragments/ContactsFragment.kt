package com.blockeq.stellarwallet.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.adapters.ContactsAdapter
import com.blockeq.stellarwallet.vmodels.ContactsRepository
import kotlinx.android.synthetic.main.contact_list.*
import timber.log.Timber

/**
 * Fragment that holds the RecyclerView
 */
class ContactsFragment : Fragment() {
    companion object {

        private var PROJECTION = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

        // TODO: Implement a more advanced example that makes use of this
        private val SELECTION = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?"

        // Request code for READ_CONTACTS. It can be any number > 0.
        private const val PERMISSIONS_REQUEST_CONTACTS = 100
    }

    // Defines a variable for the search string
    private lateinit var appContext : Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    // A UI Fragment must inflate its View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.contact_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appContext = view.context.applicationContext
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        rv_contact_list.layoutManager =  LinearLayoutManager(activity)
        rv_contact_list.itemAnimator = DefaultItemAnimator()
        setInitialState()
        rv_contact_list.postDelayed(
        {
            requestContacts()
        },
        300
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu to use in the action bar
        inflater.inflate(R.menu.contacts_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.refresh -> {
                setInitialState()
                rv_contact_list.postDelayed(
                        {
                            showContacts()
                        },
                        300
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setInitialState() {
        rv_contact_list.visibility = View.GONE
        empty_view.visibility = View.GONE
        progress_view.visibility = View.VISIBLE
    }


    private fun requestContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasPermissions(appContext, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), PERMISSIONS_REQUEST_CONTACTS)
        } else {
            showContacts()
        }
    }


    private fun hasPermissions(context: Context, vararg permissions: String): Int {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return PackageManager.PERMISSION_DENIED
            }
        }
        return  PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showContacts()
            } else {
                Timber.e("Permissions Access denied")
            }
        }
    }

    private fun showContacts() {
//        ContactsRepository(appContext).getContactListAsync(this, object : ContactsRepository.OnContactListLoaded {
//            override fun onLoaded(cursor: Cursor) {
//                populateList(cursor)
//            }
//        })
                val cursor = ContactsRepository(appContext).getContactsList()
        if (cursor != null) {
            populateList(cursor)
        }
    }

    fun populateList(cursor : Cursor){
        rv_contact_list.adapter = ContactsAdapter(cursor)
        progress_view.visibility = View.GONE
        if (cursor.count == 0) {
            empty_view.visibility = View.VISIBLE
            rv_contact_list.visibility = View.GONE
        } else {
            empty_view.visibility = View.GONE
            rv_contact_list.visibility = View.VISIBLE
        }
    }
}
