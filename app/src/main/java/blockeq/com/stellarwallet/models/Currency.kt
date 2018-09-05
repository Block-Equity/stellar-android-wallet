package blockeq.com.stellarwallet.models

class Currency(var id: Int, var code: String, var name: String, holdings: Float) : SelectionModel(code, id, holdings)