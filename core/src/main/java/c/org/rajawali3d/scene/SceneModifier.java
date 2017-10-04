package c.org.rajawali3d.scene;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RequiresWriteLock;

/**
 * Interface which must be implemented in order to modify a {@link Scene}.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface SceneModifier {

    /**
     * Called when a write lock has been acquired on the renderScene and the {@link SceneModifier} may perform its
     * work. Note that rendering will be blocked until this method returns.
     *
     * @param scene {@link Scene} The current renderScene object which is to be modified.
     */
    @RequiresWriteLock
    void doModifications(@NonNull Scene scene);
}
