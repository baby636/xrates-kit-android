package io.horizontalsystems.xrateskit.storage

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*

class Storage(private val database: Database) : IStorage {

    private val providerCoinDao = database.providerCoinsDao
    private val historicalRateDao = database.historicalRateDao
    private val chartPointDao = database.chartPointDao
    private val latestRateDao = database.latestRatesDao
    private val globalMarketInfoDao = database.globalMarketInfoDao
    private val coinInfoDao = database.coinInfoDao
    private val resourceInfoDao = database.resourceInfoDao

    //Resource
    override fun getResourceInfo(resourceType: ResourceType): ResourceInfo? {
        return resourceInfoDao.getResourceInfo(resourceType.name)
    }

    override fun saveResourceInfo(resourceInfo: ResourceInfo) {
        resourceInfoDao.insertResouceInfo(resourceInfo)
    }

    // Coin Info
    override fun getCoinInfoCount(): Int {
        return coinInfoDao.getCoinInfoCount()
    }

    override fun getCoinInfo(coinType: CoinType): CoinInfoEntity? {
        return coinInfoDao.getCoinInfo(coinType)
    }

    override fun getCoinLinks(coinType: CoinType): List<CoinLinksEntity> {
        return coinInfoDao.getCoinLinks(coinType)
    }

    override fun getCoinInfos(): List<CoinInfoEntity> {
        return coinInfoDao.getCoinInfos()
    }

    override fun getExchangeInfo(exchangeId: String): ExchangeInfoEntity? {
        return coinInfoDao.getExchangeInfo(exchangeId)
    }

    override fun getCoinCategories(coinType: CoinType): List<CoinCategory> {
        return coinInfoDao.getCoinCategories(coinType)
    }

    override fun getCoinFunds(coinType: CoinType): List<CoinFund> {
        return coinInfoDao.getCoinFunds(coinType)
    }

    override fun getCoinFundCategories(categoriesIds: List<String>): List<CoinFundCategory> {
        return coinInfoDao.getCoinFundCategories(categoriesIds)
    }

    override fun getCoinInfosByCategory(categoryId: String): List<CoinInfoEntity> {
        return coinInfoDao.getCoinInfoByCategory(categoryId)
    }

    override fun getCategorizedCoinTypes(): List<CoinType> {
        return coinInfoDao.getCategorizedCoinTypes()
    }

    override fun saveCoinInfos(coinInfos: List<CoinInfoEntity>) {
        coinInfoDao.insertCoinInfo(coinInfos)
    }

    override fun saveExchangeInfo(exchangeInfos: List<ExchangeInfoEntity>) {
        coinInfoDao.insertExchangeInfo(exchangeInfos)
    }

    override fun saveCoinCategories(coinCategoryEntities: List<CoinCategoriesEntity>) {
        coinInfoDao.insertCoinCategories(coinCategoryEntities)
    }

    override fun saveCoinCategory(coinCategories: List<CoinCategory>){
        coinInfoDao.insertCoinCategory(coinCategories)
    }

    override fun saveCoinFund(coinFunds: List<CoinFund>){
        coinInfoDao.insertCoinFund(coinFunds)
    }

    override fun saveCoinLinks(coinLinks: List<CoinLinksEntity>){
        coinInfoDao.insertCoinLinks(coinLinks)
    }

    override fun saveCoinFunds(coinFunds: List<CoinFundsEntity>){
        coinInfoDao.insertCoinFunds(coinFunds)
    }

    override fun saveCoinFundCategory(coinFundCategories: List<CoinFundCategory>){
        coinInfoDao.insertCoinFundCategory(coinFundCategories)
    }

    override fun deleteAllCoinCategories(){
        coinInfoDao.deleteAllCoinCategories()
    }

    override fun deleteAllExchangeInfo(){
        coinInfoDao.deleteAllExchangeInfo()
    }

    override fun deleteAllCoinsCategories(){
        coinInfoDao.deleteAllCoinsCategories()
    }

