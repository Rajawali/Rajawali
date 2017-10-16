package c.org.rajawali3d.materials;

import c.org.rajawali3d.annotations.RequiresRenderTask;
import c.org.rajawali3d.scene.Scene;
import org.rajawali3d.materials.Material;

import android.support.annotation.NonNull;

import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO Is this still valid?  pre reqs material programs/shaders can be shared across Scenes...
 * Manages materials in the context of a {@link Scene} in a thread safe manner. As a general rule, each
 * {@link Scene} should have its own instance of {@link MaterialManager}.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class MaterialManager {

    private final Set<Material> materials;

    // TODO Singleton/process scope?
    public MaterialManager() {
        materials = Collections.synchronizedSet(new HashSet<Material>());
    }

    /**
     * Adds a material to this manager. This can be called from any thread. If the calling thread is the GL thread, this
     * will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param material {@link Material} to be added.
     */
    @RequiresRenderTask
    public void addMaterial(@NonNull final Material material) {
        // Update the tracking structures first
        // Add the material to the collection
        materials.add(material);
        material.add();
    }

    /**
     * Removes a material from this manager. This can be called from any thread. If the calling thread is the GL thread,
     * this will be executed immediately, otherwise it will be queued for execution on the GL thread.
     *
     * @param material {@link Material} to be removed.
     */
    @RequiresRenderTask
    public void removeMaterial(@NonNull final Material material) {
        // Update the tracking structures first
        // Remove the material from the collection
        materials.remove(material);
        material.remove();
    }
}
