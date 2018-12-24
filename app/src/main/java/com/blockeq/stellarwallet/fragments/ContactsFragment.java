package com.blockeq.stellarwallet.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blockeq.stellarwallet.R;
import com.blockeq.stellarwallet.adapters.ContactsAdapter;

/**
 * Fragment that holds the RecyclerView
 */
public class ContactsFragment extends Fragment {

    private static final String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };

    // TODO: Implement a more advanced example that makes use of this
    private static final String SELECTION = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";

    // Defines a variable for the search string
    private String mSearchString = "@hotmail.com";
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = { mSearchString };

    private RecyclerView mContactListView;


    // A UI Fragment must inflate its View
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View root = inflater.inflate(R.layout.contact_list, container, false);
        mContactListView = (RecyclerView) root.findViewById(R.id.rv_contact_list);
        mContactListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactListView.setItemAnimator(new DefaultItemAnimator());
        requestContacts();
        return root;
    }

    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private void requestContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            showContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showContacts();
            } else {
                Log.e("Permissions", "Access denied");
            }
        }
    }

    private void showContacts(){

        // Initializes a loader for loading the contacts
        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                /*
                 * Makes search string into pattern and
                 * stores it in the selection array
                 */
                Uri contentUri = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_FILTER_URI,
                        Uri.encode(mSearchString));
                // Starts the query
                return new CursorLoader(
                        getActivity(),
                        contentUri,
                        PROJECTION,
                        null,
                        null,
                        null);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> objectLoader, Cursor c) {
                // Put the result Cursor in the adapter for the ListView
                mContactListView.setAdapter(new ContactsAdapter(c));
            }

            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
                // TODO do I need to do anything here?
            }
        });
    }

}
