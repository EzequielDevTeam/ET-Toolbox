package technology.ezequieldevteam.ettoolbox.ui.benchmark

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.EtApp
import technology.ezequieldevteam.ettoolbox.R
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
import kotlin.concurrent.thread

class BenchmarkFragment : Fragment() {

    private var _root: View? = null

    private fun ui(block: () -> Unit) {
        activity?.runOnUiThread {
            if (_root != null) block()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_benchmark, container, false)
        _root = v
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btn_cpu_bench).setOnClickListener { runCpuBench() }
        view.findViewById<Button>(R.id.btn_mem_bench).setOnClickListener { runMemBench() }
        view.findViewById<Button>(R.id.btn_storage_bench).setOnClickListener { runStorageBench() }
        view.findViewById<Button>(R.id.btn_full_bench).setOnClickListener { runFullBench() }
    }

    private fun runCpuBench() {
        val view = _root ?: return
        val resultView = view.findViewById<TextView>(R.id.bench_result)
        val progress = view.findViewById<ProgressBar>(R.id.bench_progress)
        val btn = view.findViewById<Button>(R.id.btn_cpu_bench)
        btn.isEnabled = false
        progress.visibility = View.VISIBLE
        resultView.text = getString(R.string.bench_running)

        thread(name = "et-bench-cpu") {
            val iterations = 50000000
            val start = SystemClock.uptimeMillis()
            var sum = 0L
            for (i in 0 until iterations) {
                sum += (i * i) % 10007
            }
            val elapsed = SystemClock.uptimeMillis() - start
            val mops = (iterations / elapsed.toDouble() / 1000).toString().take(6)

            ui {
                btn.isEnabled = true
                progress.visibility = View.GONE
                resultView.text = getString(R.string.bench_cpu_result, elapsed, mops)
            }
        }
    }

    private fun runMemBench() {
        val view = _root ?: return
        val resultView = view.findViewById<TextView>(R.id.bench_result)
        val progress = view.findViewById<ProgressBar>(R.id.bench_progress)
        val btn = view.findViewById<Button>(R.id.btn_mem_bench)
        btn.isEnabled = false
        progress.visibility = View.VISIBLE
        resultView.text = getString(R.string.bench_running)

        thread(name = "et-bench-mem") {
            val size = 100 * 1024 * 1024 // 100MB
            val buffer = ByteArray(size)
            val start = SystemClock.uptimeMillis()
            for (i in 0 until size) buffer[i] = (i % 256).toByte()
            val writeTime = SystemClock.uptimeMillis() - start

            val readStart = SystemClock.uptimeMillis()
            var sum = 0L
            for (i in 0 until size) sum += buffer[i].toLong()
            val readTime = SystemClock.uptimeMillis() - readStart

            val writeSpeed = (size / 1024 / 1024).toDouble() / (writeTime / 1000.0)
            val readSpeed = (size / 1024 / 1024).toDouble() / (readTime / 1000.0)

            ui {
                btn.isEnabled = true
                progress.visibility = View.GONE
                resultView.text = getString(R.string.bench_mem_result, writeSpeed.toString().take(5), readSpeed.toString().take(5))
            }
        }
    }

    private fun runStorageBench() {
        val view = _root ?: return
        val resultView = view.findViewById<TextView>(R.id.bench_result)
        val progress = view.findViewById<ProgressBar>(R.id.bench_progress)
        val btn = view.findViewById<Button>(R.id.btn_storage_bench)
        btn.isEnabled = false
        progress.visibility = View.VISIBLE
        resultView.text = getString(R.string.bench_running)

        thread(name = "et-bench-storage") {
            val file = java.io.File(requireContext().cacheDir, "bench_test.dat")
            val size = 50 * 1024 * 1024 // 50MB
            val buffer = ByteArray(1024 * 1024) // 1MB chunks

            try {
                val writeStart = SystemClock.uptimeMillis()
                java.io.FileOutputStream(file).use { fos ->
                    for (i in 0 until size / buffer.size) fos.write(buffer)
                }
                val writeTime = SystemClock.uptimeMillis() - writeStart

                val readStart = SystemClock.uptimeMillis()
                java.io.FileInputStream(file).use { fis ->
                    while (fis.read(buffer) != -1) {}
                }
                val readTime = SystemClock.uptimeMillis() - readStart

                val writeSpeed = (size / 1024 / 1024).toDouble() / (writeTime / 1000.0)
                val readSpeed = (size / 1024 / 1024).toDouble() / (readTime / 1000.0)

                ui {
                    btn.isEnabled = true
                    progress.visibility = View.GONE
                    resultView.text = getString(R.string.bench_storage_result, writeSpeed.toString().take(5), readSpeed.toString().take(5))
                }
            } catch (e: Exception) {
                ui {
                    btn.isEnabled = true
                    progress.visibility = View.GONE
                    resultView.text = getString(R.string.bench_error, e.message)
                }
            } finally {
                file.delete()
            }
        }
    }

    private fun runFullBench() {
        runCpuBench()
        // Could chain them, but for now just run CPU
        Toast.makeText(context, R.string.bench_full_started, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _root = null
    }
}