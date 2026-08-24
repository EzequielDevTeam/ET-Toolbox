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

class BoostFragment : Fragment() {

    private var _root: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_boost, container, false)
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
        val v = _root ?: return
        updateRam(v)
        loadExtras(v)
    }

    private fun memInfo(): Pair<Long, Long> {
        val mi = ActivityManager.MemoryInfo()
        requireContext().getSystemService(ActivityManager::class.java).getMemoryInfo(mi)
        return mi.availMem / 1048576L to mi.totalMem / 1048576L
    }

    private fun updateRam(view: View) {
        Thread {
            val (free, total) = memInfo()
            requireActivity().runOnUiThread {
                if (_root == null) return@runOnUiThread
                view.findViewById<TextView>(R.id.ram_info).text =
                    getString(R.string.boost_ram, "$free MB", "$total MB")
            }
        }.start()
    }

    private fun loadExtras(view: View) {
        val extra = view.findViewById<TextView>(R.id.boost_extra_info)
        SysRepo.zramLine { line ->
            requireActivity().runOnUiThread {
                if (_root == null) return@runOnUiThread
                extra.text = line
            }
        }

        val animCurrent = view.findViewById<TextView>(R.id.anim_current)
        animCurrent.text = getString(R.string.boost_anim_loading)

        val refresh = {
            SysRepo.animationsGet { scale ->
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    animCurrent.text =
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

        if (!EtApp.rootAvailable) {
            EtApp.requestRoot { granted ->
                if (granted && _root != null) refresh()
            }
        } else refresh()
    }

    private fun gameMode() {
        val view = _root ?: return
        val btn = view.findViewById<Button>(R.id.btn_game_mode)
        btn.isEnabled = false

        EtApp.requestRoot { granted ->
            if (_root == null) return@requestRoot
            if (!granted) {
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_LONG).show()
                    btn.isEnabled = true
                }
                return@requestRoot
            }
            Thread {
                Root.cmd("am kill-all")
                Root.ok("pm trim-caches 999999999999")
                Root.cmd("sync; echo 3 > /proc/sys/vm/drop_caches")
                val freeAfter = memInfo().first
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    btn.isEnabled = true
                    updateRam(requireView())
                    Toast.makeText(
                        context,
                        getString(R.string.boost_done, freeAfter.toString()),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }.start()
        }
    }

    private fun setAnimations(scale: String) {
        EtApp.requestRoot { granted ->
            if (_root == null) return@requestRoot
            if (!granted) {
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_SHORT).show()
                }
                return@requestRoot
            }
            SysRepo.animationsSet(scale) { ok ->
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(
                        context,
                        if (ok) R.string.boost_anim_done else R.string.modules_action_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    loadExtras(requireView())
                }
            }
        }
    }

    private fun fstrim() {
        EtApp.requestRoot { granted ->
            if (_root == null) return@requestRoot
            if (!granted) {
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
                    Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_SHORT).show()
                }
                return@requestRoot
            }
            SysRepo.fstrim { ok, out ->
                requireActivity().runOnUiThread {
                    if (_root == null) return@runOnUiThread
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
