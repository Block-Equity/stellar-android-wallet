package com.blockeq.stellarwallet.helpers

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Bip0039Test {
    @Test
    fun basic_text() {
        assertFalse(Bip0039.isValidNumberOfWords(1))
        assertFalse(Bip0039.isValidNumberOfWords(2))
        assertFalse(Bip0039.isValidNumberOfWords(3))
        assertFalse(Bip0039.isValidNumberOfWords(4))
        assertFalse(Bip0039.isValidNumberOfWords(5))
        assertFalse(Bip0039.isValidNumberOfWords(6))
        assertFalse(Bip0039.isValidNumberOfWords(7))
        assertFalse(Bip0039.isValidNumberOfWords(8))
        assertFalse(Bip0039.isValidNumberOfWords(9))
        assertFalse(Bip0039.isValidNumberOfWords(10))
        assertFalse(Bip0039.isValidNumberOfWords(11))
        assertFalse(Bip0039.isValidNumberOfWords(13))
        assertFalse(Bip0039.isValidNumberOfWords(14))
        assertFalse(Bip0039.isValidNumberOfWords(17))
        assertFalse(Bip0039.isValidNumberOfWords(25))

    }

    @Test
    fun basic_positive_text() {

        assertTrue(Bip0039.isValidNumberOfWords(12))
        assertTrue(Bip0039.isValidNumberOfWords(15))
        assertTrue(Bip0039.isValidNumberOfWords(18))
        assertTrue(Bip0039.isValidNumberOfWords(21))
        assertTrue(Bip0039.isValidNumberOfWords(24))
    }
}