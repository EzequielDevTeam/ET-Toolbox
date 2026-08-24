package technology.ezequieldevteam.ettoolbox.core.data

import technology.ezequieldevteam.ettoolbox.core.model.BloatItem

object BloatCatalog {

    val all: List<BloatItem> = listOf(
        // Google
        BloatItem("com.google.android.apps.tachyon", "Google Duo/Messages", "App de vídeo do Google", true),
        BloatItem("com.google.android.videos", "Google TV Filmes", "Loja de filmes", true),
        BloatItem("com.google.android.apps.youtube.music", "YouTube Music", "Streaming de música", true),
        BloatItem("com.google.android.googlequicksearchbox", "Barra de pesquisa", "Widget/pesquisa Google", false),
        BloatItem("com.google.android.printservice.recommendation", "Serviço de impressão", "Recomendações de impressoras", true),
        BloatItem("com.google.android.apps.betterbug", "Better Bug", "Relatório de bugs do Google", true),
        BloatItem("com.google.android.feedback", "Feedback", "Formulários de opinião", true),
        BloatItem("com.google.android.gms.location.history", "Histórico de local", "Rastreamento de localização", false),

        // Meta
        BloatItem("com.facebook.appmanager", "Facebook App Manager", "Gerenciador em segundo plano do FB", true),
        BloatItem("com.facebook.system", "Facebook App Installer", "Instalador silencioso do FB", true),
        BloatItem("com.facebook.services", "Facebook Services", "Serviços em segundo plano do FB", true),

        // Motorola
        BloatItem("com.motorola.moto", "Moto App", "Hub de dicas da Motorola", true),
        BloatItem("com.motorola.genie", "Moto Care", "Suporte/checkup Motorola", true),
        BloatItem("com.motorola.assist", "Assist Moto", "Assistente de ações", true),
        BloatItem("com.motorola.faceunlock", "Face Unlock", "Desbloqueio facial (cuidado!)", false),
        BloatItem("com.motorola.motocit", "Motorola Cit", "Modo de teste de fábrica", true),
        BloatItem("com.motorola.demo", "Retail Demo", "Modo loja/demonstração", true),
        BloatItem("com.motorola.gamemode", "Game Time", "Modo jogo da Moto (duplicado)", true),
        BloatItem("com.motorola.zap", "Moto Zap", "Compartilhamento local (obsoleto)", true),
        BloatItem("com.motorola.motogallery", "Recursos Galeria Moto", "Extras da galeria", true),

        // Operadoras / outros
        BloatItem("br.com.gvt.safari", "Claro Video", "Streaming da operadora", true),
        BloatItem("com.redbend.vdm.tc", "Agente da operadora", "Gerenciamento remoto da operadora", true),
        BloatItem("com.amazon.appmanager", "Amazon Appstore", "Instalador da Amazon", true),
        BloatItem("com.evervolv.sdk", "SDK de telemetria", "Coleta de dados do sistema", true),
        BloatItem("com.dti.att", "AT&T Services", "Serviços AT&T (se aplicável)", true),
        BloatItem("com.myvodafoneapp", "Vodafone App", "App da operadora (se aplicável)", true)
    )
}
