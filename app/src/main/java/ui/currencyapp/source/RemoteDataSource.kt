package ui.currencyapp.source

import ui.currencyapp.domain.Currency

interface RemoteDataSource {
    suspend fun getLatestCurrencies(base: String): Currency
}