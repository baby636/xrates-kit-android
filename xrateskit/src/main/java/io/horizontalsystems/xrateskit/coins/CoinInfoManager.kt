package io.horizontalsystems.xrateskit.coins

import android.content.Context
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

class CoinInfoManager(
    private val context: Context,
    private val storage: IStorage
) {

    private val coinInfoFileName = "coins.json"

    private fun updateCoinInfo() {
        var coinsResponse = CoinInfoResource.parseFile(true, context, coinInfoFileName)
        val resourceInfo = storage.getResourceInfo(ResourceType.COIN_INFO)

        val update = resourceInfo?.let {
            coinsResponse.version != it.version
        } ?: true

        if (update) {
            coinsResponse = CoinInfoResource.parseFile(false, context, coinInfoFileName)
            storage.deleteAllCoinCategories()
            storage.deleteAllCoinLinks()
            storage.deleteAllCoinsCategories()
            storage.deleteAllCoinFunds()
            storage.deleteAllCoinsFunds()
            storage.deleteAllCoinFundCategories()
            storage.deleteAllExchangeInfo()

            storage.saveCoinInfos(coinsResponse.coinInfos)
            storage.saveCoinCategories(coinsResponse.coinsCategories)
            storage.saveCoinCategory(coinsResponse.categories)
            storage.saveCoinFund(coinsResponse.funds)
            storage.saveCoinFunds(coinsResponse.coinFunds)
            storage.saveCoinFundCategory(coinsResponse.fundCategories)
            storage.saveCoinLinks(coinsResponse.links)
            storage.saveExchangeInfo(coinsResponse.exchangeInfos)
            storage.saveResourceInfo(ResourceInfo(ResourceType.COIN_INFO, coinsResponse.version))
        }
    }

    fun sync(): Single<Unit> {
        return Single.create { emitter ->
            updateCoinInfo()
            emitter.onSuccess(Unit)
        }
    }

    fun getCoinInfoDescription(coinType: CoinType): String? {
        return storage.getCoinInfo(coinType)?.description
    }

    fun getCoinRating(coinType: CoinType): String? {
        return storage.getCoinInfo(coinType)?.rating
    }

    fun getExchangeInfo(exchagenId: String): ExchangeInfoEntity? {
        return storage.getExchangeInfo(exchagenId)
    }

    fun getCoinCategories(coinType: CoinType): List<CoinCategory> {
        return storage.getCoinInfo(coinType)?.let {
            storage.getCoinCategories(it.coinType)
        } ?: emptyList()
    }

    fun getCoinFundCategories(coinType: CoinType): List<CoinFundCategory> {

        return storage.getCoinInfo(coinType)?.let {
            val funds = storage.getCoinFunds(it.coinType)
            val categories = storage.getCoinFundCategories(funds.map { it.categoryId })

            categories.forEach {  category ->
                category.funds.addAll(funds.filter { it.categoryId.contentEquals(category.id) })
            }
            categories
        } ?: emptyList()
    }

    fun getCoinCodesByCategory(categoryId: String): List<CoinType> {
        val coinInfoEntity = storage.getCoinInfosByCategory(categoryId)
        return coinInfoEntity.map { it.coinType }
    }

    fun getCoinRatingsAsync(): Single<Map<CoinType, String>> =
        Single.create { emitter ->
            try {
                val coinRatingsMap = mutableMapOf<CoinType, String>()

                storage.getCoinInfos().forEach { coin ->
                    coin.rating?.let {
                        if (it.isNotEmpty()) {
                            coinRatingsMap[coin.coinType] = it
                        }
                    }
                }
                emitter.onSuccess(coinRatingsMap)
            } catch (error: Throwable) {
                emitter.onError(error)
            }
        }

    fun getLinks(coinType: CoinType, linksByProvider: Map<LinkType, String>): Map<LinkType, String> {
        val links = mutableMapOf<LinkType, String>()
        val linksStored = storage.getCoinLinks(coinType).map { it.linkType to it.link }.toMap()

        LinkType.values().forEach { linkType ->
            val ls = linksStored[linkType]
            val lp = linksByProvider[linkType]

            val link =
                if(ls != null && ! ls.isNullOrEmpty()) ls
                else if(lp!=null && !lp.isNullOrEmpty()) lp
                else null

            link?.let{
                links.put(linkType, link)
            }
        }

        return links
    }
}