package technology.ezequieldevteam.ettoolbox.ui.clean

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.model.BloatItem
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root

class BloatAdapter(
    private val items: List<Pair<BloatItem, Boolean>>,
    private val onToggle: (BloatItem, Boolean) -> Unit
) : RecyclerView.Adapter<BloatAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.bloat_label)
        val pkg: TextView = view.findViewById(R.id.bloat_pkg)
        val desc: TextView = view.findViewById(R.id.bloat_desc)
        val state: TextView = view.findViewById(R.id.bloat_state)
        val btn: Button = view.findViewById(R.id.bloat_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bloat, parent, false)
        return Holder(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val (item, installed) = items[position]
        holder.label.text = item.label
        holder.pkg.text = item.packageName
        holder.desc.text = item.description

        if (!installed) {
            holder.state.text = holder.itemView.context.getString(R.string.clean_not_installed)
            holder.btn.isEnabled = false
            holder.btn.text = "-"
            return
        }

        val disabled = Root.cmd("pm list packages -d").contains(item.packageName)
        holder.state.text =
            if (disabled) holder.itemView.context.getString(R.string.clean_disabled) else ""
        holder.btn.text =
            if (disabled) holder.itemView.context.getString(R.string.clean_enable)
            else holder.itemView.context.getString(R.string.clean_disable)

        holder.btn.setOnClickListener { onToggle(item, disabled) }
    }
}
