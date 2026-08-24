package technology.ezequieldevteam.ettoolbox.ui.device

import android.app.ActivityManager
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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
import technology.ezequieldevteam.ettoolbox.core.model.BatteryInfo
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
import technology.ezequieldevteam.ettoolbox.core.repo.CpuRepo
import technology.ezequieldevteam.ettoolbox.core.repo.SysRepo
import kotlin.concurrent.thread

class DeviceFragment : Fragment() {

    private var _root: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_device, container, false)
        _root = v
        return v
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

    private fun ui(block: () -> Unit) {
        activity?.runOnUiThread {
            if (_root != null) block()
        }
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

        thread(name = "et-cpu-read") {
            var info = CpuRepo.readFast()

            if (info.availableGovernors.isEmpty()) {
                val latch = java.util.concurrent.CountDownLatch(1)
                CpuRepo.read { r ->
                    info = r
                    latch.countDown()
                }
                latch.await(16, java.util.concurrent.TimeUnit.SECONDS)
            }

            ui {
                val governors = info.availableGovernors
                if (info.error != null && governors.isEmpty()) {
                    cpuInfo.text = getString(
                        R.string.device_cpu_error,
                        info.error ?: getString(R.string.device_cpu_no_root)
                    )
                    applyBtn.isEnabled = false
                    return@ui
                }
                cpuInfo.text = buildString {
                    if (info.currentGovernor.isNotBlank()) {
                        appendLine(getString(R.string.device_cpu_current, info.currentGovernor))
                    }
                    if (info.freqsMhz.isNotEmpty()) {
                        appendLine(
                            getString(
                                R.string.device_cpu_freqs,
                                info.freqsMhz.joinToString(" / ")
                            )
                        )
                    }
                    if (info.minMhz != null && info.maxMhz != null) {
                        append(getString(R.string.device_cpu_minmax, info.minMhz!!, info.maxMhz!!))
                    }
                }
                spinner.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    governors
                )
                val idx = governors.indexOf(info.currentGovernor)
                if (idx >= 0) spinner.setSelection(idx)
                applyBtn.isEnabled = true
            }
        }

        applyBtn.setOnClickListener {
            val gov = spinner.selectedItem as? String ?: return@setOnClickListener
            applyBtn.isEnabled = false
            CpuRepo.applyGovernor(gov, Runtime.getRuntime().availableProcessors()) { ok ->
                ui {
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
        thread(name = "et-battery") {
            val b = readBatteryNoRoot()
            ui {
                info.text =
                    if (b == null) getString(R.string.device_battery_unavailable)
                    else getString(
                        R.string.device_battery_line,
                        b.level, b.charging, b.health, b.tempC, b.voltageMV
                    )
            }
        }
    }

    private fun readBatteryNoRoot(): BatteryInfo? {
        return try {
            val intent = requireContext().registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                ?: return null
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            if (level < 0) return null
            val pct = level * 100 / scale.coerceAtLeast(1)
            BatteryInfo(
                level = pct,
                tempC = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0,
                health = healthName(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)),
                voltageMV = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000,
                charging = chargeName(intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0))
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun healthName(code: Int) = when (code) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Boa"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Superaquecida"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Morta"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Sobretensão"
        BatteryManager.BATTERY_HEALTH_COLD -> "Fria"
        else -> "Desconhecida"
    }

    private fun chargeName(code: Int) = when (code) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Carregando"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descarregando"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Não carregando"
        BatteryManager.BATTERY_STATUS_FULL -> "Cheia"
        else -> "Desconhecido"
    }

    private fun setupDensity(view: View) {
        val current = view.findViewById<TextView>(R.id.density_current)
        val valueField = view.findViewById<EditText>(R.id.density_value)
        val applyBtn = view.findViewById<Button>(R.id.density_apply)
        val resetBtn = view.findViewById<Button>(R.id.density_reset)

        val dpi = resources.displayMetrics.densityDpi
        current.text = getString(R.string.device_density_current, dpi)

        applyBtn.setOnClickListener {
            val v = valueField.text.toString().trim().toIntOrNull()
            if (v == null || v < 200 || v > 1000) {
                Toast.makeText(context, R.string.device_density_invalid, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            applyBtn.isEnabled = false
            SysRepo.densitySet(v) { ok ->
                ui {
                    applyBtn.isEnabled = true
                    Toast.makeText(
                        context,
                        if (ok) R.string.device_density_done else R.string.modules_action_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        resetBtn.setOnClickListener {
            resetBtn.isEnabled = false
            SysRepo.densityReset { ok ->
                ui {
                    resetBtn.isEnabled = true
                    Toast.makeText(
                        context,
                        if (ok) R.string.device_density_reset_done else R.string.modules_action_failed,
                        Toast.LENGTH_SHORT
                    ).show()
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
            ui {
                applyBtn.isEnabled = granted
                restoreBtn.isEnabled = granted
            }
        }

        applyBtn.setOnClickListener {
            val model = modelField.text.toString().trim()
            val brand = brandField.text.toString().trim()
            if (model.isBlank() && brand.isBlank()) return@setOnClickListener

            thread {
                backupOriginals()
                if (model.isNotBlank()) Root.ok("setprop ro.product.model \"$model\"")
                if (brand.isNotBlank()) {
                    Root.ok("setprop ro.product.brand \"$brand\"")
                    Root.ok("setprop ro.product.manufacturer \"$brand\"")
                }
                ui { Toast.makeText(context, R.string.device_spoof_applied, Toast.LENGTH_LONG).show() }
            }
        }

        restoreBtn.setOnClickListener {
            thread {
                val model = Root.cmd("cat /data/local/tmp/ettoolbox_orig_model 2>/dev/null").trim()
                val brand = Root.cmd("cat /data/local/tmp/ettoolbox_orig_brand 2>/dev/null").trim()
                if (model.isNotBlank()) Root.ok("setprop ro.product.model \"$model\"")
                if (brand.isNotBlank()) {
                    Root.ok("setprop ro.product.brand \"$brand\"")
                    Root.ok("setprop ro.product.manufacturer \"$brand\"")
                }
                ui { Toast.makeText(context, R.string.device_spoof_restored, Toast.LENGTH_LONG).show() }
            }
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
