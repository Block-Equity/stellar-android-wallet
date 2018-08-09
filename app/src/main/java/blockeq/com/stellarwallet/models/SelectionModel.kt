package blockeq.com.stellarwallet.models

open class SelectionModel(var label: String, var value: Int, var holdings: Float) {
    override fun toString(): String {
        return label
    }
}