package technology.ezequieldevteam.ettoolbox.ui.boost

import android.app.ActivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
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
import kotlin.concurrent.thread

class BoostFragment : Fragment() {

    private var _root: View? = null

    private fun ui(block: () -> Unit) {
        activity?.runOnUiThread {
            if (_root != null) block()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_boost, container, false)
        _root = v
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btn_game_mode).setOnClickListener { gameMode() }
        view.findViewById<Button>(R.id.btn_anim_off).setOnClickListener { setAnimations("0") }
        view.findViewById<Button>(R.id.btn_anim_fast).setOnClickListener { setAnimations("0.5") }
        view.findViewById<Button>(R.id.btn_anim_normal).setOnClickListener { setAnimations("1") }
        view.findViewById<Button>(R.id.btn_fstrim).setOnClickListener { fstrim() }

        setupGameProfiles(view)
    }

    override fun onResume() {
        super.onResume()
        _root?.let { updateRam(it) }
        loadExtras()
    }

    private fun memInfo(): Pair<Long, Long> {
        val mi = ActivityManager.MemoryInfo()
        requireContext().getSystemService(ActivityManager::class.java).getMemoryInfo(mi)
        return mi.availMem / 1048576L to mi.totalMem / 1048576L
    }

    private fun updateRam(view: View) {
        thread(name = "et-ram") {
            val (free, total) = memInfo()
            ui {
                view.findViewById<TextView>(R.id.ram_info).text =
                    getString(R.string.boost_ram, "$free MB", "$total MB")
            }
        }
    }

    private fun loadExtras() {
        val view = _root ?: return
        SysRepo.zramLine { line ->
            ui { view.findViewById<TextView>(R.id.boost_extra_info).text = line }
        }

        view.findViewById<TextView>(R.id.anim_current).text = getString(R.string.boost_anim_loading)
        SysRepo.animationsGet { scale ->
            ui {
                _root?.findViewById<TextView>(R.id.anim_current)?.text =
                    getString(
                        R.string.boost_anim_current,
                        when {
                            scale <= 0.0 -> getString(R.string.boost_anim_off)
                            scale <= 0.5 -> "0.5x"
                            else -> "1x"
                        }
                    )
            }
        }
    }

    private fun gameMode() {
        val view = _root ?: return
        val btn = view.findViewById<Button>(R.id.btn_game_mode)
        btn.isEnabled = false

        EtApp.requestRoot { granted ->
            if (!granted) {
                ui {
                    Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_LONG).show()
                    btn.isEnabled = true
                }
                return@requestRoot
            }
            thread(name = "et-game") {
                Root.cmd("am kill-all")
                Root.ok("pm trim-caches 999999999999")
                Root.cmd("sync; echo 3 > /proc/sys/vm/drop_caches")
                val freeAfter = memInfo().first
                ui {
                    btn.isEnabled = true
                    _root?.let { updateRam(it) }
                    Toast.makeText(
                        context,
                        getString(R.string.boost_done, freeAfter.toString()),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setAnimations(scale: String) {
        EtApp.requestRoot { granted ->
            if (!granted) {
                ui { Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_SHORT).show() }
                return@requestRoot
            }
            SysRepo.animationsSet(scale) { ok ->
                ui {
                    Toast.makeText(
                        context,
                        if (ok) R.string.boost_anim_done else R.string.modules_action_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    loadExtras()
                }
            }
        }
    }

    private fun fstrim() {
        EtApp.requestRoot { granted ->
            if (!granted) {
                ui { Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_SHORT).show() }
                return@requestRoot
            }
            SysRepo.fstrim { ok, out ->
                ui {
                    Toast.makeText(
                        context,
                        getString(
                            if (ok) R.string.boost_fstrim_done else R.string.modules_action_failed,
                            out.ifBlank { "" }
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ===== GAME PROFILES =====
    private fun setupGameProfiles(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.profile_spinner)
        val btnApply = view.findViewById<Button>(R.id.btn_profile_apply)
        val btnSave = view.findViewById<Button>(R.id.btn_profile_save)
        val btnDelete = view.findViewById<Button>(R.id.btn_profile_delete)

        loadProfiles(spinner)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val name = parent?.getItemAtPosition(position) as? String
                if (name != null && name != getString(R.string.profile_new)) {
                    loadProfile(name)
                    btnApply.isEnabled = true
                    btnDelete.isEnabled = true
                } else {
                    clearProfileFields()
                    btnApply.isEnabled = false
                    btnDelete.isEnabled = false
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnApply.setOnClickListener { applyProfile() }
        btnSave.setOnClickListener { saveProfile() }
        btnDelete.setOnClickListener { deleteProfile() }
    }

    private fun loadProfiles(spinner: Spinner) {
        val prefs = requireContext().getSharedPreferences("et_profiles", 0)
        val count = prefs.getInt("profile_count", 0)
        val names = mutableListOf<String>()
        names.add(getString(R.string.profile_new)) // "Novo perfil"
        for (i in 0 until count) {
            val name = prefs.getString("profile_name_$i", "") ?: ""
            if (name.isNotBlank()) names.add(name)
        }
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
    }

    private fun loadProfile(name: String) {
        val prefs = requireContext().getSharedPreferences("et_profiles", 0)
        val count = prefs.getInt("profile_count", 0)
        for (i in 0 until count) {
            if ((prefs.getString("profile_name_$i", "") ?: "") == name) {
                val governor = prefs.getString("profile_gov_$i", "performance") ?: "performance"
                val animScale = prefs.getString("profile_anim_$i", "0.5") ?: "0.5"
                val doRam = prefs.getBoolean("profile_ram_$i", true)
                val doCache = prefs.getBoolean("profile_cache_$i", true)
                val doFstrim = prefs.getBoolean("profile_fstrim_$i", false)

                _root?.findViewById<Spinner>(R.id.profile_gov_spinner)?.setSelection(
                    arrayOf("performance", "powersave", "schedutil", "ondemand", "conservative").indexOf(governor).coerceAtLeast(0)
                )
                _root?.findViewById<Spinner>(R.id.profile_anim_spinner)?.setSelection(
                    arrayOf("0", "0.5", "1").indexOf(animScale).coerceAtLeast(0)
                )
                _root?.findViewById<CheckBox>(R.id.profile_ram)?.isChecked = doRam
                _root?.findViewById<CheckBox>(R.id.profile_cache)?.isChecked = doCache
                _root?.findViewById<CheckBox>(R.id.profile_fstrim)?.isChecked = doFstrim
                break
            }
        }
    }

    private fun clearProfileFields() {
        _root?.findViewById<Spinner>(R.id.profile_gov_spinner)?.setSelection(0) // performance
        _root?.findViewById<Spinner>(R.id.profile_anim_spinner)?.setSelection(1) // 0.5x
        _root?.findViewById<CheckBox>(R.id.profile_ram)?.isChecked = true
        _root?.findViewById<CheckBox>(R.id.profile_cache)?.isChecked = true
        _root?.findViewById<CheckBox>(R.id.profile_fstrim)?.isChecked = false
    }

    private fun applyProfile() {
        val view = _root ?: return
        val gov = view.findViewById<Spinner>(R.id.profile_gov_spinner).selectedItem as? String ?: "performance"
        val anim = view.findViewById<Spinner>(R.id.profile_anim_spinner).selectedItem as? String ?: "0.5"
        val doRam = view.findViewById<CheckBox>(R.id.profile_ram).isChecked
        val doCache = view.findViewById<CheckBox>(R.id.profile_cache).isChecked
        val doFstrim = view.findViewById<CheckBox>(R.id.profile_fstrim).isChecked

        EtApp.requestRoot { granted ->
            if (!granted) {
                ui { Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_LONG).show() }
                return@requestRoot
            }
            thread(name = "et-profile-apply") {
                CpuRepo.applyGovernor(gov, Runtime.getRuntime().availableProcessors()) { _ -> }
                SysRepo.animationsSet(anim) { _ -> }
                if (doRam) Root.cmd("am kill-all")
                if (doCache) Root.ok("pm trim-caches 999999999999")
                if (doRam || doCache) Root.cmd("sync; echo 3 > /proc/sys/vm/drop_caches")
                if (doFstrim) SysRepo.fstrim { _, _ -> }

                val freeAfter = memInfo().first
                ui {
                    Toast.makeText(context, getString(R.string.profile_applied, freeAfter.toString()), Toast.LENGTH_LONG).show()
                    _root?.let { updateRam(it) }
                    loadExtras()
                }
            }
        }
    }

    private fun saveProfile() {
        val view = _root ?: return
        val nameField = view.findViewById<EditText>(R.id.profile_name)
        val name = nameField.text.toString().trim()
        if (name.isBlank()) {
            Toast.makeText(context, R.string.profile_name_empty, Toast.LENGTH_SHORT).show()
            return
        }

        val gov = view.findViewById<Spinner>(R.id.profile_gov_spinner).selectedItem as? String ?: "performance"
        val anim = view.findViewById<Spinner>(R.id.profile_anim_spinner).selectedItem as? String ?: "0.5"
        val doRam = view.findViewById<CheckBox>(R.id.profile_ram).isChecked
        val doCache = view.findViewById<CheckBox>(R.id.profile_cache).isChecked
        val doFstrim = view.findViewById<CheckBox>(R.id.profile_fstrim).isChecked

        val prefs = requireContext().getSharedPreferences("et_profiles", 0)
        val editor = prefs.edit()
        val count = prefs.getInt("profile_count", 0)

        // Check if updating existing
        var idx = -1
        for (i in 0 until count) {
            if (prefs.getString("profile_name_$i", "") == name) { idx = i; break }
        }
        if (idx == -1) idx = count

        editor.putString("profile_name_$idx", name)
        editor.putString("profile_gov_$idx", gov)
        editor.putString("profile_anim_$idx", anim)
        editor.putBoolean("profile_ram_$idx", doRam)
        editor.putBoolean("profile_cache_$idx", doCache)
        editor.putBoolean("profile_fstrim_$idx", doFstrim)
        if (idx == count) editor.putInt("profile_count", count + 1)
        editor.apply()

        Toast.makeText(context, R.string.profile_saved, Toast.LENGTH_SHORT).show()
        loadProfiles(view.findViewById(R.id.profile_spinner))
    }

    private fun deleteProfile() {
        val view = _root ?: return
        val spinner = view.findViewById<Spinner>(R.id.profile_spinner)
        val name = spinner.selectedItem as? String
        if (name == null || name == getString(R.string.profile_new)) return

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.profile_delete_confirm)
            .setMessage(getString(R.string.profile_delete_body, name))
            .setPositiveButton(R.string.delete) { _, _ ->
                val prefs = requireContext().getSharedPreferences("et_profiles", 0)
                val editor = prefs.edit()
                val count = prefs.getInt("profile_count", 0)
                val newList = mutableListOf<String>()
                for (i in 0 until count) {
                    val n = prefs.getString("profile_name_$i", "") ?: ""
                    if (n != name && n.isNotBlank()) newList.add(n)
                }
                editor.clear()
                editor.putInt("profile_count", newList.size)
                newList.forEachIndexed { i, n ->
                    editor.putString("profile_name_$i", n)
                    // Copy other fields
                    val oldIdx = (0 until count).find { (prefs.getString("profile_name_$it", "") ?: "") == n } ?: 0
                    editor.putString("profile_gov_$i", prefs.getString("profile_gov_$oldIdx", "performance"))
                    editor.putString("profile_anim_$i", prefs.getString("profile_anim_$oldIdx", "0.5"))
                    editor.putBoolean("profile_ram_$i", prefs.getBoolean("profile_ram_$oldIdx", true))
                    editor.putBoolean("profile_cache_$i", prefs.getBoolean("profile_cache_$oldIdx", true))
                    editor.putBoolean("profile_fstrim_$i", prefs.getBoolean("profile_fstrim_$oldIdx", false))
                }
                editor.apply()
                Toast.makeText(context, R.string.profile_deleted, Toast.LENGTH_SHORT).show()
                loadProfiles(spinner)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}