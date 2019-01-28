package com.blockeq.stellarwallet.helpers

class Constants {

    companion object {
        const val DEFAULT_ACCOUNT_BALANCE = "0.00"
        const val STELLAR_ADDRESS_LENGTH = 56
        const val USER_INDEX = 0
        const val MINIMUM_BALANCE_INCREMENT = 0.5
        const val BASE_RESERVE = 0.5

        const val UNKNOWN_ERROR = 520

        const val LUMENS_ASSET_TYPE = "native"
        const val LUMENS_ASSET_CODE = "XLM"
        const val LUMENS_ASSET_NAME = "Stellar Lumens"

        const val INFLATION_DESTINATION = "GCCD6AJOYZCUAQLX32ZJF2MKFFAUJ53PVCFQI3RHWKL3V47QYE2BNAUT"

        const val LUMENS_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/blockeq-wallet.appspot.com/o/icon-stellar.png?alt=media&token=38b70165-5255-4113-a15e-3c72bd4fab9f"

        // Maximum double representation in string ((2^63)-1)/(10^7)
        const val MAX_ASSET_STRING_VALUE = "922337203685.4775807"

        const val DEFAULT_TRANSACTION_FAILED_CODE = "tx_failed"
    }
}
