package technology.ezequieldevteam.ettoolbox

import android.app.AlertDialog
import android.widget.ScrollView
import android.widget.TextView

object CreditsDialog {

    fun show(activity: MainActivity) {
        val pad = (16 * activity.resources.displayMetrics.density).toInt()

        val text = TextView(activity).apply {
            setText(R.string.credits_body)
            textSize = 15f
            setPadding(pad, pad / 2, pad, 0)
            setTextIsSelectable(true)
        }
        val scroll = ScrollView(activity).apply { addView(text) }

        AlertDialog.Builder(activity)
            .setTitle(R.string.credits_title)
            .setView(scroll)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
