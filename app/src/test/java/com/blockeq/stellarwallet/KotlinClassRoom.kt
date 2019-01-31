package com.blockeq.stellarwallet

import org.junit.Test

class KotlinClassRoom {
    @Test
    fun testLetElse(){
        val value : String? = null
        value?.let {
            assert(false)
        }?:run {
            assert(true)
        }
    }

    @Test
    fun testLetElse3(){
        val value : String? = "234"
        value?.let {
            assert(true)
        }?:run {
            assert(false)
        }
    }
}
