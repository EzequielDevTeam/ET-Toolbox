package technology.ezequieldevteam.ettoolbox.root

import com.topjohnwu.superuser.Shell

object Su {

    fun cmd(command: String): String =
        Shell.cmd(command).exec().out.joinToString("\n").trim()

    fun ok(command: String): Boolean =
        Shell.cmd(command).exec().isSuccess

    fun lines(command: String): List<String> =
        Shell.cmd(command).exec().out

    fun hasRoot(): Boolean = Shell.getShell().isRoot
}
