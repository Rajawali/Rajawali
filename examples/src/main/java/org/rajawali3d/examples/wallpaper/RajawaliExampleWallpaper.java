package org.rajawali3d.examples.wallpaper;

import org.rajawali3d.examples.Preferences;
import org.rajawali3d.examples.examples.general.BasicFragment.BasicRenderer;
import org.rajawali3d.examples.examples.general.CollisionDetectionFragment.CollisionDetectionRenderer;
import org.rajawali3d.examples.examples.general.SkyboxFragment.SkyboxRenderer;
import org.rajawali3d.examples.examples.general.TerrainFragment.TerrainRenderer;
import org.rajawali3d.examples.examples.optimizations.UpdateVertexBufferFragment.UpdateVertexBufferRenderer;
import org.rajawali3d.examples.examples.postprocessing.BloomEffectFragment.BloomEffectRenderer;
import org.rajawali3d.examples.examples.postprocessing.GaussianBlurFilterFragment.GaussianBlurFilterRenderer;
import org.rajawali3d.examples.examples.postprocessing.MultiPassFragment.MultiPassRenderer;
import org.rajawali3d.examples.examples.ui.CanvasTextFragment.CanvasTextRenderer;
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
        final Preferences preferences = Preferences.getInstance(this);
            switch (preferences.getWallpaperRendererPreference()) {
                case 0:
                    mRenderer = new BasicRenderer(this, null);
                    break;
                case 1:
                    mRenderer = new SkyboxRenderer(this, null);
                    break;
                case 2:
                    mRenderer = new CollisionDetectionRenderer(this, null);
                    break;
                case 3:
                    mRenderer = new TerrainRenderer(this, null);
                    break;
                case 4:
                    mRenderer = new UpdateVertexBufferRenderer(this, null);
                    break;
                case 5:
                    mRenderer = new BloomEffectRenderer(this, null);
                    break;
                case 6:
                    mRenderer = new GaussianBlurFilterRenderer(this, null);
                    break;
                case 7:
                    mRenderer = new MultiPassRenderer(this, null);
                    break;
                case 8:
                    mRenderer = new CanvasTextRenderer(this, null);
                    break;
                default:
                    mRenderer = new WallpaperRenderer(this);
            }
        RajLog.i("Creating wallpaper engine: " + mRenderer.getClass().getSimpleName());
        return new WallpaperEngine(getBaseContext(), mRenderer,
                                   ISurface.ANTI_ALIASING_CONFIG.NONE);
    }
}
