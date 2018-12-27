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
//                            appContext.contentResolver.query(
//                                    ContactsContract.Data.CONTENT_URI, PROJECTION,
//                                    ContactsContract.Data.RAW_CONTACT_ID + "=" + id + " AND "
//                                            + ContactsContract.Data.MIMETYPE + "= '"
//                                            + EnterAddressActivity.mimetypeStellarAddress + "'", null)
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
        val cursor = ContactsRepository(appContext).getContactsList()
        if (cursor != null) {
            populateList(cursor)
        }
    }
//        var RAW_PROJECTION = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1)
//        val cursor = context!!.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, RAW_PROJECTION, null, null, null)
//        populateList(cursor)
//
        // to be able to use raw projection with mime type the query has to filter by mimetype otherwise
        // a `IllegalArgumentException: Invalid column mimetype` will be thrown
//TODO RETURNS 2 manuel y ewen
//        val uri = ContactsContract.Data.CONTENT_URI
//        val RAW_PROJECTION = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Data.MIMETYPE)
//        val cursor = context!!.contentResolver.query(uri, RAW_PROJECTION,
//                ContactsContract.Data.MIMETYPE + " = ?",
//                arrayOf(EnterAddressActivity.mimetypeStellarAddress), null)
//                populateList(cursor!!)

//                val uri = ContactsContract.Data.CONTENT_URI
//        val RAW_PROJECTION = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY)
//        val cursor = context!!.contentResolver.query(uri, RAW_PROJECTION,
//                null, null, null)
//        populateList(cursor!!)

//        // Initializes a loader for loading the contacts
//        LoaderManager.getInstance<Fragment>(this).initLoader(0,
//                null, object : LoaderManager.LoaderCallbacks<Cursor> {
//            override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
//                val cursorLoader = CursorLoader(appContext)
//                cursorLoader.projection
//                // Starts the query
//                return CursorLoader(
//                        appContext,
//                        ContactsContract.Contacts.CONTENT_URI,
//                        PROJECTION, null, null, null)
//            }
//
//            override fun onLoadFinished(objectLoader: Loader<Cursor>, c: Cursor) {
//                // Put the result Cursor in the adapter for the ListView
//                rv_contact_list.adapter = ContactsAdapter(c)
//                progress_view.visibility = View.GONE
//                if (c.count == 0) {
//                    empty_view.visibility = View.VISIBLE
//                    rv_contact_list.visibility = View.GONE
//                } else {
//                    empty_view.visibility = View.GONE
//                    rv_contact_list.visibility = View.VISIBLE
//                }
//            }
//
//            override fun onLoaderReset(cursorLoader: Loader<Cursor>) { }
//        })
//    }

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
