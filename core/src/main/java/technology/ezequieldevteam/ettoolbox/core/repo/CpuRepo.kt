package technology.ezequieldevteam.ettoolbox.core.repo

import technology.ezequieldevteam.ettoolbox.core.model.CpuInfo
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root

object CpuRepo {

    private const val BASE = "/sys/devices/system/cpu"

    private const val READ_SCRIPT =
        "g=\$(cat $BASE/cpu0/cpufreq/scaling_governor 2>/dev/null); " +
        "a=\$(cat $BASE/cpu0/cpufreq/scaling_available_governors 2>/dev/null); " +
        "mn=\$(cat $BASE/cpu0/cpufreq/scaling_min_freq 2>/dev/null); " +
        "mx=\$(cat $BASE/cpu0/cpufreq/scaling_max_freq 2>/dev/null); " +
        "fr=\$(cat $BASE/cpu*/cpufreq/scaling_cur_freq 2>/dev/null | tr '\\n' ' '); " +
        "echo \"G:\$g\"; echo \"A:\$a\"; echo \"N:\$mn:\$mx\"; echo \"F:\$fr\""

    fun read(callback: (info: CpuInfo) -> Unit) {
        Root.submit(READ_SCRIPT) { _, out ->
            var gov = ""
            var avail: List<String> = emptyList()
            var minKhz: Long? = null
            var maxKhz: Long? = null
            var mhzList: List<Long> = emptyList()

            for (line in out.lines()) {
                when {
                    line.startsWith("G:") -> gov = line.substring(2).trim()
                    line.startsWith("A:") ->
                        avail = line.substring(2).trim().split(' ').filter { it.isNotBlank() }
                    line.startsWith("N:") -> {
                        val p = line.substring(2).split(':')
                        minKhz = p.getOrNull(0)?.trim()?.toLongOrNull()
                        maxKhz = p.getOrNull(1)?.trim()?.toLongOrNull()
                    }
                    line.startsWith("F:") ->
                        mhzList = line.substring(2).trim().split(' ')
                            .mapNotNull { it.trim().toLongOrNull() }
                            .filter { it > 0 }
                            .map { it / 1000 }
                }
            }

            val info =
                if (gov.isBlank() || avail.isEmpty())
                    CpuInfo("", emptyList(), emptyList(), null, null,
                        "A CPU não expôs os arquivos cpufreq via root neste aparelho.")
                else
                    CpuInfo(gov, avail, mhzList, minKhz?.div(1000), maxKhz?.div(1000), null)

            callback(info)
        }
    }

    fun applyGovernor(governor: String, cores: Int, callback: (ok: Boolean) -> Unit) {
        val safe = governor.filter { it.isLetterOrDigit() || it == '_' || it == '-' }
        if (safe.isEmpty()) {
            callback(false)
            return
        }
        val script = "for i in \$(seq 0 ${cores - 1}); do " +
            "echo $safe > $BASE/cpu\$i/cpufreq/scaling_governor 2>/dev/null; done"
        Root.submit(script) { ok, _ -> callback(ok) }
    }
}
