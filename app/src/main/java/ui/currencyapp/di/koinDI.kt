package ui.currencyapp.di

import android.app.Application
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ui.currencyapp.data.database.CurrencyDatabase
import ui.currencyapp.data.database.RoomDataSource
import ui.currencyapp.data.server.CurrencyDataSource
import ui.currencyapp.repository.CurrencyRepository
import ui.currencyapp.source.LocalDataSource
import ui.currencyapp.source.RemoteDataSource
import ui.currencyapp.ui.main.CurrencyFragment
import ui.currencyapp.ui.main.CurrencyViewModel
import ui.currencyapp.usecases.GetLatestCurrencies

fun Application.initDI() {
    startKoin {
        androidLogger()
        androidContext(this@initDI)
        modules(listOf(appModule, dataModule, scopesModule))
    }
}

private val appModule = module {
    single { CurrencyDatabase.build(get()) }
    factory<LocalDataSource> { RoomDataSource(get()) }
    factory<RemoteDataSource> { CurrencyDataSource() }
    single<CoroutineDispatcher> { Dispatchers.Main }
}

val dataModule = module {
    factory { CurrencyRepository(get(), get()) }
}

private val scopesModule = module {
    scope(named<CurrencyFragment>()) {
        viewModel { CurrencyViewModel(get(), get()) }
        scoped { GetLatestCurrencies(get()) }
    }
}