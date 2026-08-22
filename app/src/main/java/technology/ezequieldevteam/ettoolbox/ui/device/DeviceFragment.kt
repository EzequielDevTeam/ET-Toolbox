package technology.ezequieldevteam.ettoolbox.ui.device

import android.app.ActivityManager
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

    override fun onResume() {
        super.onResume()
        view?.let { setupCpu(it) }
    }

    private fun fillDeviceInfo(view: View) {
        val mi = ActivityManager.MemoryInfo()
        requireContext().getSystemService(ActivityManager::class.java).getMemoryInfo(mi)

        view.findViewById<TextView>(R.id.device_info).text = buildString {
            appendLine(getString(R.string.device_line_name, "${Build.MANUFACTURER} ${Build.MODEL}"))
            appendLine(getString(R.string.device_line_android, "${Build.VERSION.RELEASE}", "${Build.VERSION.SDK_INT}"))
            appendLine(getString(R.string.device_line_patch, Build.VERSION.SECURITY_PATCH))
            appendLine(getString(R.string.device_line_soc, Build.HARDWARE))
            appendLine(getString(R.string.device_line_cores, Runtime.getRuntime().availableProcessors()))
            appendLine(getString(R.string.device_line_ram, mi.totalMem / 1048576L))
            appendLine(getString(R.string.device_line_kernel, System.getProperty("os.version")))
            appendLine(getString(R.string.device_line_root, if (EtApp.rootAvailable) "sim" else "aguardando..."))
        }
    }

    private fun setupCpu(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.cpu_governor_spinner)
        val applyBtn = view.findViewById<Button>(R.id.cpu_apply)
        val cpuInfo = view.findViewById<TextView>(R.id.cpu_info)

        cpuInfo.text = getString(R.string.device_cpu_loading)
        applyBtn.isEnabled = false

        EtApp.requestRoot { granted ->
            if (_root == null) return@requestRoot
            if (!granted) {
                cpuInfo.text = getString(R.string.device_cpu_no_root)
                return@requestRoot
            }
            thread {
                val curGov = Su.read("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
                val available = Su.cmd("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors 2>/dev/null")
                    .split(" ").filter { it.isNotBlank() }
                val freqs = Su.cmd("cat /sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq 2>/dev/null")

                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    if (available.isEmpty()) {
                        cpuInfo.text = getString(R.string.device_cpu_no_root)
                        return@runOnUiThread
                    }
                    cpuInfo.text = buildString {
                        appendLine(getString(R.string.device_cpu_current, curGov))
                        val mhzList = freqs.lines().filter { it.isNotBlank() }
                            .mapNotNull { it.trim().toLongOrNull()?.div(1000) }
                        if (mhzList.isNotEmpty()) {
                            append(getString(R.string.device_cpu_freqs, mhzList.joinToString(" / ") { "$it MHz" }))
                        }
                    }
                    spinner.adapter = ArrayAdapter(
                        requireContext(), android.R.layout.simple_spinner_dropdown_item, available
                    )
                    val idx = available.indexOf(curGov)
                    if (idx >= 0) spinner.setSelection(idx)
                    applyBtn.isEnabled = true
                }
            }
        }

        applyBtn.setOnClickListener {
            val gov = spinner.selectedItem as? String ?: return@setOnClickListener
            thread {
                var anyOk = false
                for (i in 0 until Runtime.getRuntime().availableProcessors()) {
                    if (Su.writeFileSysfs("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor", gov)) anyOk = true
                }
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(
                        context,
                        if (anyOk) R.string.device_cpu_applied else R.string.boost_no_root,
                        Toast.LENGTH_SHORT
                    ).show()
                    setupCpu(requireView())
                }
            }
        }
    }

    private fun setupSpoof(view: View) {
        val modelField = view.findViewById<EditText>(R.id.spoof_model)
        val brandField = view.findViewById<EditText>(R.id.spoof_brand)
        val applyBtn = view.findViewById<Button>(R.id.spoof_apply)
        val restoreBtn = view.findViewById<Button>(R.id.spoof_restore)

        if (modelField.hint.isNullOrBlank()) modelField.hint = Build.MODEL
        if (brandField.hint.isNullOrBlank()) brandField.hint = Build.MANUFACTURER

        EtApp.requestRoot { granted ->
            if (_root == null) return@requestRoot
            applyBtn.isEnabled = granted
            restoreBtn.isEnabled = granted
        }

        applyBtn.setOnClickListener {
            val model = modelField.text.toString().trim()
            val brand = brandField.text.toString().trim()
            if (model.isBlank() && brand.isBlank()) return@setOnClickListener

            thread {
                backupOriginals()
                if (model.isNotBlank()) Su.setProp("ro.product.model", model)
                if (brand.isNotBlank()) {
                    Su.setProp("ro.product.brand", brand)
                    Su.setProp("ro.product.manufacturer", brand)
                }
                toastMain(R.string.device_spoof_applied)
            }
        }

        restoreBtn.setOnClickListener {
            thread {
                val model = Su.read("/data/local/tmp/ettoolbox_orig_model").trim()
                val brand = Su.read("/data/local/tmp/ettoolbox_orig_brand").trim()
                if (model.isNotBlank()) Su.setProp("ro.product.model", model)
                if (brand.isNotBlank()) {
                    Su.setProp("ro.product.brand", brand)
                    Su.setProp("ro.product.manufacturer", brand)
                }
                toastMain(R.string.device_spoof_restored)
            }
        }
    }

    private fun backupOriginals() {
        val origModel = Su.prop("ro.product.model")
        val origBrand = Su.prop("ro.product.brand")
        if (!Su.exists("/data/local/tmp/ettoolbox_orig_model") && origModel.isNotBlank()) {
            Su.write("/data/local/tmp/ettoolbox_orig_model", origModel)
        }
        if (!Su.exists("/data/local/tmp/ettoolbox_orig_brand") && origBrand.isNotBlank()) {
            Su.write("/data/local/tmp/ettoolbox_orig_brand", origBrand)
        }
    }

    private fun toastMain(resId: Int) {
        requireActivity().runOnUiThread {
            if (_root != null) Toast.makeText(context, resId, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}
