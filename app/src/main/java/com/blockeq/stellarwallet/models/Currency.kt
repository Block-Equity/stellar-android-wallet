package com.blockeq.stellarwallet.models

import org.stellar.sdk.Asset

class Currency(var id: Int, var code: String, var name: String, holdings: Double, var currenycAsset : Asset?) : SelectionModel(code, id, holdings, currenycAsset)