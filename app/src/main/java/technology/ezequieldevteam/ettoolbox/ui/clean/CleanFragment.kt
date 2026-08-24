package technology.ezequieldevteam.ettoolbox.ui.clean

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.data.BloatCatalog
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
import technology.ezequieldevteam.ettoolbox.core.repo.SysRepo

class CleanFragment : Fragment() {

    private var _root: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_clean, container, false)
        _root = v
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btn_clean_cache).setOnClickListener { cleanCache() }
        setupBloatList(view)
        loadStorage(view)
    }

    private fun loadStorage(view: View) {
        val storageView = view.findViewById<TextView>(R.id.storage_free)
        SysRepo.storageFree { line ->
            requireActivity().runOnUiThread {
                if (_root == null) return@runOnUiThread
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
            if (_root == null) return@requestRoot
            if (!granted) {
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_LONG).show()
                    btn.isEnabled = true
                    status.text = ""
                }
                return@requestRoot
            }
            Thread {
                Root.ok("pm trim-caches 999999999999")
                SysRepo.storageFree { freed ->
                    requireActivity().runOnUiThread {
                        if (_root == null) return@runOnUiThread
                        btn.isEnabled = true
                        status.text = getString(
                            R.string.clean_cache_done,
                            if (freed.isBlank()) "ok" else freed
                        )
                        loadStorage(requireView())
                    }
                }
            }.start()
        }
    }

    private fun setupBloatList(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.bloat_list)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        EtApp.requestRoot { granted ->
            if (_root == null) return@requestRoot
            Thread {
                val installedPkgs =
                    if (granted) Root.cmd("pm list packages") else ""
                val data = BloatCatalog.all.map {
                    it to installedPkgs.contains("package:${it.packageName}")
                }
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    recycler.adapter = BloatAdapter(data) { item, wasDisabled ->
                        Thread {
                            if (wasDisabled) {
                                Root.ok("pm enable ${item.packageName}")
                            } else {
                                Root.ok("pm disable-user --user 0 ${item.packageName}")
                            }
                            requireActivity().runOnUiThread {
                                (recycler.adapter as? BloatAdapter)?.notifyDataSetChanged()
                            }
                        }.start()
                    }
                }
            }.start()
        }
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
}
