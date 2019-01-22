package com.blockeq.stellarwallet

import android.support.test.InstrumentationRegistry
import android.support.test.uiautomator.UiDevice

abstract class BasePage {
    internal abstract fun onPageLoaded(): BasePage

    fun pressBack() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
    }
}
