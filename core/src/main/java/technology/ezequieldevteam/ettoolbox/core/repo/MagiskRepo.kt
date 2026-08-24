package technology.ezequieldevteam.ettoolbox.core.repo

import technology.ezequieldevteam.ettoolbox.core.model.MagiskRow
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root

object MagiskRepo {

    private const val LIST_SCRIPT =
        "for d in /data/adb/modules/*/; do id=\$(basename \"\$d\"); n=\$(sed -n 's/^name=//p' \"\$d/module.prop\" 2>/dev/null | head -n 1); v=\$(sed -n 's/^version=//p' \"\$d/module.prop\" 2>/dev/null | head -n 1); st=\"ON\"; [ -f \"\$d/disable\" ] && st=\"OFF\"; [ -f \"\$d/remove\" ] && st=\"DEL\"; echo \"ROW|\$id|\$st|\$n|\$v\"; done"

    fun list(callback: (rows: List<MagiskRow>, error: String?) -> Unit) {
        Root.submit(LIST_SCRIPT) { ok, out ->
            if (!ok && out.isBlank()) {
                callback(emptyList(), "O shell root não respondeu ao listar módulos.")
                return@submit
            }
            val rows = mutableListOf<MagiskRow>()
            for (line in out.lines()) {
                if (!line.startsWith("ROW|")) continue
                val parts = line.split("|", limit = 5)
                if (parts.size < 3) continue
                val id = parts[1]
                if (id.isBlank() || id == "*") continue
                val name = if (parts.size > 3 && parts[3].isNotBlank()) parts[3] else id
                val version = if (parts.size > 4) parts[4] else ""
                rows.add(MagiskRow(id, name, version, parts[2]))
            }
            callback(rows.sortedBy { it.name.lowercase() }, null)
        }
    }

    fun toggle(id: String, currentlyEnabled: Boolean, callback: (ok: Boolean) -> Unit) {
        val flag = if (currentlyEnabled) "touch /data/adb/modules/$id/disable" else "rm -f /data/adb/modules/$id/disable"
        Root.submit(flag) { ok, _ -> callback(ok) }
    }

    fun markRemove(id: String, callback: (ok: Boolean) -> Unit) {
        Root.submit("touch /data/adb/modules/$id/remove") { ok, _ -> callback(ok) }
    }
}
