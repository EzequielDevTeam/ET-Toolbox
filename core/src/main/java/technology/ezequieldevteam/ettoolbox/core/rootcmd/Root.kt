package technology.ezequieldevteam.ettoolbox.core.rootcmd

import com.topjohnwu.superuser.Shell

object Root {

    fun cmd(command: String): String =
        Shell.cmd(command).out.joinToString("\n")

    fun ok(command: String): Boolean = Shell.cmd(command).isSuccess

    fun submit(command: String, callback: (ok: Boolean, out: String) -> Unit) {
        Thread {
            val result = Shell.cmd(command)
            callback(result.isSuccess, result.out.joinToString("\n"))
        }.start()
    }
}
