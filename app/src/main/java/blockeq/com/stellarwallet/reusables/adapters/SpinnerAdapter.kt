package blockeq.com.stellarwallet.reusables.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.reusables.models.SelectionModel

class SpinnerAdapter(val context: Context) : BaseAdapter() {

    private var inflater: LayoutInflater? = null
    private var itemsList: Array<SelectionModel>? = null

    init {
        inflater = LayoutInflater.from(context)
    }

    fun setItemsList(itemsList: Array<SelectionModel>?) {
        this.itemsList = itemsList
    }

    override fun getCount(): Int {
        return if (itemsList != null) itemsList!!.size else 0
    }

    override fun getItem(position: Int): String? {
        return itemsList?.elementAt(position)?.label
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return getViewBasedOnState(position, convertView, parent, false)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getViewBasedOnState(position, convertView, parent, true)
    }

    private fun getViewBasedOnState(position: Int, convertView: View?, parent: ViewGroup?, isDropDown: Boolean): View {
        var vi: View? = convertView
        if (convertView == null)
            vi = inflater!!.inflate(R.layout.view_generic_spinner_item, parent, false)

        val itemTv = vi?.findViewById(R.id.itemLabel) as TextView
        if (isDropDown) {
            itemTv.setPadding(context.resources.getDimension(R.dimen.margin_padding_size_medium).toInt(), 0, 0, 0)
        }
        itemTv.text = itemsList?.elementAt(position)?.label

        return vi
    }
}