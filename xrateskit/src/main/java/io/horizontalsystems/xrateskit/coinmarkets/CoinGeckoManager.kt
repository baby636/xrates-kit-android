package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.xrateskit.api.CoinGeckoProvider
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.TimePeriod
import io.horizontalsystems.xrateskit.entities.CoinMarket
import io.horizontalsystems.xrateskit.entities.ProviderCoinInfo
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CoinGeckoManager(
    private val coinGeckoProvider: CoinGeckoProvider,
    private val storage: IStorage,
    private val factory: Factory
): IInfoManager, ICoinMarketManager {

    private fun getCoinIds(coins: List<Coin>): Single<List<String>> {

        val coinInfosSingle: Single<List<ProviderCoinInfo>>

        if(storage.getProviderCoinsInfoCount(coinGeckoProvider.provider.id) != 0)
            coinInfosSingle = Single.just(storage.getProviderCoinInfoByCodes(coinGeckoProvider.provider.id, coins.map { it.code }))
        else{
            coinInfosSingle = coinGeckoProvider.getProviderCoinInfoAsync()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map {
                    storage.saveProviderCoinInfo(it)
                    storage.getProviderCoinInfoByCodes(coinGeckoProvider.provider.id, coins.map { it.code })
                }
        }

        return coinInfosSingle.map {
            it.map { coinInfo -> coinInfo.providerCoinId }
        }

    }

    override fun getTopCoinMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>> {
        return coinGeckoProvider
            .getTopCoinMarketsAsync(currency, fetchDiffPeriod, itemsCount)
            .map { topMarkets ->
                val marketEntityList = topMarkets.map { factory.createMarketInfoEntity(it.coin, it.marketInfo) }
                storage.saveMarketInfo(marketEntityList)
                topMarkets
            }
    }

    override fun getCoinMarketsAsync(coins: List<Coin>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>> {
        return getCoinIds(coins).flatMap { coinIds ->
            coinGeckoProvider
                .getCoinMarketsAsync(coinIds, currencyCode, fetchDiffPeriod)
                .map { markets ->
                    val marketEntityList = markets.map { factory.createMarketInfoEntity(it.coin, it.marketInfo) }
                    storage.saveMarketInfo(marketEntityList)
                    markets
                }.onErrorReturn {
                    val marketEntities = storage.getOldMarketInfo(coins.map { it.code }, currencyCode)
                    marketEntities.map { CoinMarket(Coin(it.coinCode), factory.createMarketInfo(it)) }
                }
        }
    }

    override fun destroy() {
        coinGeckoProvider.destroy()
    }
}
