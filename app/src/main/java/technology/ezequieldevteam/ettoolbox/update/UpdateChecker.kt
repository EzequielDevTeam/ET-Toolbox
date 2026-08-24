package technology.ezequieldevteam.ettoolbox.update

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import technology.ezequieldevteam.ettoolbox.BuildConfig
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {

    private const val LATEST =
        "https://api.github.com/repos/EzequielDevTeam/ET-Toolbox/releases/latest"
    private const val UA = "ET-Toolbox-Updater"

    private val main = Handler(Looper.getMainLooper())

    data class Release(
        val tag: String,
        val apkUrl: String,
        val notes: String
    )

    fun check(onResult: (release: Release?, error: String?) -> Unit) {
        Thread {
            var connection: HttpURLConnection? = null
            var release: Release? = null
            var error: String? = null
            try {
                connection = (URL(LATEST).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                    instanceFollowRedirects = true
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", UA)
                }
                val code = connection.responseCode
                if (code != 200) {
                    error = "Servidor respondeu HTTP $code."
                } else {
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    val tag = json.optString("tag_name", "")
                    val notes = json.optString("body", "")
                    var apkUrl = ""
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            if (asset.optString("name", "").endsWith(".apk", ignoreCase = true)) {
                                apkUrl = asset.optString("browser_download_url", "")
                                break
                            }
                        }
                    }
                    if (tag.isBlank() || apkUrl.isBlank()) {
                        error = "Nenhum APK publicado na última release ainda."
                    } else {
                        release = Release(tag, apkUrl, notes)
                    }
                }
            } catch (e: Exception) {
                error = "Falha ao verificar atualizações: ${e.message ?: "erro desconhecido"}"
            } finally {
                connection?.disconnect()
            }
            main.post { onResult(release, error) }
        }.start()
    }

    fun isNewer(releaseTag: String): Boolean {
        val remote = releaseTag.trim().removePrefix("v").trim()
        val local = BuildConfig.VERSION_NAME.trim().removePrefix("v").trim()
        return remote.isNotBlank() && remote != local
    }
}
