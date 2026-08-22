package technology.ezequieldevteam.ettoolbox.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.databinding.FragmentDeviceBinding

class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val runtime = Runtime.getRuntime()
        binding.deviceInfo.text = buildString {
            appendLine("📱 Aparelho: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("🤖 Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("🏷️ Patch de segurança: ${Build.VERSION.SECURITY_PATCH}")
            appendLine("⚙️ SoC: ${Build.HARDWARE} (${Build.SOC_MODEL})")
            appendLine("🧠 Núcleos CPU: ${Runtime.getRuntime().availableProcessors()}")
            appendLine("💾 RAM total: ${totalRamMb()}MB")
            appendLine("🧩 Kernel: ${System.getProperty("os.version")}")
            appendLine("🔧 Java heap máx: ${(runtime.maxMemory() / 1048576)}MB")
        }
    }

    private fun totalRamMb(): Long {
        val mi = android.os.ActivityManager.MemoryInfo()
        requireContext().getSystemService(android.app.ActivityManager::class.java).getMemoryInfo(mi)
        return mi.totalMem / 1048576
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
