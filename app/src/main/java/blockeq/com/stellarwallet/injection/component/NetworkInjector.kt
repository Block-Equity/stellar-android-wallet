package blockeq.com.stellarwallet.injection.component

import blockeq.com.stellarwallet.injection.module.NetworkModule
import blockeq.com.stellarwallet.models.SupportedAsset
import dagger.Component
import javax.inject.Singleton

/**
 * Component providing inject() methods for presenters.
 */
@Singleton
@Component(modules = [(NetworkModule::class)])
interface Network {
    /**
     * Injects required dependencies into the specified SupportedAsset.
     * @param supportedAsset SupportedAsset in which to inject the dependencies
     */
    fun inject(supportedAsset: SupportedAsset)

    @Component.Builder
    interface Builder {
        fun build(): Network

        fun networkModule(networkModule: NetworkModule): Builder
    }
}