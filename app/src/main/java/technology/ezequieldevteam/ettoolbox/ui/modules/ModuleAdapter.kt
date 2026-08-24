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
    private val onRemove: (MagiskRow) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.Holder>() {

    fun replace(newItems: List<MagiskRow>) {
        items = newItems
        notifyDataSetChanged()
    }

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

        holder.btnToggle.setOnClickListener { onToggle(m) }
        holder.btnRemove.setOnClickListener { onRemove(m) }
    }
}
