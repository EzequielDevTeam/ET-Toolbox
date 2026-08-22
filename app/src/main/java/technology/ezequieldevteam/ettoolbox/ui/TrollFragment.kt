package technology.ezequieldevteam.ettoolbox.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.databinding.FragmentTrollBinding

class TrollFragment : Fragment() {

    private var _binding: FragmentTrollBinding? = null
    private val binding get() = _binding!!
    private val channelId = "et_troll"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTrollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        createChannel()
        binding.trollTitle.setText("⚠️ AVISO CRÍTICO DO SISTEMA")
        binding.trollBody.setText("Seu telefone vai explodir em 20 segundos. Corra.")
        binding.btnSend.setOnClickListener { fire() }
    }

    private fun createChannel() {
        val nm = requireContext().getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(channelId, "ET Troll 😈", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private fun fire() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 42)
            return
        }
        val title = binding.trollTitle.text.toString().ifBlank { "⚠️ ET Toolbox" }
        val body = binding.trollBody.text.toString()
        val n = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
        requireContext().getSystemService(NotificationManager::class.java).notify(1337, n)
        Toast.makeText(context, "Disparado! 😈", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 42 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) fire()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
