package c.org.rajawali3d.materials;

import c.org.rajawali3d.control.RenderTask;
import c.org.rajawali3d.scene.BaseScene;
import c.org.rajawali3d.scene.Scene;
import org.rajawali3d.materials.Material;

import android.support.annotation.NonNull;

import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages materials in the context of a {@link Scene} in a thread safe manner. As a general rule, each
 * {@link Scene} should have its own instance of {@link MaterialManager}.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class MaterialManager {

    private final Set<Material> materials;

    private final Scene scene;

    public MaterialManager(@NonNull Scene scene) {
        materials = Collections.synchronizedSet(new HashSet<Material>());
        this.scene = scene;
    }

    /**
     * Adds a material to this manager. This can be called from any thread. If the calling thread is the GL thread, this
     * will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param material {@link Material} to be added.
     */
    public void addMaterial(@NonNull final Material material) {
        // Update the tracking structures first
        // Add the material to the collection
        materials.add(material);
        ((BaseScene) scene).executeRenderTask(new RenderTask() {
            @Override
            protected void doTask() throws Exception {
                material.add();
            }
        });
    }

    /**
     * Removes a material from this manager. This can be called from any thread. If the calling thread is the GL thread,
     * this will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param material {@link Material} to be removed.
     */
    public void removeMaterial(@NonNull final Material material) {
        // Update the tracking structures first
        // Remove the material from the collection
        materials.remove(material);
        ((BaseScene) scene).executeRenderTask(new RenderTask() {
            @Override
            protected void doTask() throws Exception {
                material.remove();
            }
        });
    }
}
