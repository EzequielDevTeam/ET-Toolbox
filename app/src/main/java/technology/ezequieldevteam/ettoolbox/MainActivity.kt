package technology.ezequieldevteam.ettoolbox

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.databinding.ActivityMainBinding
import technology.ezequieldevteam.ettoolbox.ui.boost.BoostFragment
import technology.ezequieldevteam.ettoolbox.ui.clean.CleanFragment
import technology.ezequieldevteam.ettoolbox.ui.device.DeviceFragment
import technology.ezequieldevteam.ettoolbox.ui.modules.ModulesFragment
import technology.ezequieldevteam.ettoolbox.ui.troll.TrollFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var wasRooted = false

    private val rootListener: (Boolean) -> Unit = { granted ->
        binding.rootBanner.visibility = if (granted) View.GONE else View.VISIBLE
        if (granted && !wasRooted) {
            Toast.makeText(this, R.string.root_granted, Toast.LENGTH_SHORT).show()
        }
        wasRooted = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }

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

        binding.btnGrantRoot.setOnClickListener {
            EtApp.requestRoot()
        }
    }

    override fun onStart() {
        super.onStart()
        EtApp.addRootListener(rootListener)
        EtApp.requestRoot()
    }

    override fun onStop() {
        super.onStop()
        EtApp.removeRootListener(rootListener)
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
