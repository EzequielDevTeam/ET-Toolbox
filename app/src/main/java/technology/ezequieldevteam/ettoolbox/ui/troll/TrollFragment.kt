package technology.ezequieldevteam.ettoolbox.ui.troll

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import technology.ezequieldevteam.ettoolbox.MainActivity
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.data.TrollPresets
import technology.ezequieldevteam.ettoolbox.databinding.FragmentTrollBinding

class TrollFragment : Fragment() {

    private var _binding: FragmentTrollBinding? = null
    private val binding get() = _binding!!
    private val channelId = "et_troll"
    private val notifId = 1337

    private val handler = Handler(Looper.getMainLooper())
    private val pendingJobs = mutableListOf<Runnable>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTrollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        createChannel()
        setupPresets()
        setupUnitSpinner()

        val first = TrollPresets.all.first()
        binding.trollTitle.setText(first.title)
        binding.trollBody.setText(first.body)

        binding.btnSend.setOnClickListener { fire() }
        binding.btnTrollCancel.setOnClickListener { cancelAll(true) }
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

    private fun setupUnitSpinner() {
        val units = resources.getStringArray(R.array.troll_units)
        binding.spinnerUnit.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, units
        )
        binding.spinnerUnit.setSelection(0)
    }

    private fun unitFactor(): Long = when (binding.spinnerUnit.selectedItemPosition) {
        1 -> 60L          // minutos
        2 -> 3600L        // horas
        else -> 1L        // segundos
    }

    private fun unitName(): String =
        resources.getStringArray(R.array.troll_units)[binding.spinnerUnit.selectedItemPosition].lowercase()

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

        val delayRaw = binding.trollDelay.text.toString().toLongOrNull()?.coerceIn(0, 9999) ?: 0
        val millis = delayRaw * 1000L * unitFactor()
        val vibrate = binding.cbVibrate.isChecked

        if (millis > 0) {
            val job = Runnable { fireNow(vibrate) }
            pendingJobs.add(job)
            handler.postDelayed(job, millis)
            Toast.makeText(
                context,
                getString(R.string.troll_scheduled, delayRaw.toString(), unitName()),
                Toast.LENGTH_LONG
            ).show()
        } else {
            fireNow(vibrate)
        }
    }

    private fun fireNow(vibrate: Boolean) {
        val title = binding.trollTitle.text.toString().trim()
            .ifBlank { getString(R.string.app_name) }
        val body = binding.trollBody.text.toString().trim()

        if (vibrate) doVibrate()

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
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        requireContext().getSystemService(NotificationManager::class.java).notify(notifId, n)
        Toast.makeText(context, R.string.troll_sent, Toast.LENGTH_SHORT).show()
    }

    @Suppress("DEPRECATION")
    private fun doVibrate() {
        val vibrator: Vibrator? =
            if (Build.VERSION.SDK_INT >= 31) {
                (requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                    ?.defaultVibrator
            } else {
                requireContext().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 350, 150, 350), -1))
    }

    private fun cancelAll(announce: Boolean) {
        if (pendingJobs.isEmpty()) {
            if (announce) Toast.makeText(context, R.string.troll_nothing_pending, Toast.LENGTH_SHORT).show()
            return
        }
        pendingJobs.forEach { handler.removeCallbacks(it) }
        pendingJobs.clear()
        if (announce) Toast.makeText(context, R.string.troll_cancelled, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 42 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) fire()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pendingJobs.forEach { handler.removeCallbacks(it) }
        pendingJobs.clear()
        _binding = null
    }
}
