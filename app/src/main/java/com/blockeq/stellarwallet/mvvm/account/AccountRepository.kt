package com.blockeq.stellarwallet.mvvm.account

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.interfaces.OnLoadAccount
import com.blockeq.stellarwallet.models.MinimumBalance
import com.blockeq.stellarwallet.mvvm.effects.remote.OnLoadEffects
import com.blockeq.stellarwallet.mvvm.effects.remote.RemoteRepository
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse
import java.util.*

/**
 * Tried to implement (https://github.com/JoaquimLey/transport-eta/blob/26ce1a7f4b2dff12c6efa2292531035e70bfc4ae/app/src/main/java/com/joaquimley/buseta/repository/BusRepository.java)
 * While at the same time only using remote, and not local or Room db
 */
class AccountRepository {

    private var liveData = MutableLiveData<AccountResponse>()

    /**
     * Returns an observable for ALL the effects table changes
     */
    fun loadAccount(): LiveData<AccountResponse> {
        Horizon.getLoadAccountTask(object : OnLoadAccount {
            override fun onLoadAccount(result: AccountResponse?) {
                if (result != null) {
                   liveData.postValue(result)

                    WalletApplication.localStore.balances = result.balances
                    WalletApplication.userSession.minimumBalance = MinimumBalance(result)
                    WalletApplication.localStore.availableBalance = AccountUtils.calculateAvailableBalance()
                }
            }

            override fun onError(error: ErrorResponse) {

            }

        }).execute()
        return liveData
    }
}