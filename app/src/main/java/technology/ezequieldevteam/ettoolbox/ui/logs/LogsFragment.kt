package technology.ezequieldevteam.ettoolbox.ui.logs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

class LogsFragment : Fragment() {

    private var _root: View? = null
    private var currentFilter = "main"
    private val filters = arrayOf("main", "system", "kernel", "radio", "events", "crash")

    private fun ui(block: () -> Unit) {
        activity?.runOnUiThread {
            if (_root != null) block()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_logs, container, false)
        _root = v
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val spinner = view.findViewById<Spinner>(R.id.log_filter_spinner)
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filters)
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = filters[position]
                loadLogs()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        view.findViewById<Button>(R.id.btn_refresh_logs).setOnClickListener { loadLogs() }
        view.findViewById<Button>(R.id.btn_clear_logs).setOnClickListener { clearLogs() }
        view.findViewById<Button>(R.id.btn_save_logs).setOnClickListener { saveLogs() }

        loadLogs()
    }

    private fun loadLogs() {
        val view = _root ?: return
        val logView = view.findViewById<TextView>(R.id.logs_text)
        val btn = view.findViewById<Button>(R.id.btn_refresh_logs)
        btn.isEnabled = false
        logView.text = getString(R.string.logs_loading)

        thread(name = "et-logs") {
            val output = StringBuilder()
            try {
                val process = Runtime.getRuntime().exec("logcat -d -b $currentFilter -v brief *:V")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                var count = 0
                while (reader.readLine().also { line = it } != null && count < 500) {
                    output.append(line).append("\n")
                    count++
                }
                reader.close()
                process.waitFor()
            } catch (e: Exception) {
                output.append("Erro: ${e.message}")
            }
            ui {
                btn.isEnabled = true
                logView.text = if (output.isNotEmpty()) output.toString() else getString(R.string.logs_empty)
            }
        }
    }

    private fun clearLogs() {
        EtApp.requestRoot { granted ->
            if (!granted) {
                Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_SHORT).show()
                return@requestRoot
            }
            Root.submit("logcat -c") { ok, _ ->
                ui {
                    Toast.makeText(context, if (ok) R.string.logs_cleared else R.string.logs_clear_failed, Toast.LENGTH_SHORT).show()
                    loadLogs()
                }
            }
        }
    }

    private fun saveLogs() {
        val view = _root ?: return
        val logView = view.findViewById<TextView>(R.id.logs_text)
        val text = logView.text.toString()
        if (text.isBlank() || text == getString(R.string.logs_empty) || text == getString(R.string.logs_loading)) {
            Toast.makeText(context, R.string.logs_nothing_to_save, Toast.LENGTH_SHORT).show()
            return
        }
        val fileName = "et_logs_${System.currentTimeMillis()}.txt"
        try {
            val file = requireContext().getExternalFilesDir(null)?.let { java.io.File(it, fileName) }
            file?.writeText(text)
            Toast.makeText(context, getString(R.string.logs_saved, file?.absolutePath), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.logs_save_failed, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}