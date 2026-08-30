package technology.ezequieldevteam.ettoolbox.ui.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import kotlin.concurrent.thread

class NetworkFragment : Fragment() {

    private var _root: View? = null

    private fun ui(block: () -> Unit) {
        activity?.runOnUiThread {
            if (_root != null) block()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_network, container, false)
        _root = v
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadNetworkInfo()
        view.findViewById<Button>(R.id.btn_ping).setOnClickListener { ping() }
        view.findViewById<Button>(R.id.btn_dns).setOnClickListener { changeDns() }
        view.findViewById<Button>(R.id.btn_traceroute).setOnClickListener { traceroute() }
        view.findViewById<Button>(R.id.btn_refresh_net).setOnClickListener { loadNetworkInfo() }
    }

    private fun loadNetworkInfo() {
        val view = _root ?: return
        val infoView = view.findViewById<TextView>(R.id.network_info)
        infoView.text = getString(R.string.network_loading)

        thread(name = "et-net-info") {
            val output = StringBuilder()
            try {
                val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = cm.activeNetwork
                val caps = network?.let { cm.getNetworkCapabilities(it) }

                output.appendLine(getString(R.string.network_active, if (network != null) "Sim" else "Não"))
                output.appendLine(getString(R.string.network_type, caps?.let { getTransportName(it) } ?: "Desconhecido"))

                val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (iface in interfaces) {
                    val addresses = Collections.list(iface.inetAddresses)
                    for (addr in addresses) {
                        if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                            output.appendLine(getString(R.string.network_interface, iface.displayName, addr.hostAddress))
                        }
                    }
                }

                val dns = getDnsServers()
                if (dns.isNotEmpty()) output.appendLine(getString(R.string.network_dns, dns.joinToString(", ")))

            } catch (e: Exception) {
                output.append("Erro: ${e.message}")
            }
            ui { infoView.text = output.toString() }
        }
    }

    private fun getTransportName(caps: NetworkCapabilities): String {
        return if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) "Wi-Fi"
        else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) "Móvel"
        else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) "Ethernet"
        else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) "VPN"
        else "Outro"
    }

    private fun getDnsServers(): List<String> {
        val dns = mutableListOf<String>()
        try {
            val file = java.io.File("/etc/resolv.conf")
            if (file.exists()) {
                file.readLines().forEach { line ->
                    if (line.trim().startsWith("nameserver")) {
                        val parts = line.trim().split("\\s+")
                        if (parts.size > 1) dns.add(parts[1])
                    }
                }
            }
        } catch (_: Exception) {}
        return dns
    }

    private fun ping() {
        val view = _root ?: return
        val hostField = view.findViewById<EditText>(R.id.ping_host)
        val host = hostField.text.toString().trim().ifBlank { "8.8.8.8" }
        val resultView = view.findViewById<TextView>(R.id.network_result)
        val btn = view.findViewById<Button>(R.id.btn_ping)
        btn.isEnabled = false
        resultView.text = getString(R.string.network_pinging, host)

        thread(name = "et-ping") {
            try {
                val process = Runtime.getRuntime().exec("ping -c 4 -W 2 $host")
                val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                val output = reader.readText()
                reader.close()
                process.waitFor()
                val exitCode = process.exitValue()
                ui {
                    btn.isEnabled = true
                    resultView.text = if (exitCode == 0) getString(R.string.network_ping_ok, output) else getString(R.string.network_ping_fail, output)
                }
            } catch (e: Exception) {
                ui { btn.isEnabled = true; resultView.text = getString(R.string.network_error, e.message) }
            }
        }
    }

    private fun traceroute() {
        val view = _root ?: return
        val hostField = view.findViewById<EditText>(R.id.ping_host)
        val host = hostField.text.toString().trim().ifBlank { "8.8.8.8" }
        val resultView = view.findViewById<TextView>(R.id.network_result)
        val btn = view.findViewById<Button>(R.id.btn_traceroute)
        btn.isEnabled = false
        resultView.text = getString(R.string.network_tracing, host)

        thread(name = "et-trace") {
            try {
                val process = Runtime.getRuntime().exec("traceroute -m 15 -w 2 $host")
                val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                val output = reader.readText()
                reader.close()
                process.waitFor()
                ui { btn.isEnabled = true; resultView.text = if (output.isNotBlank()) output else getString(R.string.network_trace_fail) }
            } catch (e: Exception) {
                ui { btn.isEnabled = true; resultView.text = getString(R.string.network_error, e.message) }
            }
        }
    }

    private fun changeDns() {
        val view = _root ?: return
        val dnsField = view.findViewById<EditText>(R.id.dns_servers)
        val dns = dnsField.text.toString().trim().ifBlank { "1.1.1.1 8.8.8.8" }
        val btn = view.findViewById<Button>(R.id.btn_dns)

        EtApp.requestRoot { granted ->
            if (!granted) {
                Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_SHORT).show()
                return@requestRoot
            }
            btn.isEnabled = false
            thread {
                val cmds = dns.split(" ").map { "setprop net.dns${it.index + 1} ${it.value}" }
                val script = cmds.joinToString("; ")
                val (ok, out) = Root.runner.run(script)
                ui {
                    btn.isEnabled = true
                    Toast.makeText(context, if (ok) getString(R.string.network_dns_ok) else getString(R.string.network_dns_fail, out), Toast.LENGTH_LONG).show()
                    loadNetworkInfo()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}