package technology.ezequieldevteam.ettoolbox.ui.modules

data class ModuleUpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val changelog: String,
    val hasUpdate: Boolean
)