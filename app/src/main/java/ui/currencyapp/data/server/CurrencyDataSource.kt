package ui.currencyapp.data.server

import ui.currencyapp.data.toDomainCurrency
import ui.currencyapp.domain.Currency
import ui.currencyapp.source.RemoteDataSource

class CurrencyDataSource : RemoteDataSource {

    override suspend fun getLatestCurrencies(base: String): Currency {
        return CurrencyDb.service
            .getAllCurrencies(base, "GBP,EUR,JPY,BRL").await()
            .toDomainCurrency()
    }
}