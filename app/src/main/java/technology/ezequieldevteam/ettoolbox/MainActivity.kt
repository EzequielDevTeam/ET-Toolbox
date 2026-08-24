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
import technology.ezequieldevteam.ettoolbox.update.ApkInstaller
import technology.ezequieldevteam.ettoolbox.update.UpdateChecker

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

        binding.topToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_credits -> {
                    CreditsDialog.show(this)
                    true
                }
                R.id.action_update -> {
                    checkUpdate(manual = true)
                    true
                }
                else -> false
            }
        }

        binding.btnGrantRoot.setOnClickListener {
            EtApp.requestRoot()
        }

        if (savedInstanceState == null) {
            checkUpdate(manual = false)
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

    fun checkUpdate(manual: Boolean) {
        Toast.makeText(this, R.string.update_checking, Toast.LENGTH_SHORT).show()
        UpdateChecker.check { release, error ->
            if (isFinishing || isDestroyed) return@check
            if (release != null && UpdateChecker.isNewer(release.tag)) {
                askInstall(release)
            } else if (release != null) {
                if (manual) toast(R.string.update_up_to_date)
            } else if (manual) {
                Toast.makeText(this, error ?: "", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun askInstall(release: UpdateChecker.Release) {
        val notes = release.notes.take(600)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_found_title, release.tag))
            .setMessage(
                getString(R.string.update_found_body) +
                    (if (notes.isNotBlank()) "\n\n$notes" else "")
            )
            .setPositiveButton(R.string.update_download) { _, _ ->
                ApkInstaller.downloadAndInstall(this, release.tag, release.apkUrl)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun onInstallDownloadFailed() {
        toast(R.string.update_download_failed)
    }

    fun onInstallOpenFailed() {
        toast(R.string.update_install_failed)
    }

    private fun toast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
