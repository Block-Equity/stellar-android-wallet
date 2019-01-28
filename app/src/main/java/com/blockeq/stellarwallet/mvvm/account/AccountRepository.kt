package com.blockeq.stellarwallet.mvvm.account

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.interfaces.OnLoadAccount
import com.blockeq.stellarwallet.models.MinimumBalance
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse
import timber.log.Timber

/**
 * Tried to implement (https://github.com/JoaquimLey/transport-eta/blob/26ce1a7f4b2dff12c6efa2292531035e70bfc4ae/app/src/main/java/com/joaquimley/buseta/repository/BusRepository.java)
 * While at the same time only using remote, and not local or Room db
 */
class AccountRepository {
    private var liveData = MutableLiveData<AccountEvent>()

    /**
     * Returns an observable for ALL the effects table changes
     */
    fun loadAccount(): LiveData<AccountEvent> {
        Timber.d("Loading account")
        Horizon.getLoadAccountTask(object : OnLoadAccount {
            override fun onLoadAccount(result: AccountResponse?) {
                if (result != null) {
                    Timber.d("onLoadAccount")

                    WalletApplication.wallet.setBalances(result.balances)
                    WalletApplication.userSession.setMinimumBalance(MinimumBalance(result))
                    WalletApplication.wallet.setAvailableBalance(AccountUtils.calculateAvailableBalance())

                    liveData.postValue(AccountEvent(200, result))
                }
            }

            override fun onError(error: ErrorResponse) {
                Timber.e("Error Loading account")
                liveData.postValue(AccountEvent(error.code, null))
            }

        }).execute()
        return liveData
    }

    data class AccountEvent(val httpCode:Int, val accountResponse: AccountResponse?)

}