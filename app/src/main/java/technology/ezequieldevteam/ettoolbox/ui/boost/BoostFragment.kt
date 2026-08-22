package technology.ezequieldevteam.ettoolbox.ui.boost

import android.app.ActivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.databinding.FragmentBoostBinding
import technology.ezequieldevteam.ettoolbox.root.Su
import kotlin.concurrent.thread

class BoostFragment : Fragment() {

    private var _binding: FragmentBoostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBoostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateRam()
        binding.btnGameMode.setOnClickListener { gameMode() }
    }

    override fun onResume() {
        super.onResume()
        updateRam()
    }

    private fun memInfo(): Pair<Long, Long> {
        val mi = ActivityManager.MemoryInfo()
        requireContext().getSystemService(ActivityManager::class.java).getMemoryInfo(mi)
        return mi.availMem / 1048576L to mi.totalMem / 1048576L
    }

    private fun updateRam() {
        thread {
            val (free, total) = memInfo()
            requireActivity().runOnUiThread {
                if (_binding != null) {
                    binding.ramInfo.text =
                        getString(R.string.boost_ram, "${free} MB", "${total} MB")
                }
            }
        }
    }

    private fun gameMode() {
        if (!EtApp.rootAvailable) {
            Toast.makeText(context, R.string.boost_no_root, Toast.LENGTH_LONG).show()
            return
        }
        binding.btnGameMode.isEnabled = false
        thread {
            Su.cmd("am kill-all")
            Su.ok("pm trim-caches 999999999999")
            Su.cmd("sync; echo 3 > /proc/sys/vm/drop_caches")
            val freeAfter = memInfo().first
            requireActivity().runOnUiThread {
                if (_binding == null) return@runOnUiThread
                binding.btnGameMode.isEnabled = true
                updateRam()
                Toast.makeText(
                    context,
                    getString(R.string.boost_done, freeAfter.toString()),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
