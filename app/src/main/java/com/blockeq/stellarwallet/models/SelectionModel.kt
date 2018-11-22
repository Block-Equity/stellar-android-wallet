package com.blockeq.stellarwallet.models

import org.stellar.sdk.Asset

open class SelectionModel(var label: String, var value: Int, var holdings: Float, var asset : Asset?) {
    override fun toString(): String {
        return label
    }
}