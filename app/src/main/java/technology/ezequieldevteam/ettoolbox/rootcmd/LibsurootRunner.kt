package technology.ezequieldevteam.ettoolbox.rootcmd

import com.topjohnwu.superuser.Shell
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root

class LibsuRunner : Root.Runner {

    override fun run(command: String): Pair<Boolean, String> {
        val result = Shell.cmd(command)
        return result.isSuccess to result.out.joinToString("\n")
    }
}
