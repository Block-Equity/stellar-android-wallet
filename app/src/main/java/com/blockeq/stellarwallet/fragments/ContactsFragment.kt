package com.blockeq.stellarwallet.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.adapters.ContactsAdapter

/**
 * Fragment that holds the RecyclerView
 */
class ContactsFragment : Fragment() {
    companion object {

        private val PROJECTION = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

        // TODO: Implement a more advanced example that makes use of this
        private val SELECTION = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?"

        // Request code for READ_CONTACTS. It can be any number > 0.
        private val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    }

    // Defines a variable for the search string
    private val mSearchString = "@hotmail.com"
    // Defines the array to hold values that replace the ?
    private val mSelectionArgs = arrayOf(mSearchString)

    private var mContactListView: RecyclerView? = null


    // A UI Fragment must inflate its View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the fragment layout
        val root = inflater.inflate(R.layout.contact_list, container, false)
        mContactListView = root.findViewById<View>(R.id.rv_contact_list) as RecyclerView
        mContactListView!!.layoutManager = LinearLayoutManager(activity)
        mContactListView!!.itemAnimator = DefaultItemAnimator()
        requestContacts()
        return root
    }

    private fun requestContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity!!.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_READ_CONTACTS)
        } else {
            showContacts()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showContacts()
            } else {
                Log.e("Permissions", "Access denied")
            }
        }
    }

    private fun showContacts() {

        // Initializes a loader for loading the contacts
        loaderManager.initLoader(0,
                null, object : LoaderManager.LoaderCallbacks<Cursor> {
            override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
                /*
                 * Makes search string into pattern and
                 * stores it in the selection array
                 */
                val contentUri = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_FILTER_URI,
                        Uri.encode(mSearchString))
                // Starts the query
                return CursorLoader(
                        activity!!,
                        contentUri,
                        PROJECTION, null, null, null)
            }

            override fun onLoadFinished(objectLoader: Loader<Cursor>, c: Cursor) {
                // Put the result Cursor in the adapter for the ListView
                mContactListView!!.adapter = ContactsAdapter(c)
            }

            override fun onLoaderReset(cursorLoader: Loader<Cursor>) {
                // TODO do I need to do anything here?
            }
        })
    }



}
