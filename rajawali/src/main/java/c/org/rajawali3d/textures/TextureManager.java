package c.org.rajawali3d.textures;

import android.support.annotation.NonNull;
import c.org.rajawali3d.renderer.Renderer;
import org.rajawali3d.renderer.FrameTask;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class TextureManager {

    private final Renderer renderer;

    private final Set<ATexture> textures;

    public TextureManager(@NonNull Renderer renderer) {
        this.renderer = renderer;
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
}
