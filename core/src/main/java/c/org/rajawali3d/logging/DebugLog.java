package c.org.rajawali3d.logging;

import org.rajawali3d.BuildConfig;
import timber.log.Timber;

/**
 * Debug-mode logging for static methods and non-LoggingComponents
 *
 * @author Randy Picolet
 */
public class DebugLog extends Timber.DebugTree {

    static {
        if(BuildConfig.DEBUG) {
            Timber.plant(new DebugLog());
        }
    }
}
