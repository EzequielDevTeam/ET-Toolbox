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
import technology.ezequieldevteam.ettoolbox.data.BloatCatalog
import technology.ezequieldevteam.ettoolbox.root.Su
import kotlin.concurrent.thread

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
    }

    private fun cleanCache() {
        if (!EtApp.rootAvailable) {
            Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_LONG).show()
            return
        }
        val btn = requireView().findViewById<Button>(R.id.btn_clean_cache)
        val status = requireView().findViewById<TextView>(R.id.clean_status)
        btn.isEnabled = false
        status.text = "Limpando..."
        thread {
            Su.ok("pm trim-caches 999999999999")
            val freed = Su.cmd("df -h /data | tail -1 | awk '{print $4}'")
            requireActivity().runOnUiThread {
                if (_root == null) return@runOnUiThread
                btn.isEnabled = true
                status.text = getString(R.string.clean_cache_done, if (freed.isBlank()) "ok" else freed)
            }
        }
    }

    private fun setupBloatList(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.bloat_list)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        thread {
            val installedPkgs = if (EtApp.rootAvailable) Su.cmd("pm list packages") else ""
            val data = BloatCatalog.all.map {
                it to installedPkgs.contains("package:${it.packageName}")
            }
            requireActivity().runOnUiThread {
                if (_root == null) return@runOnUiThread
                recycler.adapter = BloatAdapter(data) { item, wasDisabled ->
                    thread {
                        if (wasDisabled) {
                            Su.ok("pm enable ${item.packageName}")
                        } else {
                            Su.ok("pm disable-user --user 0 ${item.packageName}")
                        }
                        requireActivity().runOnUiThread {
                            (recycler.adapter as? BloatAdapter)?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}
