package technology.ezequieldevteam.ettoolbox.data

/**
 * Catalogo de pacotes comuns considerados bloatware.
 * Baseado nas listas do projeto universal-android-debloater (GPL-3.0).
 * Cada entrada e verificada contra o aparelho em tempo de execucao.
 */
data class BloatItem(
    val packageName: String,
    val label: String,
    val description: String
)

object BloatCatalog {

    val all: List<BloatItem> = listOf(
        BloatItem(
            "com.google.android.marvin.talkback",
            "TalkBack",
            "Leitor de tela para acessibilidade. Desative apenas se nao utiliza."
        ),
        BloatItem(
            "com.android.printspooler",
            "Servico de impressao",
            "Imprimir documentos pelo celular. Raramente usado."
        ),
        BloatItem(
            "com.android.printservice.recommendation",
            "Recomendacoes de impressao",
            "Sugere impressoras. Inutil sem o servico de impressao."
        ),
        BloatItem(
            "com.google.ar.core",
            "ARCore",
            "Realidade aumentada da Google. Pesa bastante se voce nao usa apps AR."
        ),
        BloatItem(
            "com.google.android.projection.gearhead",
            "Android Auto",
            "Integracao com centrais multimidia de carros."
        ),
        BloatItem(
            "com.google.android.feedback",
            "Feedback",
            "Envia relatorios de erro para a Google."
        ),
        BloatItem(
            "com.google.android.apps.wellbeing",
            "Bem-estar digital",
            "Controles de tempo de uso. Alguns preferem remover."
        ),
        BloatItem(
            "com.google.android.gm",
            "Gmail",
            "Cliente de e-mail da Google."
        ),
        BloatItem(
            "com.google.android.youtube",
            "YouTube",
            "Aplicativo oficial do YouTube."
        ),
        BloatItem(
            "com.google.android.googlequicksearchbox",
            "Busca Google",
            "Widget de busca e feed da Google. Cuidado: removelo pode afetar a barra de pesquisa do launcher."
        ),
        BloatItem(
            "com.google.android.apps.googlecamera.fishfood",
            "Google Camera (teste)",
            "Variante experimental da camera. Se a camera padrao funciona, esta versao sobra."
        ),
        BloatItem(
            "com.android.dreams.basic",
            "Protetor de tela",
            "Protetor de tela basico do sistema."
        ),
        BloatItem(
            "com.android.emergency",
            "Emergencia",
            "Informacoes de emergencia na tela de bloqueio."
        ),
        BloatItem(
            "com.android.stk",
            "SIM Toolkit",
            "Ferramentas da operadora dentro do chip. Geralmente inutil."
        )
    )
}
