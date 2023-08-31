package ui.currencyapp.ui.main


import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ui.currencyapp.R
import ui.currencyapp.navigation.Navigation
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        initViews()
    }

    private fun initViews() {
        initCurrencyFragment()
    }

    private fun initCurrencyFragment() {
        Navigation.replaceFragment(supportFragmentManager, R.id.fragmentContainer, CurrencyFragment())
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    companion object {
        private val CLASS_TAG = MainActivity::class.java.simpleName
    }
}

