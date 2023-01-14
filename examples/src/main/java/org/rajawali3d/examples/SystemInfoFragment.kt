package org.rajawali3d.examples

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SystemInfoFragment : PreferenceFragmentCompat() {

    companion object {
        private const val PREFERENCE_ = "preference_"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = super.onCreateView(inflater, container, savedInstanceState)
        container?.context?.setTheme(R.style.PreferenceTheme)
        return view
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_system)
        findPreference<Preference>(PREFERENCE_ + "APPVERSION")?.summary = BuildConfig.VERSION_NAME
        findPreference<Preference>(PREFERENCE_ + "BOARD")?.summary = Build.BOARD
        findPreference<Preference>(PREFERENCE_ + "BRAND")?.summary = Build.BRAND
        findPreference<Preference>(PREFERENCE_ + "CPU_ABI")?.summary = Build.SUPPORTED_ABIS[0]
        findPreference<Preference>(PREFERENCE_ + "DISPLAY")?.summary = Build.DISPLAY
        findPreference<Preference>(PREFERENCE_ + "USER")?.summary = Build.USER
    }
}