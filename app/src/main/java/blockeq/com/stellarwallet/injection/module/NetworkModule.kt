package blockeq.com.stellarwallet.injection

import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.services.networking.SupportedAssetsApi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Module which provides all required dependencies about network
 */
@Module
@Suppress("unused")
object NetworkModule {
    /**
     * Provides the Support API service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the SupportAssets service implementation.
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun provideSupportedAssetsApi(retrofit: Retrofit): SupportedAssetsApi {
        return retrofit.create(SupportedAssetsApi::class.java)
    }

    /**
     * Provides the Retrofit object.
     * @return the Retrofit object
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun provideRetrofitInterface(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(Constants.SUPPORTED_ASSETS_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
    }
}
