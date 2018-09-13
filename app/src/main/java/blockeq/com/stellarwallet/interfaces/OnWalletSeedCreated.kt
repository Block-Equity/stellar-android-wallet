package blockeq.com.stellarwallet.interfaces

import org.stellar.sdk.KeyPair

interface OnWalletSeedCreated {
    fun onWalletSeedCreated(keyPair: KeyPair?)
}