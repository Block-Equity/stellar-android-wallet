package com.blockeq.stellarwallet

import junit.framework.TestCase.assertEquals
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
    fun testLetElse2(){
        val value : String? = "234"
        value?.let {
            assert(true)
        }?:run {
            assert(false)
        }
    }

    @Test
    fun testReverseArray(){
        val values : ArrayList<Int> = arrayListOf()
        values.add(1)
        values.add(2)
        values.add(3)
        values.add(4)
        values.add(5)

        values.reverse()
        assertEquals(values.size, 5)
        assertEquals(values[0], 5)
    }
}
