package technology.ezequieldevteam.ettoolbox.ui.device

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.root.Su
import kotlin.concurrent.thread

class DeviceFragment : Fragment() {

    private var _root: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fillDeviceInfo(view)
        setupCpu(view)
        setupSpoof(view)
    }

    private fun fillDeviceInfo(view: View) {
        val mi = android.os.ActivityManager.MemoryInfo()
        requireContext().getSystemService(android.app.ActivityManager::class.java).getMemoryInfo(mi)

        view.findViewById<TextView>(R.id.device_info).text = buildString {
            appendLine("Aparelho: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Patch de seguranca: ${Build.VERSION.SECURITY_PATCH}")
            appendLine("SoC: ${Build.HARDWARE}")
            appendLine("Nucleos CPU: ${Runtime.getRuntime().availableProcessors()}")
            appendLine("RAM total: ${mi.totalMem / 1048576L} MB")
            appendLine("Kernel: ${System.getProperty("os.version")}")
            appendLine("Root: ${if (EtApp.rootAvailable) "sim" else "nao"}")
        }
    }

    private fun setupCpu(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.cpu_governor_spinner)
        val applyBtn = view.findViewById<Button>(R.id.cpu_apply)
        val cpuInfo = view.findViewById<TextView>(R.id.cpu_info)

        if (!EtApp.rootAvailable) {
            cpuInfo.text = "CPU: requer root"
            applyBtn.isEnabled = false
            return
        }

        thread {
            val curGov = Su.cmd("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor 2>/dev/null")
            val available = Su.lines("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors 2>/dev/null")
                .flatMap { it.split(" ") }.filter { it.isNotBlank() }
            val freqs = Su.cmd("cat /sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq 2>/dev/null")

            requireActivity().runOnUiThread {
                if (_root == null) return@runOnUiThread
                cpuInfo.text = buildString {
                    appendLine("Governador atual: $curGov")
                    val mhzList = freqs.lines().filter { it.isNotBlank() }
                        .mapNotNull { it.trim().toLongOrNull()?.div(1000) }
                    if (mhzList.isNotEmpty()) appendLine("Frequencias: ${mhzList.joinToString(" / ") { "$it MHz" }}")
                }
                if (available.isNotEmpty()) {
                    spinner.adapter = ArrayAdapter(
                        requireContext(), android.R.layout.simple_spinner_dropdown_item, available
                    )
                    val idx = available.indexOf(curGov)
                    if (idx >= 0) spinner.setSelection(idx)
                }
            }
        }

        applyBtn.setOnClickListener {
            val gov = spinner.selectedItem as? String ?: return@setOnClickListener
            thread {
                var anyOk = false
                for (i in 0 until Runtime.getRuntime().availableProcessors()) {
                    if (Su.ok("echo $gov > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor")) anyOk = true
                }
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(
                        context,
                        if (anyOk) R.string.device_cpu_applied else R.string.boost_no_root,
                        Toast.LENGTH_SHORT
                    ).show()
                    onViewCreated(requireView(), null)
                }
            }
        }
    }

    private fun setupSpoof(view: View) {
        val modelField = view.findViewById<EditText>(R.id.spoof_model)
        val brandField = view.findViewById<EditText>(R.id.spoof_brand)
        val applyBtn = view.findViewById<Button>(R.id.spoof_apply)
        val restoreBtn = view.findViewById<Button>(R.id.spoof_restore)

        modelField.hint = Build.MODEL
        brandField.hint = Build.MANUFACTURER

        if (!EtApp.rootAvailable) {
            applyBtn.isEnabled = false
            restoreBtn.isEnabled = false
            return
        }

        applyBtn.setOnClickListener {
            val model = modelField.text.toString().trim()
            val brand = brandField.text.toString().trim()
            if (model.isBlank() && brand.isBlank()) return@setOnClickListener

            thread {
                backupOriginals()
                if (model.isNotBlank()) Su.ok("resetprop ro.product.model '$model'")
                if (brand.isNotBlank()) Su.ok("resetprop ro.product.brand '$brand'")
                if (brand.isNotBlank()) Su.ok("resetprop ro.product.manufacturer '$brand'")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, R.string.device_spoof_applied, Toast.LENGTH_LONG).show()
                }
            }
        }

        restoreBtn.setOnClickListener {
            thread { restoreOriginals { } }
        }
    }

    private fun backupOriginals() {
        Su.ok("[ -f /data/local/tmp/ettoolbox_orig_model ] || resetprop ro.product.model > /data/local/tmp/ettoolbox_orig_model")
        Su.ok("[ -f /data/local/tmp/ettoolbox_orig_brand ] || resetprop ro.product.brand > /data/local/tmp/ettoolbox_orig_brand")
    }

    private fun restoreOriginals(done: () -> Unit) {
        val model = Su.cmd("cat /data/local/tmp/ettoolbox_orig_model 2>/dev/null").trim()
        val brand = Su.cmd("cat /data/local/tmp/ettoolbox_orig_brand 2>/dev/null").trim()
        if (model.isNotBlank()) Su.ok("resetprop ro.product.model '$model'")
        if (brand.isNotBlank()) {
            Su.ok("resetprop ro.product.brand '$brand'")
            Su.ok("resetprop ro.product.manufacturer '$brand'")
        }
        requireActivity().runOnUiThread {
            if (_root != null) {
                Toast.makeText(context, R.string.device_spoof_restored, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}
