package technology.ezequieldevteam.ettoolbox.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.BuildConfig
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.update.UpdateChecker

class SettingsFragment : Fragment() {

    private var _root: View? = null

    private fun ui(block: () -> Unit) {
        activity?.runOnUiThread {
            if (_root != null) block()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_settings, container, false)
        _root = v
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireContext().getSharedPreferences("et_settings", 0)

        val autoUpdate = view.findViewById<Switch>(R.id.sw_auto_update)
        val autoUpdateInterval = view.findViewById<Switch>(R.id.sw_auto_update_interval)
        val showRootBanner = view.findViewById<Switch>(R.id.sw_show_root_banner)
        val vibrateOnActions = view.findViewById<Switch>(R.id.sw_vibrate)

        autoUpdate.isChecked = prefs.getBoolean("auto_update", false)
        showRootBanner.isChecked = prefs.getBoolean("show_root_banner", true)
        vibrateOnActions.isChecked = prefs.getBoolean("vibrate_on_actions", true)

        autoUpdate.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("auto_update", checked).apply()
        }
        showRootBanner.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("show_root_banner", checked).apply()
            ui { updateRootBannerVisibility(checked) }
        }
        vibrateOnActions.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("vibrate_on_actions", checked).apply()
        }

        view.findViewById<Button>(R.id.btn_check_update).setOnClickListener { checkUpdateManual() }
        view.findViewById<Button>(R.id.btn_clear_cache).setOnClickListener { clearAppCache() }
        view.findViewById<Button>(R.id.btn_export_scripts).setOnClickListener { exportScripts() }
        view.findViewById<Button>(R.id.btn_import_scripts).setOnClickListener { importScripts() }
        view.findViewById<Button>(R.id.btn_reset_all).setOnClickListener { resetAllSettings() }

        val versionText = view.findViewById<TextView>(R.id.settings_version)
        versionText.text = getString(R.string.settings_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    private fun updateRootBannerVisibility(show: Boolean) {
        val activity = activity as? MainActivity
        activity?.binding?.rootBanner?.visibility = if (show && !EtApp.rootAvailable) View.VISIBLE else View.GONE
    }

    private fun checkUpdateManual() {
        val activity = activity as? MainActivity
        activity?.checkUpdate(manual = true)
    }

    private fun clearAppCache() {
        try {
            requireContext().cacheDir.deleteRecursively()
            Toast.makeText(context, R.string.settings_cache_cleared, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.settings_cache_fail, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportScripts() {
        val scriptPrefs = requireContext().getSharedPreferences("et_scripts", 0)
        val count = scriptPrefs.getInt("count", 0)
        if (count == 0) {
            Toast.makeText(context, R.string.settings_no_scripts, Toast.LENGTH_SHORT).show()
            return
        }
        val json = StringBuilder("[\n")
        for (i in 0 until count) {
            val name = scriptPrefs.getString("script_name_$i", "")
            val content = scriptPrefs.getString("script_content_$i", "")
            if (name.isNotBlank()) {
                json.append("  {\"name\": \"").append(name.replace("\"", "\\\"")).append("\", \"content\": \"")
                    .append(content.replace("\"", "\\\"").replace("\n", "\\n")).append("\"},\n")
            }
        }
        if (json.length > 2) json.setLength(json.length - 2)
        json.append("\n]")

        val fileName = "et_scripts_backup_${System.currentTimeMillis()}.json"
        try {
            val file = requireContext().getExternalFilesDir(null)?.let { java.io.File(it, fileName) }
            file?.writeText(json.toString())
            Toast.makeText(context, getString(R.string.settings_exported, file?.absolutePath), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.settings_export_fail, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun importScripts() {
        // TODO: Implement file picker
        Toast.makeText(context, R.string.settings_import_todo, Toast.LENGTH_SHORT).show()
    }

    private fun resetAllSettings() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_reset_title)
            .setMessage(R.string.settings_reset_body)
            .setPositiveButton(R.string.reset) { _, _ ->
                requireContext().getSharedPreferences("et_settings", 0).edit().clear().apply()
                requireContext().getSharedPreferences("et_scripts", 0).edit().clear().apply()
                Toast.makeText(context, R.string.settings_reset_done, Toast.LENGTH_SHORT).show()
                requireActivity().recreate()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}