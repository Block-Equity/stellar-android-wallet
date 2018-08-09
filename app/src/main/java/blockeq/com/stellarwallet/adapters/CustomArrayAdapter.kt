package blockeq.com.stellarwallet.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.models.SelectionModel

open class CustomArrayAdapter(context: Context, private var resource: Int, private var itemsList: MutableList<SelectionModel>?) :
        ArrayAdapter<SelectionModel>(context, resource, itemsList) {

    private var inflater: LayoutInflater? = null

    init {
        inflater = LayoutInflater.from(context)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var vi: View? = convertView
        if (convertView == null)
            vi = inflater!!.inflate(resource, parent, false)

        val itemTv = vi?.findViewById(R.id.itemLabel) as TextView
        itemTv.text = itemsList?.elementAt(position)?.label

        return vi
    }

}