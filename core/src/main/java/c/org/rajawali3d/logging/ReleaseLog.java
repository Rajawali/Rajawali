package c.org.rajawali3d.logging;

import android.util.Log;
import org.rajawali3d.BuildConfig;
import timber.log.Timber;

/**
 * Release-mode logging for static methods
 *
 * @author Randy Picolet
 */

public class ReleaseLog extends Timber.Tree {

    static {
        if (!BuildConfig.DEBUG) {
            Timber.plant(new ReleaseLog());
        }
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        // Proguard should remove anything lower than Log.INFO, filtering anyway
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

        // TODO
    }
}
