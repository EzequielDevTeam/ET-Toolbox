package technology.ezequieldevteam.ettoolbox.data

data class TrollPreset(
    val name: String,
    val title: String,
    val body: String
)

object TrollPresets {

    val all: List<TrollPreset> = listOf(
        TrollPreset(
            "Explosao",
            "AVISO CRITICO DO SISTEMA",
            "Seu telefone vai explodir em 20 segundos. Afaste-se do aparelho imediatamente."
        ),
        TrollPreset(
            "Rosto nao detectado",
            "Camera de seguranca",
            "Rosto nao detectado. Iniciando protocolo de seguranca."
        ),
        TrollPreset(
            "Atualizacao",
            "Atualizacao do sistema",
            "Baixando atualizacao gigante usando seus dados moveis. Nao desligue o aparelho."
        ),
        TrollPreset(
            "Bateria",
            "Bateria critica",
            "1% restante. O aparelho sera desligado em 5 segundos."
        ),
        TrollPreset(
            "Mae chegando",
            "Localizacao familiar",
            "Sua mae esta a 200 metros da porta. Guarde o controle do video game."
        )
    )
}
