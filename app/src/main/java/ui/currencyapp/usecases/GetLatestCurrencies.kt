package ui.currencyapp.usecases

import ui.currencyapp.repository.CurrencyRepository
import ui.currencyapp.domain.Currency

class GetLatestCurrencies(private val currenciesRepository: CurrencyRepository) {
    suspend fun invoke(): List<Currency> = currenciesRepository.getLatestCurrencies()
}