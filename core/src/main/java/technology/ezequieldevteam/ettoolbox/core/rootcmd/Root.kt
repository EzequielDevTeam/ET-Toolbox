package technology.ezequieldevteam.ettoolbox.core.rootcmd

import java.util.concurrent.atomic.AtomicBoolean

object Root {

    /**
     * Abstracao do shell root. A camada Android injeta uma implementacao
     * (libsu) no inicio do app; o modulo core permanece Kotlin JVM puro.
     */
    interface Runner {
        fun run(command: String): Pair<Boolean, String>
    }

    @Volatile
    lateinit var runner: Runner

    val ready: Boolean get() = this::runner.isInitialized

    const val TIMEOUT_MS = 15_000L
    const val TIMEOUT_MSG = "O shell root não respondeu em 15 segundos."

    private fun safeRun(command: String): Pair<Boolean, String> =
        try {
            runner.run(command)
        } catch (t: Throwable) {
            false to ("Erro no shell: ${t.message ?: t.javaClass.simpleName}")
        }

    fun cmd(command: String): String = safeRun(command).second

    fun ok(command: String): Boolean = safeRun(command).first

    /**
     * Executa em background com watchdog: se o shell nao responder em
     * TIMEOUT_MS, entrega erro ao callback de qualquer forma. O resultado
     * chega exatamente uma vez - nunca deixa a tela travada em "carregando".
     */
    fun submit(command: String, callback: (ok: Boolean, out: String) -> Unit) {
        val done = AtomicBoolean(false)

        val worker = Thread {
            val (success, output) = safeRun(command)
            if (done.compareAndSet(false, true)) {
                callback(success, output)
            }
        }

        val watchdog = Thread {
            try {
                worker.join(TIMEOUT_MS)
            } catch (_: InterruptedException) {
            }
            if (done.compareAndSet(false, true)) {
                callback(false, TIMEOUT_MSG)
            }
        }

        worker.start()
        watchdog.start()
    }
}
