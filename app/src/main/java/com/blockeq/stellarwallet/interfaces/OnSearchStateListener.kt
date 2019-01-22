package com.blockeq.stellarwallet.interfaces

import com.mancj.materialsearchbar.MaterialSearchBar

abstract class OnSearchStateListener :  MaterialSearchBar.OnSearchActionListener {
    override fun onButtonClicked(buttonCode: Int) {
        //empty implementation
    }

    abstract override fun onSearchStateChanged(enabled: Boolean)

    override fun onSearchConfirmed(text: CharSequence?) {
        //empty implementation
    }
}
