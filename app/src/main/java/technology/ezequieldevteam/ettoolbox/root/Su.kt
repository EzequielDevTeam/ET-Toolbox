package technology.ezequieldevteam.ettoolbox.root

import com.topjohnwu.superuser.Shell

object Su {

    fun cmd(command: String): String =
        Shell.cmd(command).exec().out.joinToString("\n").trim()

    fun ok(command: String): Boolean =
        Shell.cmd(command).exec().isSuccess

    fun lines(command: String): List<String> =
        Shell.cmd(command).exec().out

    fun hasRoot(): Boolean = try {
        Shell.getShell().isRoot
    } catch (t: Throwable) {
        false
    }

    fun exists(path: String): Boolean = ok("test -e \"$path\"")

    fun read(path: String): String = cmd("cat \"$path\"")

    fun write(path: String, content: String): Boolean =
        ok("printf '%s\\n' \"${content.replace("$", "\\$").replace("\"", "\\\"")}\" > \"$path\"")

    fun append(path: String, content: String): Boolean =
        ok("printf '%s\\n' \"${content.replace("$", "\\$").replace("\"", "\\\"")}\" >> \"$path\"")

    fun prop(name: String): String =
        cmd("resetprop \"$name\" 2>/dev/null").ifBlank { cmd("getprop \"$name\"") }

    fun setProp(name: String, value: String): Boolean =
        ok("resetprop \"$name\" \"${value.replace("\"", "")}\"")

    fun delProp(name: String): Boolean = ok("resetprop --delete \"$name\"")

    fun writeFileSysfs(path: String, value: String): Boolean =
        ok("echo \"$value\" > \"$path\"")

    fun backupFile(src: String, dst: String): Boolean =
        ok("[ -f \"$dst\" ] || cp \"$src\" \"$dst\"")

    fun restoreBackup(dst: String, src: String): Boolean =
        ok("[ -f \"$dst\" ] && cp \"$dst\" \"$src\"")
}
