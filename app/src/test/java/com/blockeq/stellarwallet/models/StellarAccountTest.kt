package com.blockeq.stellarwallet.models

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotSame
import org.apache.commons.lang.builder.HashCodeBuilder
import org.junit.Test
import org.stellar.sdk.KeyPair
import org.stellar.sdk.responses.AccountResponse

class StellarAccountTest {
    @Test
    fun testHashcodeBuilder(){
        val val1 = HashCodeBuilder(17, 37)
                .append("this is a string")
                .append(Integer(12334))
                .append(13423L)
                .append("this is a string2")
                .toHashCode()

        val val2 = HashCodeBuilder(17, 37)
                .append("this is a string")
                .append(Integer(12334))
                .append(13423L)
                .append("this is a string2")
                .toHashCode()

        assertEquals(val1, val2)
    }

    @Test
    fun testHashCodeMockEqual(){
        val account1 = "GDXVG5T344TBLPCYYTUTMJTWNU2DN6XV2IH3CQYNTNQ2JVG7IOSPTCP5"

        val stellarAccount1 = BasicStellarAccount(account1, "inflation_destination", 123L, 2345)
        val stellarAccount2 = BasicStellarAccount(account1, "inflation_destination", 123L, 2345)

        assertEquals(stellarAccount1.basicHashCode(), stellarAccount2.basicHashCode())
    }

    @Test
    fun testHashCodeMockDiff(){
        val account1 = "GDXVG5T344TBLPCYYTUTMJTWNU2DN6XV2IH3CQYNTNQ2JVG7IOSPTCP5"
        val account2 = "GA7UNACGPOAITFO2APERDQ3ASUY2OFODYIBIFL42RZVMPBUKWEFW7MPJ"

        val stellarAccount1 = BasicStellarAccount(account1, "inflation_destination", 123L, 2345)
        val stellarAccount2 = BasicStellarAccount(account2, "inflation_destination", 123L, 2345)

        assertNotSame(stellarAccount1.basicHashCode(), stellarAccount2.basicHashCode())
        val stellarAccount3 = BasicStellarAccount(account1, "inflation_destination2", 123L, 2345)
        assertNotSame(stellarAccount1.basicHashCode(), stellarAccount3.basicHashCode())
        val stellarAccount4 = BasicStellarAccount(account1, "inflation_destination", 1234L, 2345)
        assertNotSame(stellarAccount1.basicHashCode(), stellarAccount4.basicHashCode())
        val stellarAccount5 = BasicStellarAccount(account1, "inflation_destination", 123L, 23456)
        assertNotSame(stellarAccount1.basicHashCode(), stellarAccount5.basicHashCode())
    }



    @Test
    fun testHashCodeEqual(){
        //This are redundant tests with the implementation
        val keyPair = KeyPair.fromAccountId("GA5J2E65ERCMYNND7JTBZ57AZFDBYCQWSLWNKTSU4UIYD37MLZUU2T5C")
        val stellarAccount1 = StellarAccountImpl(AccountResponse(keyPair, 1234))

        val keyPair2 = KeyPair.fromAccountId("GA5J2E65ERCMYNND7JTBZ57AZFDBYCQWSLWNKTSU4UIYD37MLZUU2T5C")
        val stellarAccount2 = StellarAccountImpl(AccountResponse(keyPair2, 1234))

        assertEquals(stellarAccount1.basicHashCode(), stellarAccount2.basicHashCode())
    }

    @Test
    fun testHashCodeDifferent(){
        //This are redundant tests with the implementation
        val keyPair = KeyPair.fromAccountId("GDXVG5T344TBLPCYYTUTMJTWNU2DN6XV2IH3CQYNTNQ2JVG7IOSPTCP5")
        val accountResponse = StellarAccountImpl(AccountResponse(keyPair, 1234))

        val keyPair2 = KeyPair.fromAccountId("GA7UNACGPOAITFO2APERDQ3ASUY2OFODYIBIFL42RZVMPBUKWEFW7MPJ")
        val accountResponse2 = StellarAccountImpl(AccountResponse(keyPair2, 1235))

        assertNotSame(accountResponse.basicHashCode(), accountResponse2.basicHashCode())
    }
}