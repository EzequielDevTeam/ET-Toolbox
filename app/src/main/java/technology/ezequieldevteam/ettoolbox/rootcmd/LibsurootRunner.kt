package technology.ezequieldevteam.ettoolbox.rootcmd

import com.topjohnwu.superuser.Shell
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root

class LibsurootRunner : Root.Runner {

    override fun run(command: String): Pair<Boolean, String> {
        val result = Shell.cmd(command).exec()
        return result.isSuccess to result.out.joinToString("\n")
    }
}
