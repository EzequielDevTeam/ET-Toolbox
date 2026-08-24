# Arquitetura do ET Toolbox (v0.4.0)

## Visão geral

Projeto Gradle com **dois módulos**, no estilo multi-pasta dos grandes projetos open source:

```
ETToolbox/
├── app/      → UI Android (Fragments, layouts, Material 3)
├── core/     → lógica pura em Kotlin JVM (sem Android)
├── docs/     → esta documentação
├── scripts/  → utilitários de desenvolvimento
└── .github/  → CI que gera o APK
```

## Módulo `core` (Kotlin JVM puro)

Não conhece nada de Android. Pode ser testado em qualquer JVM.

| Pacote | Responsabilidade |
|---|---|
| `rootcmd.Root` | Acesso ao shell root via libsu (`cmd`, `ok`, `submit`) |
| `model.Models` | Data classes compartilhadas (MagiskRow, CpuInfo, BatteryInfo, BloatItem, TrollPreset) |
| `data.BloatCatalog` | Catálogo expandido de bloatware |
| `data.TrollPresets` | Presets de trollagem |
| `repo.MagiskRepo` | Lista/toggle/remove módulos em `/data/adb/modules` |
| `repo.CpuRepo` | Lê e aplica governadores de CPU |
| `repo.SysRepo` | Bateria, densidade, animações, fstrim, zram, reboot |

### Padrão anti-bug adotado

Cada operação usa **um único comando shell** cuja saída vem com marcadores
(`ROW|id|estado|nome|versão`, `G:`, `A:`, `N:`, `F:`). O resultado é parseado
no Kotlin e o erro, quando existe, volta para a tela — nada de "carregando infinito".

Callbacks chegam na thread do shell; a camada `app` faz `runOnUiThread`.

## Módulo `app`

- `EtApp` — Application: cria o shell libsu (mount master) e gerencia o pedido de root
  sob demanda (`requestRoot`) com listeners.
- `MainActivity` — toolbar superior (créditos + verificação de atualização), banner de
  root, navegação inferior com 5 abas.
- `ui.*` — um pacote por aba (boost / troll / clean / modules / device).
- `update.UpdateChecker` — consulta a API `releases/latest` do GitHub e compara a tag
  com a `versionName` local.
- `update.ApkInstaller` — baixa o APK pelo DownloadManager e dispara o instalador do
  sistema via FileProvider.

## Fluxo de atualização automática

1. App consulta `https://api.github.com/repos/EzequielDevTeam/ET-Toolbox/releases/latest`.
2. Se a tag difere da versão instalada, mostra diálogo com as notas da release.
3. Usuário aceita → DownloadManager baixa o APK anexado à release.
4. Ao terminar, abre o instalador do sistema (permissão REQUEST_INSTALL_PACKAGES).

## CI

`.github/workflows/build.yml`: JDK 17 → `./gradlew :app:assembleDebug --no-daemon`,
publicando o APK como artifact `ETToolbox-debug`.
