package technology.ezequieldevteam.ettoolbox.ui.appmanager

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
import kotlin.concurrent.thread

class AppManagerFragment : Fragment() {

    private var _root: View? = null
    private var allApps: List<AppInfo> = emptyList()
    private var filteredApps: MutableList<AppInfo> = mutableListOf()
    private lateinit var adapter: AppAdapter
    private var showSystem = false
    private var showEnabledOnly = true

    private fun ui(block: () -> Unit) {
        activity?.runOnUiThread {
            if (_root != null) block()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_app_manager, container, false)
        _root = v
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.apps_list)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = AppAdapter(filteredApps, onClick = { toggleApp(it) }, onLongClick = { showAppInfo(it) })
        recycler.adapter = adapter

        val searchField = view.findViewById<EditText>(R.id.app_search)
        searchField.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                adapter.filter.filter(s)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        view.findViewById<CheckBox>(R.id.cb_show_system).setOnCheckedChangeListener { _, checked ->
            showSystem = checked
            adapter.filter.filter(searchField.text)
        }
        view.findViewById<CheckBox>(R.id.cb_enabled_only).setOnCheckedChangeListener { _, checked ->
            showEnabledOnly = checked
            adapter.filter.filter(searchField.text)
        }

        view.findViewById<Button>(R.id.btn_refresh_apps).setOnClickListener { loadApps() }
        view.findViewById<Button>(R.id.btn_batch_disable).setOnClickListener { batchDisable() }
        view.findViewById<Button>(R.id.btn_batch_enable).setOnClickListener { batchEnable() }

        loadApps()
    }

    private fun loadApps() {
        val view = _root ?: return
        val pm = requireContext().packageManager
        thread(name = "et-apps-load") {
            val list = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps = mutableListOf<AppInfo>()
            for (app in list) {
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdated = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                if (!showSystem && isSystem && !isUpdated) continue
                if (showEnabledOnly && !app.enabled) continue
                val label = pm.getApplicationLabel(app).toString()
                apps.add(AppInfo(app.packageName, label, app.sourceDir, app.enabled, isSystem))
            }
            ui {
                allApps = apps.sortedBy { it.label.lowercase() }
                adapter.filter.filter(view.findViewById<EditText>(R.id.app_search).text)
            }
        }
    }

    private fun toggleApp(app: AppInfo) {
        EtApp.requestRoot { granted ->
            if (!granted) {
                Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_SHORT).show()
                return@requestRoot
            }
            val newState = !app.enabled
            Root.submit("pm ${if (newState) "enable" else "disable-user --user 0"} ${app.packageName}") { ok, _ ->
                ui {
                    if (ok) {
                        val idx = filteredApps.indexOf(app)
                        if (idx >= 0) {
                            filteredApps[idx] = app.copy(enabled = newState)
                            adapter.notifyItemChanged(idx)
                        }
                        Toast.makeText(context, getString(if (newState) R.string.app_enabled_toast else R.string.app_disabled_toast, app.label), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, R.string.app_action_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showAppInfo(app: AppInfo) {
        AlertDialog.Builder(requireContext())
            .setTitle(app.label)
            .setMessage(getString(R.string.app_info_body, app.packageName, app.path, if (app.isSystem) getString(R.string.yes) else getString(R.string.no), if (app.enabled) getString(R.string.app_enabled) else getString(R.string.app_disabled)))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun batchDisable() {
        val selected = filteredApps.filter { !it.enabled }.take(10)
        if (selected.isEmpty()) {
            Toast.makeText(context, R.string.app_none_selected, Toast.LENGTH_SHORT).show()
            return
        }
        EtApp.requestRoot { granted ->
            if (!granted) {
                Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_SHORT).show()
                return@requestRoot
            }
            thread {
                var count = 0
                for (app in selected) {
                    if (Root.ok("pm disable-user --user 0 ${app.packageName}")) count++
                }
                ui {
                    Toast.makeText(context, getString(R.string.app_batch_done, count), Toast.LENGTH_LONG).show()
                    loadApps()
                }
            }
        }
    }

    private fun batchEnable() {
        val selected = filteredApps.filter { it.enabled }.take(10)
        if (selected.isEmpty()) {
            Toast.makeText(context, R.string.app_none_selected, Toast.LENGTH_SHORT).show()
            return
        }
        EtApp.requestRoot { granted ->
            if (!granted) {
                Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_SHORT).show()
                return@requestRoot
            }
            thread {
                var count = 0
                for (app in selected) {
                    if (Root.ok("pm enable ${app.packageName}")) count++
                }
                ui {
                    Toast.makeText(context, getString(R.string.app_batch_done, count), Toast.LENGTH_LONG).show()
                    loadApps()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }

    data class AppInfo(
        val packageName: String,
        val label: String,
        val path: String,
        var enabled: Boolean,
        val isSystem: Boolean
    )
}

class AppAdapter(
    initialList: List<AppManagerFragment.AppInfo>,
    private val onClick: (AppManagerFragment.AppInfo) -> Unit,
    private val onLongClick: (AppManagerFragment.AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>(), Filterable {

    private var items = initialList

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
        val packageName: TextView = view.findViewById(R.id.app_package)
        val status: TextView = view.findViewById(R.id.app_status)
        val toggle: CheckBox = view.findViewById(R.id.app_toggle)
        val systemBadge: TextView = view.findViewById(R.id.app_system_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.label
        holder.packageName.text = item.packageName
        holder.status.text = if (item.enabled) R.string.app_enabled else R.string.app_disabled
        holder.status.setTextColor(if (item.enabled) holder.itemView.context.getColor(android.R.color.holo_green_dark) else holder.itemView.context.getColor(android.R.color.holo_red_dark))
        holder.toggle.isChecked = item.enabled
        holder.toggle.setOnCheckedChangeListener(null)
        holder.toggle.setOnCheckedChangeListener { _, checked -> if (checked != item.enabled) onClick(item) }
        holder.systemBadge.visibility = if (item.isSystem) View.VISIBLE else View.GONE
        try {
            holder.icon.setImageDrawable(holder.itemView.context.packageManager.getApplicationIcon(item.packageName))
        } catch (_: Exception) {
            holder.icon.setImageResource(android.R.drawable.sym_def_app_icon)
        }
        holder.itemView.setOnClickListener { onClick(item) }
        holder.itemView.setOnLongClickListener { onLongClick(item); true }
    }

    override fun getItemCount() = items.size

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val query = constraint?.toString()?.trim()?.lowercase() ?: ""
            val results = FilterResults()
            results.values = if (query.isBlank()) allApps else allApps.filter { it.label.lowercase().contains(query) || it.packageName.lowercase().contains(query) }
            results.count = results.values.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            items = (results?.values as? List<AppManagerFragment.AppInfo>) ?: emptyList()
            notifyDataSetChanged()
        }
    }

    companion object {
        @Volatile
        var allApps: List<AppManagerFragment.AppInfo> = emptyList()
    }
}