package technology.ezequieldevteam.ettoolbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import technology.ezequieldevteam.ettoolbox.R

class CleanFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val tv = TextView(requireContext())
        val pad = (48 * resources.displayMetrics.density).toInt()
        tv.setPadding(pad, pad, pad, pad)
        tv.text = getString(R.string.soon)
        return tv
    }
}
