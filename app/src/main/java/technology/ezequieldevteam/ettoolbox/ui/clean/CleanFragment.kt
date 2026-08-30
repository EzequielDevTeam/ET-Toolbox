package technology.ezequieldevteam.ettoolbox.ui.clean

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.data.BloatCatalog
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
import technology.ezequieldevteam.ettoolbox.core.repo.SysRepo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class CleanFragment : Fragment() {

    private var _root: View? = null
    private var showSmartScanner = false
    private var smartScannerResults: List<SmartBloatItem> = emptyList()

    private fun ui(block: () -> Unit) {
        activity?.runOnUiThread {
            if (_root != null) block()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_clean, container, false)
        _root = v
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btn_clean_cache).setOnClickListener { cleanCache() }
        view.findViewById<Button>(R.id.btn_smart_scan).setOnClickListener { runSmartScan() }
        view.findViewById<Button>(R.id.btn_disable_all_smart).setOnClickListener { disableAllSmart() }
        view.findViewById<CheckBox>(R.id.cb_show_smart).setOnCheckedChangeListener { _, checked ->
            showSmartScanner = checked
            updateBloatList()
        }
        setupBloatList(view)
        loadStorage(view)
    }

    private fun loadStorage(view: View) {
        val storageView = view.findViewById<TextView>(R.id.storage_free)
        SysRepo.storageFree { line ->
            ui {
                if (line.isNotBlank()) storageView.text = line
            }
        }
    }

    private fun cleanCache() {
        val view = _root ?: return
        val btn = view.findViewById<Button>(R.id.btn_clean_cache)
        val status = view.findViewById<TextView>(R.id.clean_status)
        btn.isEnabled = false
        status.text = getString(R.string.clean_working)

        EtApp.requestRoot { granted ->
            if (!granted) {
                ui {
                    Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_LONG).show()
                    btn.isEnabled = true
                    status.text = ""
                }
                return@requestRoot
            }
            Thread {
                Root.ok("pm trim-caches 999999999999")
                SysRepo.storageFree { freed ->
                    ui {
                        btn.isEnabled = true
                        status.text = getString(
                            R.string.clean_cache_done,
                            if (freed.isBlank()) "ok" else freed
                        )
                        loadStorage(view)
                    }
                }
            }.start()
        }
    }

    private fun runSmartScan() {
        val view = _root ?: return
        val btn = view.findViewById<Button>(R.id.btn_smart_scan)
        val status = view.findViewById<TextView>(R.id.clean_status)
        btn.isEnabled = false
        status.text = getString(R.string.clean_smart_scanning)

        EtApp.requestRoot { granted ->
            if (!granted) {
                ui {
                    Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_LONG).show()
                    btn.isEnabled = true
                    status.text = ""
                }
                return@requestRoot
            }
            thread(name = "et-smart-scan") {
                val results = scanForUnusedApps()
                ui {
                    btn.isEnabled = true
                    smartScannerResults = results
                    showSmartScanner = true
                    view.findViewById<CheckBox>(R.id.cb_show_smart).isChecked = true
                    status.text = getString(R.string.clean_smart_found, results.size)
                    updateBloatList()
                }
            }
        }
    }

    private fun scanForUnusedApps(): List<SmartBloatItem> {
        val pm = requireContext().packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val results = mutableListOf<SmartBloatItem>()

        val knownBloat = BloatCatalog.all.map { it.packageName }.toSet()

        // Get last used time via dumpsys usagestats (requires root or usage stats permission)
        val lastUsedMap = getLastUsedTimes()

        for (app in installedApps) {
            val pkg = app.packageName
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdated = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            if (!isSystem || isUpdated) continue // Only system apps

            if (knownBloat.contains(pkg)) continue // Already in catalog

            val label = pm.getApplicationLabel(app).toString()
            val lastUsed = lastUsedMap[pkg] ?: 0L
            val daysSinceUsed = if (lastUsed > 0) (System.currentTimeMillis() - lastUsed) / (1000 * 60 * 60 * 24) else -1L

            // Consider unused if never used or not used in 30+ days
            val isUnused = lastUsed == 0L || daysSinceUsed >= 30

            if (isUnused) {
                results.add(SmartBloatItem(
                    packageName = pkg,
                    label = label,
                    description = getString(R.string.clean_smart_unused, daysSinceUsed),
                    lastUsedMillis = lastUsed,
                    safe = true
                ))
            }
        }

        return results.sortedByDescending { it.lastUsedMillis }
    }

    private fun getLastUsedTimes(): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        try {
            val output = Root.cmd("dumpsys usagestats 2>/dev/null | grep -E 'pkg=|lastTimeActive=' | head -200")
            var currentPkg = ""
            for (line in output.lines()) {
                if (line.contains("pkg=")) {
                    currentPkg = line.substringAfter("pkg=").substringBefore(" ").trim()
                } else if (line.contains("lastTimeActive=") && currentPkg.isNotBlank()) {
                    val timeStr = line.substringAfter("lastTimeActive=").trim()
                    timeStr.toLongOrNull()?.let { map[currentPkg] = it }
                    currentPkg = ""
                }
            }
        } catch (_: Exception) {}
        return map
    }

    private fun disableAllSmart() {
        val toDisable = smartScannerResults.filter { it.selected }
        if (toDisable.isEmpty()) {
            Toast.makeText(context, R.string.clean_smart_none_selected, Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.clean_smart_disable_title)
            .setMessage(getString(R.string.clean_smart_disable_body, toDisable.size))
            .setPositiveButton(R.string.disable) { _, _ ->
                EtApp.requestRoot { granted ->
                    if (!granted) {
                        Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_SHORT).show()
                        return@requestRoot
                    }
                    thread {
                        var count = 0
                        for (item in toDisable) {
                            if (Root.ok("pm disable-user --user 0 ${item.packageName}")) count++
                        }
                        ui {
                            Toast.makeText(context, getString(R.string.clean_smart_disabled, count), Toast.LENGTH_LONG).show()
                            runSmartScan() // Refresh
                        }
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setupBloatList(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.bloat_list)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        EtApp.requestRoot { granted ->
            Thread {
                val installedPkgs =
                    if (granted) Root.cmd("pm list packages") else ""
                val catalogData = BloatCatalog.all.map {
                    it to installedPkgs.contains("package:${it.packageName}")
                }

                ui {
                    val adapter = BloatAdapter(catalogData) { item, wasDisabled ->
                        Thread {
                            if (wasDisabled) {
                                Root.ok("pm enable ${item.packageName}")
                            } else {
                                Root.ok("pm disable-user --user 0 ${item.packageName}")
                            }
                            ui {
                                (recycler.adapter as? BloatAdapter)?.notifyDataSetChanged()
                            }
                        }.start()
                    }
                    // Add smart scanner items at the end if enabled
                    if (showSmartScanner && smartScannerResults.isNotEmpty()) {
                        val smartData = smartScannerResults.map { it to false }
                        val combinedAdapter = CombinedBloatAdapter(catalogData, smartData, adapter.onToggle)
                        recycler.adapter = combinedAdapter
                    } else {
                        recycler.adapter = adapter
                    }
                }
            }.start()
        }
    }

    private fun updateBloatList() {
        _root?.let { setupBloatList(it) }
    }

    override fun onResume() {
        super.onResume()
        _root?.let {
            setupBloatList(it)
            loadStorage(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }

    data class SmartBloatItem(
        val packageName: String,
        val label: String,
        val description: String,
        val lastUsedMillis: Long,
        val safe: Boolean
    ) {
        var selected = false
    }
}

// Combined adapter for catalog + smart scanner
class CombinedBloatAdapter(
    private val catalogData: List<Pair<technology.ezequieldevteam.ettoolbox.core.data.BloatItem, Boolean>>,
    private val smartData: List<Pair<CleanFragment.SmartBloatItem, Boolean>>,
    private val onToggle: (technology.ezequieldevteam.ettoolbox.core.data.BloatItem, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_CATALOG = 0
        const val TYPE_SMART = 1
        const val TYPE_HEADER = 2
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return TYPE_HEADER
        if (position <= catalogData.size) return TYPE_CATALOG
        if (position == catalogData.size + 1) return TYPE_HEADER
        return TYPE_SMART
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderVH(inflater.inflate(R.layout.item_bloat_header, parent, false))
            TYPE_CATALOG -> BloatVH(inflater.inflate(R.layout.item_bloat, parent, false))
            else -> SmartVH(inflater.inflate(R.layout.item_smart_bloat, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderVH -> {
                holder.title.text = if (position == 0)
                    holder.itemView.context.getString(R.string.clean_bloat_catalog)
                else
                    holder.itemView.context.getString(R.string.clean_smart_results)
            }
            is BloatVH -> {
                val idx = position - 1
                if (idx < catalogData.size) {
                    val (item, installed) = catalogData[idx]
                    holder.bind(item, installed, onToggle)
                }
            }
            is SmartVH -> {
                val idx = position - catalogData.size - 2
                if (idx < smartData.size) {
                    val (item, _) = smartData[idx]
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 1 + catalogData.size + (if (smartData.isNotEmpty()) 1 + smartData.size else 0)
    }

    inner class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.bloat_header_title)
    }

    inner class BloatVH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.bloat_name)
        val desc: TextView = view.findViewById(R.id.bloat_desc)
        val status: TextView = view.findViewById(R.id.bloat_status)
        val toggle: Button = view.findViewById(R.id.bloat_toggle)
        val safeBadge: TextView = view.findViewById(R.id.bloat_safe_badge)

        fun bind(item: technology.ezequieldevteam.ettoolbox.core.data.BloatItem, installed: Boolean, onToggle: (technology.ezequieldevteam.ettoolbox.core.data.BloatItem, Boolean) -> Unit) {
            name.text = item.label
            desc.text = item.description
            status.text = if (installed) view.context.getString(R.string.clean_installed) else view.context.getString(R.string.clean_not_installed)
            status.setTextColor(if (installed) view.context.getColor(android.R.color.holo_green_dark) else view.context.getColor(android.R.color.holo_red_dark))
            safeBadge.visibility = if (item.safe) View.VISIBLE else View.GONE
            toggle.text = if (installed) view.context.getString(R.string.clean_disable) else view.context.getString(R.string.clean_enable)
            toggle.setOnClickListener { onToggle(item, installed) }
        }
    }

    inner class SmartVH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.smart_name)
        val desc: TextView = view.findViewById(R.id.smart_desc)
        val lastUsed: TextView = view.findViewById(R.id.smart_last_used)
        val checkBox: CheckBox = view.findViewById(R.id.smart_checkbox)

        fun bind(item: CleanFragment.SmartBloatItem) {
            name.text = item.label
            desc.text = item.packageName
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            lastUsed.text = if (item.lastUsedMillis > 0)
                view.context.getString(R.string.clean_smart_last_used, sdf.format(Date(item.lastUsedMillis)))
            else
                view.context.getString(R.string.clean_smart_never_used)
            checkBox.isChecked = item.selected
            checkBox.setOnCheckedChangeListener { _, checked -> item.selected = checked }
        }
    }
}