package ui.currencyapp.ui.app

import android.app.Application
import ui.currencyapp.di.initDI

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initDI()
    }
}
