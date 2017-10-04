package c.org.rajawali3d.textures;

import android.support.annotation.NonNull;
import android.util.Log;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.scene.BaseScene;
import c.org.rajawali3d.scene.Scene;

import net.jcip.annotations.ThreadSafe;
import c.org.rajawali3d.core.RenderTask;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Manages texture data in the context of a {@link Scene} in a thread safe manner. As a general rule, each
 * {@link Scene} should have its own instance of {@link TextureManager}.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public final class TextureManager {

    private static final String TAG = "TextureManager";

    private final Set<BaseTexture> textures;
    private final Scene scene;

    public TextureManager(@NonNull Scene scene) {
        textures = Collections.synchronizedSet(new HashSet<BaseTexture>());
        this.scene = scene;
    }

    /**
     * Adds a texture to this manager. This can be called from any thread. If the calling thread is the GL thread, this
     * will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param texture {@link BaseTexture} to be added.
     */
    public void addTexture(@NonNull final BaseTexture texture) {
        // Update the tracking structures first
        // Add the texture to the collection
        textures.add(texture);
        ((BaseScene)scene).executeRenderTask(new RenderTask() {
            @Override
            protected void doTask() throws Exception {
                texture.add();
            }
        });
    }

    /**
     * Removes a texture from this manager. This can be called from any thread. If the calling thread is the GL thread,
     * this will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param texture {@link BaseTexture} to be removed.
     */
    public void removeTexture(@NonNull final BaseTexture texture) {
        // Update the tracking structures first
        // Remove the texture from the collection
        textures.remove(texture);
        ((BaseScene)scene).executeRenderTask(new RenderTask() {
            @Override
            protected void doTask() throws Exception {
                texture.remove();
            }
        });
    }

    /**
     * Replaces a texture in this manager. This can be called from any thread. If the calling thread is the GL thread,
     * this will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param texture {@link BaseTexture} to be replaced.
     *
     * @throws TextureException Thrown if an internal error occurs while trying to replace the texture.
     */
    public void replaceTexture(@NonNull final BaseTexture texture) throws TextureException {
        // Check the tracking structure first
        if (!textures.contains(texture)) {
            throw new TextureException("Tried replacing texture " + texture + " but it was not previously added.");
        }
        ((BaseScene)scene).executeRenderTask(new RenderTask() {
            @Override
            protected void doTask() throws Exception {
                texture.replace();
            }
        });
    }

    @RenderThread
    public void reloadTextures() {
        if (textures.size() != 0) {
            synchronized (textures) {
                final Iterator<BaseTexture> iterator = textures.iterator();
                while (iterator.hasNext()) {
                    final BaseTexture texture = iterator.next();
                    try {
                        if (texture.willRecycle()) {
                            // Remove this texture...we weren't tracking its data and cant restore it automatically
                            texture.remove();
                            iterator.remove();
                        } else {
                            // Reload this texture
                            texture.add();
                        }
                    } catch (TextureException exception) {
                        Log.e(TAG, "Failed while trying to reload texture " + texture, exception);
                    }
                }
            }
        }
    }
}
