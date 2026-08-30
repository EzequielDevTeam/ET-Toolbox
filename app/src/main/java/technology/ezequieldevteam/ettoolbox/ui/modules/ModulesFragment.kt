package technology.ezequieldevteam.ettoolbox.ui.modules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.model.MagiskRow
import technology.ezequieldevteam.ettoolbox.core.repo.MagiskRepo
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import kotlin.concurrent.thread

class ModulesFragment : Fragment() {

    private var _root: View? = null
    private var allModules: List<MagiskRow> = emptyList()
    private var moduleUpdates: Map<String, ModuleUpdateInfo> = emptyMap()
    private var showUpdates = false

    private fun ui(block: () -> Unit) {
        activity?.runOnUiThread {
            if (_root != null) block()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_modules, container, false)
        _root = v
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.modules_list)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = ModuleAdapter(
            emptyList(),
            onToggle = { m -> toggle(m) },
            onRemove = { m -> remove(m) },
            onUpdate = { m -> updateModule(m) }
        )

        view.findViewById<TextInputEditText>(R.id.modules_search).addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    applyFilter()
                }
            }
        )

        view.findViewById<Button>(R.id.btn_check_updates).setOnClickListener { checkUpdates() }
        view.findViewById<Button>(R.id.btn_update_all).setOnClickListener { updateAll() }
        view.findViewById<CheckBox>(R.id.cb_show_updates).setOnCheckedChangeListener { _, checked ->
            showUpdates = checked
            applyFilter()
        }

        load()
    }

    private fun load() {
        val view = _root ?: return
        val status = view.findViewById<TextView>(R.id.modules_status)
        val errorView = view.findViewById<TextView>(R.id.modules_error)
        status.text = getString(R.string.modules_loading)
        errorView.visibility = View.GONE

        if (!EtApp.rootAvailable) {
            EtApp.requestRoot { granted ->
                if (_root == null) return@requestRoot
                if (granted) {
                    load()
                } else {
                    status.text = getString(R.string.modules_need_root)
                }
            }
            return
        }

        MagiskRepo.list { rows, error ->
            ui {
                val v = _root ?: return@ui
                if (error != null) {
                    status.text = ""
                    val err = v.findViewById<TextView>(R.id.modules_error)
                    err.visibility = View.VISIBLE
                    err.text = getString(R.string.modules_error_prefix, error)
                    allModules = emptyList()
                    applyFilter()
                    return@ui
                }
                allModules = rows
                status.text =
                    if (rows.isEmpty()) getString(R.string.modules_none)
                    else getString(R.string.modules_count, rows.size)
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val view = _root ?: return
        val query = view.findViewById<TextInputEditText>(R.id.modules_search)
            .text?.toString()?.trim().orEmpty()
        val filtered =
            if (query.isBlank()) allModules
            else allModules.filter {
                it.name.contains(query, true) || it.id.contains(query, true)
            }
        val recycler = view.findViewById<RecyclerView>(R.id.modules_list)
        (recycler.adapter as? ModuleAdapter)?.replace(filtered, moduleUpdates, showUpdates)
    }

    private fun checkUpdates() {
        val view = _root ?: return
        val btn = view.findViewById<Button>(R.id.btn_check_updates)
        val status = view.findViewById<TextView>(R.id.modules_status)
        btn.isEnabled = false
        status.text = getString(R.string.modules_checking_updates)

        thread(name = "et-module-updates") {
            val updates = mutableMapOf<String, ModuleUpdateInfo>()
            for (module in allModules) {
                val info = fetchModuleUpdate(module.id, module.version)
                info?.let { updates[module.id] = it }
            }
            ui {
                btn.isEnabled = true
                moduleUpdates = updates
                val hasUpdates = updates.values.any { it.hasUpdate }
                status.text = getString(if (hasUpdates) R.string.modules_updates_found else R.string.modules_no_updates, updates.values.count { it.hasUpdate })
                applyFilter()
            }
        }
    }

    private fun fetchModuleUpdate(moduleId: String, currentVersion: String): ModuleUpdateInfo? {
        // Try to fetch from GitHub releases of known module repos
        // This is a simplified version - in reality you'd need a module index
        return try {
            // For now, just return null - this would need a proper module repository index
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun updateModule(module: MagiskRow) {
        val updateInfo = moduleUpdates[module.id]
        if (updateInfo == null || !updateInfo.hasUpdate) {
            Toast.makeText(context, R.string.modules_no_update_available, Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.modules_update_title)
            .setMessage(getString(R.string.modules_update_body, module.name, updateInfo.latestVersion))
            .setPositiveButton(R.string.modules_update) { _, _ ->
                downloadAndInstallUpdate(module, updateInfo)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun downloadAndInstallUpdate(module: MagiskRow, info: ModuleUpdateInfo) {
        val view = _root ?: return
        val status = view.findViewById<TextView>(R.id.modules_status)
        status.text = getString(R.string.modules_downloading, module.name)

        // This would download the module zip and place it in /data/adb/modules/
        // For now, just open the download URL
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(info.downloadUrl))
            startActivity(intent)
            Toast.makeText(context, R.string.modules_open_download, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.modules_download_failed, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAll() {
        val toUpdate = allModules.filter { moduleUpdates[it.id]?.hasUpdate == true }
        if (toUpdate.isEmpty()) {
            Toast.makeText(context, R.string.modules_no_updates_to_install, Toast.LENGTH_SHORT).show()
            return
        }
        // Open each download URL
        for (module in toUpdate) {
            val info = moduleUpdates[module.id]
            info?.let {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(it.downloadUrl))
                    startActivity(intent)
                } catch (_: Exception) {}
            }
        }
        Toast.makeText(context, getString(R.string.modules_opened_downloads, toUpdate.size), Toast.LENGTH_LONG).show()
    }

    private fun toggle(m: MagiskRow) {
        MagiskRepo.toggle(m.id, m.enabled) { ok ->
            ui {
                Toast.makeText(
                    context,
                    if (ok) R.string.modules_toggled else R.string.modules_action_failed,
                    Toast.LENGTH_SHORT
                ).show()
                load()
            }
        }
    }

    private fun remove(m: MagiskRow) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.modules_remove_confirm_title)
            .setMessage(getString(R.string.modules_remove_confirm_body, m.name))
            .setPositiveButton(R.string.modules_remove) { _, _ ->
                MagiskRepo.markRemove(m.id) { ok ->
                    ui {
                        Toast.makeText(
                            context,
                            if (ok) R.string.modules_removed else R.string.modules_action_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                        load()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }

    data class ModuleUpdateInfo(
        val latestVersion: String,
        val downloadUrl: String,
        val changelog: String,
        val hasUpdate: Boolean
    )
}