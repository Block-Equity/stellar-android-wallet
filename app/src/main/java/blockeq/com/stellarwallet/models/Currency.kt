package blockeq.com.stellarwallet.models

import org.stellar.sdk.Asset

class Currency(var id: Int, var code: String, var name: String, holdings: Float, var specificAsset : Asset) : SelectionModel(code, id, holdings, specificAsset)