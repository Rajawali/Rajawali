package c.org.rajawali3d.materials;

import android.support.annotation.NonNull;
import org.rajawali3d.materials.Material;
import org.rajawali3d.renderer.FrameTask;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class MaterialManager {

    private final Set<Material> materials;

    public MaterialManager() {
        materials = Collections.synchronizedSet(new HashSet<Material>());
    }

    @NonNull
    public FrameTask addMaterial(@NonNull final Material material) {
        // Update the tracking structures first
        // Add the material to the collection
        materials.add(material);
        return new FrameTask() {
            @Override
            protected void doTask() throws Exception {
                material.add();
            }
        };
    }

    @NonNull
    public FrameTask removeMaterial(@NonNull final Material material) {
        // Update the tracking structures first
        // Remove the material from the collection
        materials.remove(material);
        return new FrameTask() {
            @Override
            protected void doTask() throws Exception {
                material.remove();
            }
        };
    }
}
