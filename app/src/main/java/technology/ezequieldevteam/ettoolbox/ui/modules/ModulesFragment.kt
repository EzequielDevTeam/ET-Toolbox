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
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.root.Su
import kotlin.concurrent.thread

class ModulesFragment : Fragment() {

    private var _root: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_modules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadModules(view)
    }

    private fun parseProp(content: List<String>, key: String): String =
        content.firstOrNull { it.startsWith("$key=") }?.substringAfter('=') ?: ""

    private fun loadModules(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.modules_list)
        val status = view.findViewById<TextView>(R.id.modules_status)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        if (!EtApp.rootAvailable) {
            status.text = getString(R.string.modules_loading)
            EtApp.requestRoot { granted ->
                if (_root == null) return@requestRoot
                if (granted) loadModules(requireView())
                else status.text = getString(R.string.modules_none)
            }
            return
        }

        thread {
            val dirs = Su.lines("ls /data/adb/modules 2>/dev/null")
            val modules = mutableListOf<ModuleAdapter.MagiskModule>()

            for (dir in dirs) {
                if (dir.isBlank() || dir.startsWith("ls:") || dir.contains("No such")) continue
                val prop = Su.lines("cat /data/adb/modules/$dir/module.prop 2>/dev/null")
                modules.add(
                    ModuleAdapter.MagiskModule(
                        dirName = dir,
                        name = parseProp(prop, "name"),
                        version = parseProp(prop, "version"),
                        enabled = !Su.cmd("test -f /data/adb/modules/$dir/disable && echo yes").contains("yes"),
                        markedRemove = Su.cmd("test -f /data/adb/modules/$dir/remove && echo yes").contains("yes")
                    )
                )
            }

            requireActivity().runOnUiThread {
                if (_root == null) return@runOnUiThread
                status.text = ""
                recycler.adapter = ModuleAdapter(
                    modules,
                    onToggle = { m ->
                        thread {
                            if (m.enabled) Su.ok("touch /data/adb/modules/${m.dirName}/disable")
                            else Su.ok("rm -f /data/adb/modules/${m.dirName}/disable")
                            requireActivity().runOnUiThread {
                                Toast.makeText(context, R.string.modules_toggled, Toast.LENGTH_SHORT).show()
                                loadModules(requireView())
                            }
                        }
                    },
                    onRemove = { m ->
                        thread {
                            Su.ok("touch /data/adb/modules/${m.dirName}/remove")
                            requireActivity().runOnUiThread {
                                Toast.makeText(context, R.string.modules_removed, Toast.LENGTH_SHORT).show()
                                loadModules(requireView())
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadModules(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}
