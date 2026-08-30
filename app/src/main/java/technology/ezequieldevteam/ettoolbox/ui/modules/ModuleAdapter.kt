package technology.ezequieldevteam.ettoolbox.ui.modules

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.model.MagiskRow

class ModuleAdapter(
    private var items: List<MagiskRow>,
    private val onToggle: (MagiskRow) -> Unit,
    private val onRemove: (MagiskRow) -> Unit,
    private val onUpdate: (MagiskRow) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.Holder>() {

    private var moduleUpdates: Map<String, ModuleUpdateInfo> = emptyMap()
    private var showUpdates = false

    fun replace(
        newItems: List<MagiskRow>,
        updates: Map<String, ModuleUpdateInfo> = emptyMap(),
        showUpdates: Boolean = false
    ) {
        items = newItems
        moduleUpdates = updates
        this.showUpdates = showUpdates
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.module_name)
        val version: TextView = view.findViewById(R.id.module_version)
        val state: TextView = view.findViewById(R.id.module_state)
        val btnToggle: Button = view.findViewById(R.id.module_toggle)
        val btnRemove: Button = view.findViewById(R.id.module_remove)
        val btnUpdate: Button = view.findViewById(R.id.module_update)
        val updateBadge: TextView = view.findViewById(R.id.module_update_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_module, parent, false)
        return Holder(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val m = items[position]
        holder.name.text = m.name.ifBlank { m.id }
        holder.version.text = m.version
        holder.state.text = when {
            m.markedRemove -> holder.itemView.context.getString(R.string.modules_state_del)
            m.enabled -> holder.itemView.context.getString(R.string.modules_state_on)
            else -> holder.itemView.context.getString(R.string.modules_state_off)
        }

        val updateInfo = moduleUpdates[m.id]
        val hasUpdate = showUpdates && updateInfo?.hasUpdate == true
        holder.updateBadge.visibility = if (hasUpdate) View.VISIBLE else View.GONE
        holder.btnUpdate.visibility = if (hasUpdate) View.VISIBLE else View.GONE
        if (hasUpdate) {
            holder.updateBadge.text = holder.itemView.context.getString(R.string.module_update_available, updateInfo!!.latestVersion)
        }

        holder.btnToggle.setOnClickListener { onToggle(m) }
        holder.btnRemove.setOnClickListener { onRemove(m) }
        holder.btnUpdate.setOnClickListener { onUpdate(m) }
    }
}