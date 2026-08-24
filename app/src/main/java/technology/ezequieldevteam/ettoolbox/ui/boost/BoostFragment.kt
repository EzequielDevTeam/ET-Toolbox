package technology.ezequieldevteam.ettoolbox.ui.boost

import android.app.ActivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}
