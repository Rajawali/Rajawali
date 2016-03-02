package org.rajawali3d.examples.wallpaper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import org.rajawali3d.examples.R;

/**
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class FragmentPreferences extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private SharedPreferences mSharedPreferences;

    private int[] preferencesToLoad() {
        return new int[] { R.xml.preferences};
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getPreferenceManager().getSharedPreferences();

        // Load defined preferences
        for (int preferenceResource : preferencesToLoad()) {
            addPreferencesFromResource(preferenceResource);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
