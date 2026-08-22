package technology.ezequieldevteam.ettoolbox.ui

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
import technology.ezequieldevteam.ettoolbox.databinding.FragmentBoostBinding
import technology.ezequieldevteam.ettoolbox.root.Su

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

    private fun memInfo(): Pair<Long, Long> {
        val mi = android.os.ActivityManager.MemoryInfo()
        val am = requireContext().getSystemService(android.app.ActivityManager::class.java)
        am.getMemoryInfo(mi)
        return mi.availMem / 1048576 to mi.totalMem / 1048576
    }

    private fun updateRam() {
        val (free, total) = memInfo()
        binding.ramInfo.text = getString(R.string.boost_ram, "${free}MB", "${total}MB")
    }

    private fun gameMode() {
        if (!EtApp.rootAvailable) {
            Toast.makeText(context, "Root não disponível 😢", Toast.LENGTH_SHORT).show()
            return
        }
        Su.cmd("am kill-all")
        Su.ok("pm trim-caches 999999999999")
        Su.cmd("sync; echo 3 > /proc/sys/vm/drop_caches")
        System.gc()
        updateRam()
        val (free, _) = memInfo()
        Toast.makeText(context, "MODO JOGO ATIVO! ${free}MB livres 🎮", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
