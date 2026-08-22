package technology.ezequieldevteam.ettoolbox.root

import com.topjohnwu.superuser.Shell

object Su {
    fun cmd(command: String): String =
        Shell.cmd(command).exec().out.joinToString("\n")

    fun ok(command: String): Boolean = Shell.cmd(command).exec().isSuccess
}
