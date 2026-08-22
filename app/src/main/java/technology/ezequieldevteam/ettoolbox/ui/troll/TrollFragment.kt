package technology.ezequieldevteam.ettoolbox.ui.troll

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
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
import com.google.android.material.chip.Chip
import technology.ezequieldevteam.ettoolbox.MainActivity
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.data.TrollPresets
import technology.ezequieldevteam.ettoolbox.databinding.FragmentTrollBinding

class TrollFragment : Fragment() {

    private var _binding: FragmentTrollBinding? = null
    private val binding get() = _binding!!
    private val channelId = "et_troll"
    private val notifId = 1337

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTrollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        createChannel()
        setupPresets()

        binding.trollTitle.setText(TrollPresets.all.first().title)
        binding.trollBody.setText(TrollPresets.all.first().body)
        binding.btnSend.setOnClickListener { fire() }
    }

    private fun setupPresets() {
        for (preset in TrollPresets.all) {
            val chip = Chip(requireContext())
            chip.text = preset.name
            chip.isCheckable = false
            chip.setOnClickListener {
                binding.trollTitle.setText(preset.title)
                binding.trollBody.setText(preset.body)
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun createChannel() {
        val nm = requireContext().getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(channelId, "ET Toolbox", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private fun fire() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 42)
            return
        }

        val title = binding.trollTitle.text.toString().trim()
            .ifBlank { getString(R.string.app_name) }
        val body = binding.trollBody.text.toString().trim()
        val delaySec = binding.trollDelay.text.toString().toIntOrNull()?.coerceIn(0, 600) ?: 0

        if (delaySec > 0) {
            binding.btnSend.isEnabled = false
            view?.postDelayed({ fireNow(title, body); binding.btnSend.isEnabled = true }, delaySec * 1000L)
            Toast.makeText(context, "Disparando em ${delaySec}s...", Toast.LENGTH_SHORT).show()
        } else {
            fireNow(title, body)
        }
    }

    private fun fireNow(title: String, body: String) {
        val pi = PendingIntent.getActivity(
            requireContext(), 0,
            Intent(requireContext(), MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        requireContext().getSystemService(NotificationManager::class.java).notify(notifId, n)
        Toast.makeText(context, R.string.troll_sent, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 42 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) fire()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
