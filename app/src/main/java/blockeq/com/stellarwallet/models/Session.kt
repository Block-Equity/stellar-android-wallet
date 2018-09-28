package blockeq.com.stellarwallet.models

import org.stellar.sdk.KeyPair

/**
 * Class which provides a model for Session
 * @constructor Sets all properties of the Session
 * @property keyPair the used to make Horizon api calls
 */
class Session(var keyPair: KeyPair)
