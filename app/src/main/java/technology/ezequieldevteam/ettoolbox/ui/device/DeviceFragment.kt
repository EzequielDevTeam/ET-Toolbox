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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
import technology.ezequieldevteam.ettoolbox.core.repo.CpuRepo
import technology.ezequieldevteam.ettoolbox.core.repo.SysRepo

class DeviceFragment : Fragment() {

    private var _root: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fillDeviceInfo(view)
        setupCpu(view)
        setupBattery(view)
        setupDensity(view)
        setupReboot(view)
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
            append(getString(R.string.device_line_root, if (EtApp.rootAvailable) "sim" else "aguardando..."))
        }
    }

    private fun setupCpu(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.cpu_governor_spinner)
        val applyBtn = view.findViewById<Button>(R.id.cpu_apply)
        val cpuInfo = view.findViewById<TextView>(R.id.cpu_info)

        cpuInfo.text = getString(R.string.device_cpu_loading)

        val start = {
            CpuRepo.read { info ->
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    if (info.error != null || info.availableGovernors.isEmpty()) {
                        cpuInfo.text = getString(
                            R.string.device_cpu_error,
                            info.error ?: getString(R.string.device_cpu_no_root)
                        )
                        applyBtn.isEnabled = false
                        return@runOnUiThread
                    }
                    cpuInfo.text = buildString {
                        appendLine(getString(R.string.device_cpu_current, info.currentGovernor))
                        if (info.freqsMhz.isNotEmpty()) {
                            appendLine(getString(R.string.device_cpu_freqs,
                                info.freqsMhz.joinToString(" / ") { "$it" }))
                        }
                        if (info.minMhz != null && info.maxMhz != null) {
                            append(getString(R.string.device_cpu_minmax, info.minMhz, info.maxMhz))
                        }
                    }
                    spinner.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        info.availableGovernors
                    )
                    val idx = info.availableGovernors.indexOf(info.currentGovernor)
                    if (idx >= 0) spinner.setSelection(idx)
                    applyBtn.isEnabled = true
                }
            }
        }

        if (!EtApp.rootAvailable) {
            EtApp.requestRoot { granted ->
                if (_root == null) return@requestRoot
                if (!granted) {
                    requireActivity().runOnUiThread {
                        if (_root == null) return@runOnUiThread
                        cpuInfo.text = getString(R.string.device_cpu_no_root)
                        applyBtn.isEnabled = false
                    }
                } else {
                    start()
                }
            }
        } else {
            start()
        }

        applyBtn.setOnClickListener {
            val gov = spinner.selectedItem as? String ?: return@setOnClickListener
            applyBtn.isEnabled = false
            CpuRepo.applyGovernor(gov, Runtime.getRuntime().availableProcessors()) { ok ->
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(
                        context,
                        if (ok) R.string.device_cpu_applied else R.string.modules_action_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    setupCpu(requireView())
                }
            }
        }
    }

    private fun setupBattery(view: View) {
        val info = view.findViewById<TextView>(R.id.battery_info)
        info.text = getString(R.string.device_battery_loading)
        SysRepo.battery { b ->
            requireActivity().runOnUiThread {
                if (_root == null) return@runOnUiThread
                info.text =
                    if (b == null) getString(R.string.device_cpu_error, "bateria indisponível")
                    else getString(
                        R.string.device_battery_line,
                        b.level, b.charging, b.health, b.tempC, b.voltageMV
                    )
            }
        }
    }

    private fun setupDensity(view: View) {
        val current = view.findViewById<TextView>(R.id.density_current)
        val valueField = view.findViewById<EditText>(R.id.density_value)
        val applyBtn = view.findViewById<Button>(R.id.density_apply)
        val resetBtn = view.findViewById<Button>(R.id.density_reset)

        current.text = getString(R.string.device_density_loading)
        applyBtn.isEnabled = false
        resetBtn.isEnabled = false

        val load = {
            SysRepo.densityCurrent { value, error ->
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    if (value != null) {
                        current.text = getString(R.string.device_density_current, value)
                        applyBtn.isEnabled = true
                        resetBtn.isEnabled = true
                    } else {
                        current.text = getString(R.string.device_cpu_error, error ?: "?")
                    }
                }
            }
        }

        if (!EtApp.rootAvailable) {
            EtApp.requestRoot { granted ->
                if (granted && _root != null) load()
            }
        } else load()

        applyBtn.setOnClickListener {
            val v = valueField.text.toString().trim().toIntOrNull()
            if (v == null || v < 200 || v > 1000) {
                Toast.makeText(context, R.string.device_density_invalid, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            SysRepo.densitySet(v) { ok ->
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(
                        context,
                        if (ok) R.string.device_density_done else R.string.modules_action_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    load()
                }
            }
        }

        resetBtn.setOnClickListener {
            SysRepo.densityReset { ok ->
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(
                        context,
                        if (ok) R.string.device_density_reset_done else R.string.modules_action_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    load()
                }
            }
        }
    }

    private fun setupReboot(view: View) {
        val normal = view.findViewById<Button>(R.id.btn_reboot_normal)
        val recovery = view.findViewById<Button>(R.id.btn_reboot_recovery)
        val bootloader = view.findViewById<Button>(R.id.btn_reboot_bootloader)

        val confirm: (String, Int) -> Unit = { mode, titleRes ->
            AlertDialog.Builder(requireContext())
                .setTitle(titleRes)
                .setMessage(R.string.device_reboot_confirm_body)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    SysRepo.reboot(mode) { }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        normal.setOnClickListener { confirm("normal", R.string.device_reboot_normal_title) }
        recovery.setOnClickListener { confirm("recovery", R.string.device_reboot_recovery_title) }
        bootloader.setOnClickListener { confirm("bootloader", R.string.device_reboot_bootloader_title) }
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
            requireActivity().runOnUiThread {
                if (_root == null) return@runOnUiThread
                applyBtn.isEnabled = granted
                restoreBtn.isEnabled = granted
            }
        }

        applyBtn.setOnClickListener {
            val model = modelField.text.toString().trim()
            val brand = brandField.text.toString().trim()
            if (model.isBlank() && brand.isBlank()) return@setOnClickListener

            Thread {
                backupOriginals()
                if (model.isNotBlank()) Root.ok("setprop ro.product.model \"$model\"")
                if (brand.isNotBlank()) {
                    Root.ok("setprop ro.product.brand \"$brand\"")
                    Root.ok("setprop ro.product.manufacturer \"$brand\"")
                }
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(context, R.string.device_spoof_applied, Toast.LENGTH_LONG).show()
                }
            }.start()
        }

        restoreBtn.setOnClickListener {
            Thread {
                val model = Root.cmd("cat /data/local/tmp/ettoolbox_orig_model 2>/dev/null").trim()
                val brand = Root.cmd("cat /data/local/tmp/ettoolbox_orig_brand 2>/dev/null").trim()
                if (model.isNotBlank()) Root.ok("setprop ro.product.model \"$model\"")
                if (brand.isNotBlank()) {
                    Root.ok("setprop ro.product.brand \"$brand\"")
                    Root.ok("setprop ro.product.manufacturer \"$brand\"")
                }
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(context, R.string.device_spoof_restored, Toast.LENGTH_LONG).show()
                }
            }.start()
        }
    }

    private fun backupOriginals() {
        val origModel = Root.cmd("getprop ro.product.model").trim()
        val origBrand = Root.cmd("getprop ro.product.brand").trim()
        if (!Root.cmd("test -e /data/local/tmp/ettoolbox_orig_model && echo y").contains("y")
            && origModel.isNotBlank()
        ) {
            Root.ok("echo '$origModel' > /data/local/tmp/ettoolbox_orig_model")
        }
        if (!Root.cmd("test -e /data/local/tmp/ettoolbox_orig_brand && echo y").contains("y")
            && origBrand.isNotBlank()
        ) {
            Root.ok("echo '$origBrand' > /data/local/tmp/ettoolbox_orig_brand")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}
