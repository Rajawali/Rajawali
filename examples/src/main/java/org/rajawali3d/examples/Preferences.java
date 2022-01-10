package org.rajawali3d.examples;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import org.rajawali3d.util.RajLog;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class Preferences implements OnSharedPreferenceChangeListener {

    private static final String WALLPAPER_RENDERER_KEY = "org.rajawali3d.examples.Preferences.wallpaper_renderer";

    private static Preferences instance;

    private final SharedPreferences preferences;

    private int wallpaperRendererPreference;

    public static Preferences getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (Preferences.class) {
                if (instance == null) {
                    instance = new Preferences(context);
                }
            }
        }
        return instance;
    }

    private Preferences(@NonNull Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);

        updatePreferences();
    }

    private void updatePreferences() {
        try {
            wallpaperRendererPreference = Integer.parseInt(preferences.getString(WALLPAPER_RENDERER_KEY, "0"));
        } catch (Exception e) {
            RajLog.e("updatePreferences() failed: " + e.getMessage());
            wallpaperRendererPreference = 0;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // This has the potential to get heavy, but we will consider it ok for this demonstration
        updatePreferences();
    }

    @NonNull
    public int getWallpaperRendererPreference() {
        return wallpaperRendererPreference;
    }
}
