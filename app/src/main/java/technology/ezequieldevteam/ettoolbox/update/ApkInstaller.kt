package technology.ezequieldevteam.ettoolbox.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import technology.ezequieldevteam.ettoolbox.MainActivity
import java.io.File

object ApkInstaller {

    fun downloadAndInstall(activity: MainActivity, tag: String, url: String) {
        val fileName = "ETToolbox-$tag.apk"

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(fileName)
            setDescription("Baixando atualização do ET Toolbox")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(
                activity,
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
        }

        val manager =
            activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) != downloadId) return
                activity.unregisterReceiver(this)

                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = manager.query(query)
                var statusOk = false
                var uri: Uri? = null
                cursor.use { c ->
                    if (c.moveToFirst()) {
                        val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        statusOk = status == DownloadManager.STATUS_SUCCESSFUL
                        if (statusOk) {
                            uri = manager.getUriForDownloadedFile(downloadId)
                        }
                    }
                }

                if (!statusOk) {
                    activity.onInstallDownloadFailed()
                    return
                }

                installApk(activity, File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName), uri)
            }
        }

        activity.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
    }

    private fun installApk(activity: MainActivity, file: File, directUri: Uri?) {
        val uri: Uri = directUri ?: FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.files",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            activity.startActivity(intent)
        } catch (_: Exception) {
            activity.onInstallOpenFailed()
        }
    }
}
