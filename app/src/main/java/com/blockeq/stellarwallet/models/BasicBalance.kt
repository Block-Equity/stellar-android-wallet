package com.blockeq.stellarwallet.models

import java.io.Serializable

data class BasicBalance(var balance: String, var assetType: String, var assetCode: String, var assetIssuer: String?) : Serializable
