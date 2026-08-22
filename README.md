# ET Toolbox

**ET Toolbox** é uma caixa de ferramentas completa para aparelhos Android com root, desenvolvida e mantida pela **EzequielDevTeam Technology**. O projeto nasceu da necessidade real de reunir, em um único aplicativo leve e direto, várias funções que normalmente exigiriam meio dúzia de apps separados: acelerar o sistema para jogos, limpar lixo de fábrica, gerenciar módulos Magisk, controlar a CPU e até pregar peças nos amigos com notificações falsas.

Tudo isso em um APK pequeno, sem anúncios, sem telemetria, sem coleta de dados e sem qualquer tipo de monetização.

---

## O que este aplicativo faz

O ET Toolbox é organizado em cinco abas, cada uma com um propósito claro:

### 1. Boost (Modo Jogo)

A aba principal para quem quer desempenho máximo antes de jogar. Com um único botão, o aplicativo executa uma sequência de limpeza de memória no nível do sistema:

- Encerra todos os processos em segundo plano (`am kill-all`);
- Corta os caches do gerenciador de pacotes (`pm trim-caches`);
- Libera a memória física do kernel descartando caches de disco (`drop_caches`).

Antes de aplicar qualquer coisa, a aba mostra em tempo real quanta RAM está livre e qual o total do aparelho. Depois de aplicar o modo jogo, os números são atualizados para você ver exatamente quanto de memória foi recuperado. Em aparelhos com pouca RAM (3 a 4 GB), a diferença costuma ser de centenas de megabytes liberados na hora.

### 2. Troll (Notificações)

Uma função de pura diversão: envia notificações falsas de alta prioridade no aparelho, perfeitas para pregar peças. O aplicativo vem com vários presets prontos — aviso de explosão do sistema, bateria crítica, atualização gigante baixando com seus dados móveis, "sua mãe está chegando", entre outros.

Você também pode escrever título e corpo personalizados, além de definir um **atraso em segundos**: a pessoa pega o celular, você dispara escondido, e a notificação aparece minutos depois quando ela já esqueceu. É inofensivo, não altera nada do sistema e serve só para rir.

### 3. Limpeza (Bloatware e Cache)

Aqui mora uma das funções mais úteis do aplicativo. A lista de bloatware vem pré-carregada com os pacotes de fábrica mais conhecidos que ficam rodando em segundo plano sem servir para nada: apps de operadora, assistentes de voz redundantes, serviços de nuvem duplicados, navegadores impostos pelo fabricante e por aí vai.

Para cada item, o ET Toolbox mostra:

- Nome amigável e descrição em português do que aquele pacote faz;
- Se ele está instalado no seu aparelho;
- Se está ativo ou desativado;
- Um botão para desativar ou reativar com um toque.

Importante: o aplicativo usa `pm disable-user`, que é o método **reversível** — nada é desinstalado de verdade, então se algo parar de funcionar é só reativar o pacote pela própria lista. Nenhum risco real de brick. Além disso, há um botão dedicado de limpeza geral de cache que libera espaço instantaneamente e mostra quanto ficou livre.

### 4. Módulos (Gerenciador Magisk)

Um gerenciador completo dos módulos Magisk instalados, direto pela interface do app:

- Lista todos os módulos presentes em `/data/adb/modules` com nome e versão lidos do `module.prop`;
- Mostra o estado de cada um (ativo, desativado ou com remoção pendente);
- Permite ativar/desativar qualquer módulo com um toque (via arquivo `disable`);
- Permite marcar módulos para remoção na próxima reinicialização (via arquivo `remove`).

É a mesma mecânica que o próprio Magisk usa internamente, respeitando o protocolo oficial de módulos. Nada é apagado imediatamente — a remoção acontece de forma segura no reboot, exatamente como o Magisk espera.

### 5. Aparelho (Informações, CPU e Identidade)

A aba técnica, dividida em três cartões:

**Informações do aparelho** — fabricante, modelo, versão do Android, nível do patch de segurança, SoC, número de núcleos, RAM total, versão do kernel e status do root. Tudo em texto puro, direto ao ponto.

**Governador da CPU** — leitura das frequências atuais de todos os núcleos em tempo real e do governador em uso. Você pode trocar o governador de **todos os núcleos de uma vez** escolhendo entre os disponíveis no kernel do seu aparelho (performance, powersave, schedutil, etc.). Quer o celular voando? `performance`. Quer bateria durando o dia inteiro? `powersave`. Um toque e pronto.

**Spoof de identidade** — permite mudar temporariamente o modelo e a marca reportados pelo aparelho usando `resetprop`, com backup automático dos valores originais em `/data/local/tmp/ettoolbox_spoof_backup` e botão de restauração dedicado. Útil para testar compatibilidade de apps, enganar verificações bobas de hardware ou simplesmente zoar quem for ver as configurações do telefone. Tudo reversível com um clique.

---

## Requisitos

- **Android 8.0 (API 26) ou superior**;
- **Root via Magisk** — as funções de sistema (boost, limpeza, módulos, CPU, spoof) exigem acesso root. As funções Troll funcionam mesmo sem root, mas pedem permissão de notificações no Android 13+;
- Arquitetura arm64-v8a (padrão em praticamente todos os aparelhos modernos).

## Instalação

