package technology.ezequieldevteam.ettoolbox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.databinding.ActivityMainBinding
import technology.ezequieldevteam.ettoolbox.ui.boost.BoostFragment
import technology.ezequieldevteam.ettoolbox.ui.clean.CleanFragment
import technology.ezequieldevteam.ettoolbox.ui.device.DeviceFragment
import technology.ezequieldevteam.ettoolbox.ui.modules.ModulesFragment
import technology.ezequieldevteam.ettoolbox.ui.troll.TrollFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) show(BoostFragment())

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_boost -> BoostFragment()
                R.id.nav_troll -> TrollFragment()
                R.id.nav_clean -> CleanFragment()
                R.id.nav_modules -> ModulesFragment()
                else -> DeviceFragment()
            }
            show(fragment)
            true
        }
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
