package technology.ezequieldevteam.ettoolbox

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.google.android.material.color.DynamicColors
import com.topjohnwu.superuser.Shell
import technology.ezequieldevteam.ettoolbox.core.rootcmd.Root
import technology.ezequieldevteam.ettoolbox.rootcmd.LibsurootRunner
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

class EtApp : Application() {

    companion object {
        init {
            Shell.enableVerboseLogging = false
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_MOUNT_MASTER)
                    .setTimeout(90)
            )
        }

        @Volatile
        var rootAvailable = false
            private set

        private val main = Handler(Looper.getMainLooper())
        private val listeners = CopyOnWriteArrayList<(Boolean) -> Unit>()

        fun addRootListener(listener: (Boolean) -> Unit) {
            listeners.add(listener)
            main.post { listener(rootAvailable) }
        }

        fun removeRootListener(listener: (Boolean) -> Unit) {
            listeners.remove(listener)
        }

        /**
         * Garante que existe um shell (pede root ao Magisk se ainda nao houver decisao)
         * e reporta o resultado. Pode ser chamado quantas vezes quiser: o Magisk
         * memoriza a escolha do usuario, entao chamadas seguintes sao instantaneas.
         */
        fun requestRoot(onResult: ((Boolean) -> Unit)? = null) {
            thread(name = "et-root") {
                val granted = try {
                    Shell.getShell()
                    Shell.isAppGrantedRoot() == true
                } catch (t: Throwable) {
                    false
                }
                main.post {
                    if (granted != rootAvailable) {
                        rootAvailable = granted
                        listeners.forEach { it(granted) }
                    }
                    onResult?.invoke(granted)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Root.runner = LibsurootRunner()
        DynamicColors.applyToActivitiesIfAvailable(this)
        requestRoot()
    }
}