1. Baixe o APK mais recente na página de [Releases](https://github.com/EzequielDevTeam/ET-Toolbox/releases) deste repositório;
2. Instale normalmente (permita "instalar apps de fontes desconhecidas" se for a primeira vez);
3. Abra o app e conceda acesso root ao Magisk quando solicitado;
4. Pronto. Todas as abas estarão funcionais.

Não há versão na Play Store e nunca haverá. Este é um projeto independente distribuído diretamente aos usuários.

## Sobre o suporte ao Android 17

Muita gente já perguntou, então vamos deixar registrado aqui de forma clara e definitiva:

**O suporte ao Android 17 vai chegar, mas somente depois que todos os telefones e custom ROMs estiverem atualizados.**

Isso não é preguiça nem falta de capacidade técnica — é uma decisão consciente de engenharia. Cada versão nova do Android muda comportamentos internos, APIs privadas, restrições de SELinux e mecanismos de segurança. Lançar uma versão "pronta para Android 17" agora, antes de o ecossistema real absorver a novidade, seria entregar aos usuários um aplicativo testado contra um sistema que quase ninguém tem, cheio de caminhos que ainda vão mudar até o lançamento final e nas ROMs estáveis.

Nossa estratégia é a mesma que sempre funcionou bem neste projeto:

1. Esperar o Android 17 ser finalizado e publicado pelo AOSP;
2. Aguardar as custom ROMs principais (LineageOS, crDroid, Pixel Experience e derivados) portarem e estabilizarem a base;
3. Verificar na prática, em aparelhos reais rodando essas ROMs, o que muda de comportamento;
4. Só então publicar a versão com suporte oficial ao Android 17, testada de verdade e não no chute.

Enquanto isso, o ET Toolbox continua funcionando normalmente nas versões atuais, incluindo o Android 16. Quando a hora certa chegar, a atualização sai aqui primeiro.

## Por que fazemos isto

Este projeto foi feito **por pura boa vontade**. Não existe empresa por trás, não existem patrocinadores cobrando contrapartidas, não existe assinatura, não existe versão "premium", não existem anúncios e não existe coleta de nenhum dado. Nenhum byte do seu aparelho sai dele por causa deste aplicativo.

Nós mantemos o ET Toolbox porque gostamos do que fazemos, porque acreditamos que o usuário tem direito a ferramentas honestas no próprio aparelho que pagou, e porque a cena Android de código aberto sempre nos deu muito — este é o nosso jeito de devolver.

Sobre o ritmo de atualizações, para evitar expectativas erradas: **não prometemos atualização todo dia** — isso seria mentira e ninguém consegue manter esse ritmo com qualidade. O que garantimos é manutenção **constante e responsiva**: correções saem rápido quando aparece um problema, melhorias entram continuamente conforme temos tempo, e cada versão passa por teste real em aparelho físico antes de ser publicada. Preferimos lançar menos vezes e lançar certo do que encher o histórico de releases quebradas.

Se o projeto te ajudou, considere dar uma estrela no repositório. É de graça, leva dois segundos e é o combustível que mantém a gente seguindo.

## Compilando você mesmo

O projeto usa Gradle 8.7 + AGP 8.5 + Kotlin 1.9.24. Para compilar localmente:

```bash
git clone https://github.com/EzequielDevTeam/ET-Toolbox.git
cd ET-Toolbox
./gradlew :app:assembleDebug
```

O APK será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

Existe também um workflow de GitHub Actions neste repositório (`.github/workflows/build.yml`) que compila automaticamente a cada push — o APK resultante fica disponível como artefato da execução, então você pode baixar builds fresquinhos sem instalar nada na sua máquina.

## Estrutura do projeto

```
ET-Toolbox/
├── .github/workflows/build.yml      # CI: compila o APK a cada push
├── app/
│   ├── build.gradle.kts             # Configuração do módulo (libsu, Material 3)
│   └── src/main/
│       ├── AndroidManifest.xml      # Permissões (notificações, vibrar)
│       └── java/technology/ezequieldevteam/ettoolbox/
│           ├── EtApp.kt             # Classe Application (cores dinâmicas + root)
│           ├── MainActivity.kt      # Host das 5 abas + navegação inferior
│           ├── data/
│           │   ├── BloatCatalog.kt  # Catálogo de bloatware conhecido
│           │   └── TrollPresets.kt  # Presets de notificação troll
│           ├── root/
│           │   └── Su.kt            # Camada de execução root (libsu)
│           └── ui/
│               ├── boost/           # Modo jogo / limpeza de RAM
│               ├── clean/           # Bloatware + cache (lista + adapter)
│               ├── device/          # Info, CPU e spoof de identidade
│               ├── modules/         # Gerenciador de módulos Magisk
│               └── troll/           # Notificações personalizadas
└── settings.gradle.kts              # Repositórios (Google, Maven Central, JitPack)
```

## Créditos e licença

- Interface construída com [Material Components Android](https://github.com/material-components/material-components-android);
- Execução root via [libsu](https://github.com/topjohnwu/libsu), do topjohnwu, criador do Magisk;
- Inspirado na filosofia de projetos como LSPosed e Shamiko, que provaram que ferramentas de usuário avançado podem ser livres, leves e confiáveis.

Este projeto é distribuído "no estado em que se encontra", sem garantias. Use com bom senso — principalmente as funções de sistema. Desativar bloatware e mexer em governadores de CPU são operações seguras e reversíveis, mas lembre-se: o aparelho é seu e as decisões também.

---

**ET Toolbox** — EzequielDevTeam Technology
Feito à mão, com root e sem pressa. Atualização constante, qualidade em primeiro lugar.
