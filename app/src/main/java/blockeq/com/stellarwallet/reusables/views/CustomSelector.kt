package blockeq.com.stellarwallet.reusables.views

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.adapters.CustomArrayAdapter
import blockeq.com.stellarwallet.models.SelectionModel
import kotlinx.android.synthetic.main.view_custom_selector.view.*


class CustomSelector @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    private val INPUT_TYPE_NUMBER = 1
    private val INPUT_TYPE_DECIMAL = 2
    private val INPUT_TYPE_TEXT = 3
    private val INPUT_TYPE_EMAIL = 4
    private val INPUT_TYPE_NONE = 5

    init {
        LayoutInflater.from(context)
                .inflate(R.layout.view_custom_selector, this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it,
                    R.styleable.CustomSelector, 0, 0)
            setHint(typedArray.getString(R.styleable.CustomSelector_hint))
            setColor(typedArray.getColor(R.styleable.CustomSelector_customStrokeColor,
                    ContextCompat.getColor(context, R.color.alto)))
            setInputType(typedArray.getInt(R.styleable.CustomSelector_inputType, 0))
        }
    }

    private fun setHint(hint: String) {
        editText.hint = hint
    }

    private fun setColor(color: Int) {
        val editTextSld = editText.background as StateListDrawable
        val editTextBgChildren = (editTextSld.constantState as DrawableContainer.DrawableContainerState).children
        val editTextDisabledGd = editTextBgChildren.get(0) as GradientDrawable
        val editTextEnabledGd = editTextBgChildren.get(1) as GradientDrawable
        editTextDisabledGd.setStroke(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                1f, resources.displayMetrics).toInt(), color)
        editTextEnabledGd.setStroke(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                1f, resources.displayMetrics).toInt(), color)
        val spinnerGd = spinnerContainer.background as GradientDrawable
        spinnerGd.setStroke(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                1f, resources.displayMetrics).toInt(), color)
        spinnerGd.setColor(color)
        spinner.setPopupBackgroundDrawable(ColorDrawable(color))
    }

    private fun setInputType(inputType: Int) {
        when (inputType) {
            INPUT_TYPE_NUMBER -> editText.inputType = InputType.TYPE_CLASS_NUMBER
            INPUT_TYPE_DECIMAL -> editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            INPUT_TYPE_TEXT -> editText.inputType = InputType.TYPE_CLASS_TEXT
            INPUT_TYPE_EMAIL -> editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            INPUT_TYPE_NONE -> editText.inputType = 0
            else -> editText.inputType = InputType.TYPE_CLASS_TEXT
        }
    }

    fun setSelectionValues(values: MutableList<SelectionModel>) {
        val customArrayAdapter = CustomArrayAdapter(context, R.layout.view_generic_spinner_item, values)
        spinner.adapter = customArrayAdapter
    }

}