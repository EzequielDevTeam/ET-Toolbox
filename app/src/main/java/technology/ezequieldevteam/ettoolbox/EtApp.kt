package technology.ezequieldevteam.ettoolbox

import android.app.Application
import com.topjohnwu.superuser.Shell

class EtApp : Application() {
    companion object {
        init {
            Shell.enableVerboseLogging = false
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(10)
            )
        }
        var rootAvailable = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        rootAvailable = Shell.getShell().isRoot
    }
}
