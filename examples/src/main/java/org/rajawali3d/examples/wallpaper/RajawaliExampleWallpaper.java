package org.rajawali3d.examples.wallpaper;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.rajawali3d.examples.examples.general.BasicFragment.BasicRenderer;
import org.rajawali3d.examples.examples.general.CollisionDetectionFragment.CollisionDetectionRenderer;
import org.rajawali3d.examples.examples.general.SkyboxFragment.SkyboxRenderer;
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.wallpaper.Wallpaper;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public class RajawaliExampleWallpaper extends Wallpaper {

    private ISurfaceRenderer mRenderer;

    @Override
    public Engine onCreateEngine() {
        RajLog.v("Creating wallpaper engine.");
        final SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final int renderer = Integer.parseInt(mSharedPreferences.getString("renderer_class", "0"));
            RajLog.i("Creating wallpaper engine: " + renderer);
            switch (renderer) {
                case 0:
                    mRenderer = new BasicRenderer(this, null);
                    break;
                case 1:
                    mRenderer = new SkyboxRenderer(this, null);
                    break;
                case 2:
                    mRenderer = new CollisionDetectionRenderer(this, null);
                    break;
                default:
                    mRenderer = new WallpaperRenderer(this);
            }
        return new WallpaperEngine(getBaseContext(), mRenderer, ISurface.ANTI_ALIASING_CONFIG.NONE);
    }
}
