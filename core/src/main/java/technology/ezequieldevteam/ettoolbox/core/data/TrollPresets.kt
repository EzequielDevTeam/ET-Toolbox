package technology.ezequieldevteam.ettoolbox.core.data

import technology.ezequieldevteam.ettoolbox.core.model.TrollPreset

object TrollPresets {

    val all: List<TrollPreset> = listOf(
        TrollPreset(
            "Bateria acabando",
            "Bateria acabando",
            "Sua bateria está a 1%. Conecte o carregador antes que seja tarde..."
        ),
        TrollPreset(
            "Update disponível",
            "Update disponível",
            "Uma atualização crítica do sistema está pronta para instalar agora."
        ),
        TrollPreset(
            "Armazenamento cheio",
            "Armazenamento cheio",
            "Seu armazenamento está esgotado. Delete tudo ou compre um cartão."
        ),
        TrollPreset(
            "Você foi invadido",
            "Você foi invadido",
            "Conexão suspeita detectada no seu Wi-Fi. Verifique seus aparelhos."
        ),
        TrollPreset(
            "Boletos",
            "Lembrete importante",
            "Hoje é dia de pagar os boletos! Corre antes que vença."
        ),
        TrollPreset(
            "ET está aqui",
            "ET está aqui",
            "Telefone... casa... O ET Toolbox passou por aqui."
        )
    )
}
