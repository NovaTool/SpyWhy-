package com.spywhy.wallet.core.di

import com.spywhy.wallet.core.network.api.CoinGeckoApi
import com.spywhy.wallet.core.network.api.SimpleSwapApi
import com.spywhy.wallet.core.network.api.ChangeNowApi
import com.spywhy.wallet.core.network.api.SideShiftApi
import com.spywhy.wallet.core.network.api.OneInchApi
import com.spywhy.wallet.core.util.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideCoinGeckoApi(client: OkHttpClient, moshi: Moshi): CoinGeckoApi {
        return Retrofit.Builder()
            .baseUrl(Constants.COINGECKO_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CoinGeckoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSimpleSwapApi(client: OkHttpClient, moshi: Moshi): SimpleSwapApi {
        return Retrofit.Builder()
            .baseUrl(Constants.SIMPLESWAP_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SimpleSwapApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChangeNowApi(client: OkHttpClient, moshi: Moshi): ChangeNowApi {
        return Retrofit.Builder()
            .baseUrl(Constants.CHANGENOW_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ChangeNowApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSideShiftApi(client: OkHttpClient, moshi: Moshi): SideShiftApi {
        return Retrofit.Builder()
            .baseUrl(Constants.SIDESHIFT_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SideShiftApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOneInchApi(client: OkHttpClient, moshi: Moshi): OneInchApi {
        return Retrofit.Builder()
            .baseUrl(Constants.ONEINCH_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OneInchApi::class.java)
    }
}
