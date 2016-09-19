package org.rajawali3d.textures;

import android.support.annotation.NonNull;
import android.util.Log;
import c.org.rajawali3d.annotations.GLThread;
import org.rajawali3d.renderer.FrameTask;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class TextureManager {

    private static final String TAG = "TextureManager";

    private final Set<ATexture> textures;

    public TextureManager() {
        textures = Collections.synchronizedSet(new HashSet<ATexture>());
    }

    @NonNull
    public FrameTask addTexture(@NonNull final ATexture texture) {
        // Update the tracking structures first
        // Add the texture to the collection
        textures.add(texture);
        return new FrameTask() {
            @Override
            protected void doTask() throws Exception {
                texture.add();
            }
        };
    }

    @NonNull
    public FrameTask removeTexture(@NonNull final ATexture texture) {
        // Update the tracking structures first
        // Remove the texture from the collection
        textures.remove(texture);
        return new FrameTask() {
            @Override
            protected void doTask() throws Exception {
                texture.remove();
            }
        };
    }

    @NonNull
    public FrameTask replaceTexture(@NonNull final ATexture texture) throws TextureException {
        // Check the tracking structure first
        if (!textures.contains(texture)) {
            throw new TextureException("Tried replacing texture " + texture + " but it was not previously added.");
        }
        return new FrameTask() {
            @Override
            protected void doTask() throws Exception {
                texture.replace();
            }
        };
    }

    @GLThread
    public void reloadTextures() {
        if (textures.size() != 0) {
            synchronized (textures) {
                final Iterator<ATexture> iterator = textures.iterator();
                while (iterator.hasNext()) {
                    final ATexture texture = iterator.next();
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
