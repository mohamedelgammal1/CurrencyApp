package ui.currencyapp.repository

import ui.currencyapp.domain.Currency
import ui.currencyapp.source.LocalDataSource
import ui.currencyapp.source.RemoteDataSource

class CurrencyRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val BASE_CURRENCY: String = "USD"
) {

    suspend fun getLatestCurrencies(): List<Currency> {
        if (localDataSource.isEmpty()) {
            val currencies =
                remoteDataSource.getLatestCurrencies(BASE_CURRENCY)
            localDataSource.saveCurrencies(currencies)
        }

        return localDataSource.getLatestCurrencies()
    }
}