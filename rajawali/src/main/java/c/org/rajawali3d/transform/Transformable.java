package c.org.rajawali3d.transform;

import android.support.annotation.NonNull;

/**
 * Interface which a scene member capable of being transformed must implement.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Transformable {

    /**
     * Initiates a request to transform this object. The caller will be blocked until a write lock is acquired and
     * the provided {@link Transformer} will prevent any render passes from occuring until its work is complete. This
     * method will ensure that prior to returning, the write lock is released.
     *
     * @param transformer {@link Transformer} implementation which will execute a series of transformations on this
     *                                       object.
     * @throws InterruptedException if the calling thread is interrupted while waiting for lock acquisition.
     */
    void requestTransformations(@NonNull Transformer transformer) throws InterruptedException;
}
