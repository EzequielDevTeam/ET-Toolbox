package technology.ezequieldevteam.ettoolbox.core.repo

import technology.ezequieldevteam.ettoolbox.core.model.BatteryInfo
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root

object SysRepo {

    fun battery(callback: (info: BatteryInfo?) -> Unit) {
        Root.submit("dumpsys battery") { _, out ->
            var level = -1
            var temp = 0
            var healthCode = 0
            var voltUV = 0
            var statusCode = 0
            for (line in out.lines()) {
                val t = line.trim()
                when {
                    t.startsWith("level:") -> level = t.substringAfter(':').trim().toIntOrNull() ?: level
                    t.startsWith("temperature:") -> temp = t.substringAfter(':').trim().toIntOrNull() ?: temp
                    t.startsWith("health:") -> healthCode = t.substringAfter(':').trim().toIntOrNull() ?: healthCode
                    t.startsWith("voltage:") -> voltUV = t.substringAfter(':').trim().toIntOrNull() ?: voltUV
                    t.startsWith("status:") -> statusCode = t.substringAfter(':').trim().toIntOrNull() ?: statusCode
                }
            }
            if (level < 0) {
                callback(null)
                return@submit
            }
            callback(
                BatteryInfo(
                    level = level,
                    tempC = temp / 10.0,
                    health = healthName(healthCode),
                    voltageMV = voltUV / 1000,
                    charging = chargeName(statusCode)
                )
            )
        }
    }

    private fun healthName(code: Int) = when (code) {
        2 -> "Boa"
        3 -> "Superaquecida"
        4 -> "Morta"
        5 -> "Sobretensão"
        7 -> "Fria"
        else -> "Desconhecida"
    }

    private fun chargeName(code: Int) = when (code) {
        2 -> "Carregando"
        3 -> "Descarregando"
        4 -> "Não carregando"
        5 -> "Cheia"
        else -> "Desconhecido"
    }

    fun densityCurrent(callback: (value: Int?, error: String?) -> Unit) {
        Root.submit("wm density") { _, out ->
            val nums = Regex("(\\d+)").findAll(out).mapNotNull { it.groupValues[1].toIntOrNull() }.toList()
            if (nums.isEmpty()) callback(null, "Não foi possível ler a densidade atual.")
            else callback(nums.last(), null)
        }
    }

    fun densitySet(value: Int, callback: (ok: Boolean) -> Unit) {
        Root.submit("wm density $value") { ok, _ -> callback(ok) }
    }

    fun densityReset(callback: (ok: Boolean) -> Unit) {
        Root.submit("wm density reset") { ok, _ -> callback(ok) }
    }

    fun animationsGet(callback: (scale: Double) -> Unit) {
        Root.submit(
            "s1=$(settings get global window_animation_scale); s2=$(settings get global transition_animation_scale); s3=$(settings get global animator_duration_scale); echo \"SC|\$s1|\$s2|\$s3\""
        ) { _, out ->
            val line = out.lines().firstOrNull { it.startsWith("SC|") } ?: ""
            val p = line.split("|")
            val v = p.getOrNull(1)?.toDoubleOrNull()
                ?: p.getOrNull(2)?.toDoubleOrNull()
                ?: p.getOrNull(3)?.toDoubleOrNull()
                ?: 1.0
            callback(v)
        }
    }

    fun animationsSet(scale: String, callback: (ok: Boolean) -> Unit) {
        val safe = scale.filter { it.isDigit() || it == '.' }
        val cmd =
            "settings put global window_animation_scale $safe; " +
            "settings put global transition_animation_scale $safe; " +
            "settings put global animator_duration_scale $safe"
        Root.submit(cmd) { ok, _ -> callback(ok) }
    }

    fun fstrim(callback: (ok: Boolean, out: String) -> Unit) {
        Root.submit("/system/bin/fstrim -v /data 2>&1") { ok, out -> callback(ok, out.trim()) }
    }

    fun storageFree(callback: (line: String) -> Unit) {
        Root.submit("df -h /data | tail -n 1 | awk '{print \$3\" usados / \"\$2\" total / \"\$4\" livres\"}'") { _, out ->
            callback(out.trim())
        }
    }

    fun zramLine(callback: (line: String) -> Unit) {
        Root.submit(
            "st=$(grep SwapTotal /proc/meminfo 2>/dev/null); z=\$(cat /sys/block/zram0/mm_stat 2>/dev/null | awk '{print \$1}'); echo \"Z|\$st|\$z\""
        ) { _, out ->
            val l = out.lines().firstOrNull { it.startsWith("Z|") } ?: ""
            val p = l.split("|")
            val swap = p.getOrNull(1)?.substringAfter(':')?.trim().orEmpty()
            val bytes = p.getOrNull(2)?.trim()?.toLongOrNull()
            val zramMB = if (bytes != null && bytes > 0) "${bytes / 1048576} MB" else "-"
            callback("Swap: $swap | zram gravado: $zramMB")
        }
    }

    fun reboot(mode: String, callback: (ok: Boolean) -> Unit) {
        val arg = when (mode) {
            "recovery" -> "recovery"
            "bootloader" -> "bootloader"
            else -> ""
        }
        val value = if (arg.isBlank()) "reboot" else "reboot,$arg"
        Root.submit("setprop sys.powerctl \"$value\"") { ok, _ -> callback(ok) }
    }
}