    override fun deleteAllCoinLinks(){
        coinInfoDao.deleteAllCoinLinks()
    }

    override fun deleteAllCoinFunds(){
        coinInfoDao.deleteAllCoinFunds()
    }

    override fun deleteAllCoinFundCategories(){
        coinInfoDao.deleteAllCoinFundCategories()
    }

    override fun deleteAllCoinsFunds(){
        coinInfoDao.deleteAllCoinsFunds()
    }


    // Provider Coin Info
    override fun searchCoins(searchText: String): List<ProviderCoinEntity> {
        return providerCoinDao.searchCoins(searchText)
    }

    override fun saveProviderCoins(providerCoins: List<ProviderCoinEntity>) {
        providerCoinDao.insertAll(providerCoins)
    }

    override fun getProviderCoins(coinTypes: List<CoinType>): List<ProviderCoinEntity> {
        return providerCoinDao.getProviderCoins(coinTypes)
    }

    override fun getProviderCoin(coinType: CoinType): ProviderCoinEntity? {
        return providerCoinDao.getProviderCoin(coinType)
    }

    override  fun getCoinTypesByProviderCoinId(providerCoinId: String, provider: InfoProvider): List<CoinType> {
        return when (provider) {
            is InfoProvider.CoinGecko -> providerCoinDao.getCoinTypesForCoinGecko(providerCoinId)
            is InfoProvider.CryptoCompare -> providerCoinDao.getCoinTypesForCryptoCompare(providerCoinId)
            else -> listOf()
        }
    }

    override fun clearPriorities() {
        providerCoinDao.resetPriorities(Int.MAX_VALUE)
    }

    override fun setPriorityForCoin(coinType: CoinType, priority: Int) {
        providerCoinDao.setPriorityForCoin(coinType, priority)
    }

    // HistoricalRate

    override fun saveHistoricalRate(rate: HistoricalRate) {
        historicalRateDao.insert(rate)
    }

    override fun getHistoricalRate(coinType: CoinType, currencyCode: String, timestamp: Long): HistoricalRate? {
        return historicalRateDao.getRate(coinType, currencyCode, timestamp)
    }

    //  ChartPoint

    override fun getChartPoints(key: ChartInfoKey): List<ChartPointEntity> {
        return chartPointDao.getList(key.coinType, key.currency, key.chartType)
    }

    override fun saveChartPoints(points: List<ChartPointEntity>) {
        chartPointDao.insert(points)
    }

    override fun deleteChartPoints(key: ChartInfoKey) {
        chartPointDao.delete(key.coinType, key.currency, key.chartType)
    }

    //  MarketStats

    override fun getLatestRate(coinType: CoinType, currencyCode: String): LatestRateEntity? {
        return latestRateDao.getLatestRate(coinType, currencyCode)
    }

    override fun getOldLatestRates(coinTypes: List<CoinType>, currencyCode: String): List<LatestRateEntity> {
        return latestRateDao.getOldList(coinTypes, currencyCode)
    }

    override fun saveLatestRates(marketInfoList: List<LatestRateEntity>) {
        latestRateDao.insertAll(marketInfoList)
    }

    // GlobalMarketInfo
    override fun getGlobalMarketPointInfo(currencyCode: String, timePeriod: TimePeriod): GlobalCoinMarketPointInfo? {
        return globalMarketInfoDao.getPointInfo(currencyCode, timePeriod)?.let {
            it.points.addAll(globalMarketInfoDao.getPoints(it.id))
            it
        }
    }

    override fun deleteGlobalMarketPointInfo(currencyCode: String, timePeriod: TimePeriod){
        globalMarketInfoDao.deletePointInfo(currencyCode, timePeriod)
    }

    override fun saveGlobalMarketPointInfo(globalCoinMarketPointInfo: GlobalCoinMarketPointInfo){
        globalMarketInfoDao.insertPointsInfoDetails(globalCoinMarketPointInfo)
    }

}
