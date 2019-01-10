package com.blockeq.stellarwallet.utils

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.blockeq.stellarwallet.interfaces.CloudNodeStorage
import com.blockeq.stellarwallet.models.BasicBalance
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class CloudNodeStorageImpl(context: Context) : CloudNodeStorage {
    private val ENABLE_CLODE_STORAGE = false
    private val WEAR_PATH = "/blockeq_wallet"
    private val KEY_ACCOUNT_ID = "KEY_ACCOUNT_ID"
    private val KEY_BALANCES = "KEY_BALANCES"

    private val dataClient = Wearable.getDataClient(context.applicationContext)
    private val appContext = context.applicationContext

    /**
     * the operation is async.
     */
    override fun saveAccountId(accountId: String) {
        if (ENABLE_CLODE_STORAGE) {
            val putDataMapReq = PutDataMapRequest.create(WEAR_PATH)
            val dataMap = putDataMapReq.dataMap
            dataMap.putString(KEY_ACCOUNT_ID, accountId)
            val putDataReq = putDataMapReq.asPutDataRequest()
            val putDataTask = dataClient.putDataItem(putDataReq)
            putDataTask.addOnCompleteListener {
                Timber.v("saveWearableData complete listener")
            }
        }
    }

    /**
     * the operation is async.
     */
    override fun saveBalances(list: ArrayList<BasicBalance>) {
        if (ENABLE_CLODE_STORAGE) {
            val putDataMapReq = PutDataMapRequest.create(WEAR_PATH)
            val dataMap = putDataMapReq.dataMap
            val bundle = Bundle()
            bundle.putSerializable(KEY_BALANCES, list)
            dataMap.putAll(DataMap.fromBundle(bundle))
            val putDataReq = putDataMapReq.asPutDataRequest()
            val putDataTask = dataClient.putDataItem(putDataReq)
            putDataTask.addOnCompleteListener {
                Timber.v("saveWearableData complete listener")
            }
        }
    }

    override fun clearNode() {
        if (ENABLE_CLODE_STORAGE) {
            val uri = Uri.Builder()
                    .scheme(PutDataRequest.WEAR_URI_SCHEME)
                    .path(WEAR_PATH)
                    /**
                     * wildcard means delete path from all the nodes.
                     */
                    .authority("*")
                    .build()

            Wearable.getDataClient(appContext).deleteDataItems(uri)
        }
    }
}
