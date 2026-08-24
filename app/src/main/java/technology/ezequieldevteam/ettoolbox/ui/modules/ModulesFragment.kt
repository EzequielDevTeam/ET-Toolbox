package technology.ezequieldevteam.ettoolbox.ui.modules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.model.MagiskRow
import technology.ezequieldevteam.ettoolbox.core.repo.MagiskRepo

class ModulesFragment : Fragment() {

    private var _root: View? = null
    private var allModules: List<MagiskRow> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_modules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.modules_list)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = ModuleAdapter(
            emptyList(),
            onToggle = { m -> toggle(m) },
            onRemove = { m -> remove(m) }
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
            if (_root == null) return@list
            requireActivity().runOnUiThread {
                val v = _root ?: return@runOnUiThread
                if (error != null) {
                    status.text = ""
                    val err = v.findViewById<TextView>(R.id.modules_error)
                    err.visibility = View.VISIBLE
                    err.text = getString(R.string.modules_error_prefix, error)
                    allModules = emptyList()
                    applyFilter()
                    return@runOnUiThread
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
        (recycler.adapter as? ModuleAdapter)?.replace(filtered)
    }

    private fun toggle(m: MagiskRow) {
        MagiskRepo.toggle(m.id, m.enabled) { ok ->
            requireActivity().runOnUiThread {
                if (_root == null) return@runOnUiThread
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
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.modules_remove_confirm_title)
            .setMessage(getString(R.string.modules_remove_confirm_body, m.name))
            .setPositiveButton(R.string.modules_remove) { _, _ ->
                MagiskRepo.markRemove(m.id) { ok ->
                    requireActivity().runOnUiThread {
                        if (_root == null) return@runOnUiThread
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
}
