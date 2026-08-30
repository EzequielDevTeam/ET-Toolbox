package technology.ezequieldevteam.ettoolbox.ui.scripts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
import kotlin.concurrent.thread

class ScriptsFragment : Fragment() {

    private var _root: View? = null
    private val scripts = mutableListOf<ScriptItem>()
    private lateinit var adapter: ScriptAdapter

    private fun ui(block: () -> Unit) {
        activity?.runOnUiThread {
            if (_root != null) block()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_scripts, container, false)
        _root = v
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecycler(view)
        loadScripts()
        view.findViewById<Button>(R.id.btn_add_script).setOnClickListener { showAddDialog() }
        view.findViewById<Button>(R.id.btn_run_all).setOnClickListener { runAll() }
    }

    private fun setupRecycler(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.scripts_list)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = ScriptAdapter(scripts, onRun = { runScript(it) }, onEdit = { editScript(it) }, onDelete = { deleteScript(it) })
        recycler.adapter = adapter
    }

    private fun loadScripts() {
        val saved = requireContext().getSharedPreferences("et_scripts", 0)
        val count = saved.getInt("count", 0)
        scripts.clear()
        for (i in 0 until count) {
            val name = saved.getString("script_name_$i", "") ?: ""
            val content = saved.getString("script_content_$i", "") ?: ""
            if (name.isNotBlank()) scripts.add(ScriptItem(name, content))
        }
        adapter.notifyDataSetChanged()
    }

    private fun saveScripts() {
        val prefs = requireContext().getSharedPreferences("et_scripts", 0).edit()
        prefs.putInt("count", scripts.size)
        scripts.forEachIndexed { i, s ->
            prefs.putString("script_name_$i", s.name)
            prefs.putString("script_content_$i", s.content)
        }
        prefs.apply()
    }

    private fun showAddDialog(script: ScriptItem? = null) {
        val isEdit = script != null
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_script, null)
        val nameField = view.findViewById<EditText>(R.id.script_name)
        val contentField = view.findViewById<EditText>(R.id.script_content)

        if (isEdit) {
            val s = script!!
            nameField.setText(s.name)
            contentField.setText(s.content)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(if (isEdit) R.string.script_edit_title else R.string.script_add_title)
            .setView(view)
            .setPositiveButton(if (isEdit) R.string.save else R.string.script_add) { _, _ ->
                val name = nameField.text.toString().trim()
                val content = contentField.text.toString().trim()
                if (name.isBlank() || content.isBlank()) {
                    Toast.makeText(context, R.string.script_empty_error, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (isEdit) {
                    val idx = scripts.indexOf(script)
                    if (idx >= 0) scripts[idx] = ScriptItem(name, content)
                } else {
                    scripts.add(ScriptItem(name, content))
                }
                saveScripts()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun editScript(script: ScriptItem) = showAddDialog(script)

    private fun deleteScript(script: ScriptItem) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.script_delete_confirm)
            .setMessage(getString(R.string.script_delete_body, script.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                scripts.remove(script)
                saveScripts()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun runScript(script: ScriptItem) {
        val view = _root ?: return
        val btn = view.findViewById<Button>(R.id.btn_add_script)
        btn.isEnabled = false

        EtApp.requestRoot { granted ->
            if (!granted) {
                ui {
                    Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_LONG).show()
                    btn.isEnabled = true
                }
                return@requestRoot
            }
            thread(name = "et-script") {
                val (ok, out) = Root.runner.run(script.content)
                ui {
                    btn.isEnabled = true
                    Toast.makeText(context, if (ok) getString(R.string.script_done) else getString(R.string.script_failed, out), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun runAll() {
        if (scripts.isEmpty()) {
            Toast.makeText(context, R.string.script_none, Toast.LENGTH_SHORT).show()
            return
        }
        val view = _root ?: return
        val btn = view.findViewById<Button>(R.id.btn_run_all)
        btn.isEnabled = false

        EtApp.requestRoot { granted ->
            if (!granted) {
                ui {
                    Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_LONG).show()
                    btn.isEnabled = true
                }
                return@requestRoot
            }
            thread(name = "et-script-all") {
                var success = 0
                var failed = 0
                var output = ""
                for (s in scripts) {
                    val (ok, out) = Root.runner.run(s.content)
                    if (ok) success++ else { failed++; output += "${s.name}: $out\n" }
                }
                ui {
                    btn.isEnabled = true
                    Toast.makeText(context, getString(R.string.script_all_done, success, failed), Toast.LENGTH_LONG).show()
                    if (failed > 0) {
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle(R.string.script_errors_title)
                            .setMessage(output)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }

    data class ScriptItem(val name: String, val content: String)
}

class ScriptAdapter(
    private val items: List<ScriptsFragment.ScriptItem>,
    private val onRun: (ScriptsFragment.ScriptItem) -> Unit,
    private val onEdit: (ScriptsFragment.ScriptItem) -> Unit,
    private val onDelete: (ScriptsFragment.ScriptItem) -> Unit
) : RecyclerView.Adapter<ScriptAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.script_item_name)
        val content: TextView = view.findViewById(R.id.script_item_content)
        val btnRun: Button = view.findViewById(R.id.script_item_run)
        val btnEdit: Button = view.findViewById(R.id.script_item_edit)
        val btnDelete: Button = view.findViewById(R.id.script_item_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_script, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.content.text = item.content.take(80) + if (item.content.length > 80) "..." else ""
        holder.btnRun.setOnClickListener { onRun(item) }
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size
}