package technology.ezequieldevteam.ettoolbox.core.model

data class MagiskRow(
    val id: String,
    val name: String,
    val version: String,
    val state: String
) {
    val enabled: Boolean get() = state == "ON"
    val markedRemove: Boolean get() = state == "DEL"
}

data class CpuInfo(
    val currentGovernor: String,
    val availableGovernors: List<String>,
    val freqsMhz: List<Long>,
    val minMhz: Long?,
    val maxMhz: Long?,
    val error: String?
)

data class BatteryInfo(
    val level: Int,
    val tempC: Double,
    val health: String,
    val voltageMV: Int,
    val charging: String
)

data class BloatItem(
    val packageName: String,
    val label: String,
    val description: String,
    val safe: Boolean
)

data class TrollPreset(
    val name: String,
    val title: String,
    val body: String
)
