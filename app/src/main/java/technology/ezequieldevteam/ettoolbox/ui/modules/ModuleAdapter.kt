package technology.ezequieldevteam.ettoolbox.ui.modules

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import technology.ezequieldevteam.ettoolbox.R

class ModuleAdapter(
    private val modules: List<MagiskModule>,
    private val onToggle: (MagiskModule) -> Unit,
    private val onRemove: (MagiskModule) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.Holder>() {

    data class MagiskModule(
        val dirName: String,
        val name: String,
        val version: String,
        val enabled: Boolean,
        val markedRemove: Boolean
    )

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.module_name)
        val version: TextView = view.findViewById(R.id.module_version)
        val state: TextView = view.findViewById(R.id.module_state)
        val btnToggle: Button = view.findViewById(R.id.module_toggle)
        val btnRemove: Button = view.findViewById(R.id.module_remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_module, parent, false)
        return Holder(v)
    }

    override fun getItemCount() = modules.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val m = modules[position]
        holder.name.text = m.name.ifBlank { m.dirName }
        holder.version.text = m.version
        holder.state.text =
            if (m.markedRemove) "remocao pendente"
            else if (m.enabled) holder.itemView.context.getString(R.string.modules_state_on)
            else holder.itemView.context.getString(R.string.modules_state_off)

        holder.btnToggle.setOnClickListener { onToggle(m) }
        holder.btnRemove.setOnClickListener { onRemove(m) }
    }
}
