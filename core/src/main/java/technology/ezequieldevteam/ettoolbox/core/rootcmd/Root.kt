package technology.ezequieldevteam.ettoolbox.core.rootcmd

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

    fun cmd(command: String): String = runner.run(command).second

    fun ok(command: String): Boolean = runner.run(command).first

    fun submit(command: String, callback: (ok: Boolean, out: String) -> Unit) {
        Thread {
            val (success, output) = runner.run(command)
            callback(success, output)
        }.start()
    }
}
