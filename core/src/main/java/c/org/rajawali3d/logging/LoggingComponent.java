package c.org.rajawali3d.logging;

import c.org.rajawali3d.annotations.DebugOnly;
import org.rajawali3d.BuildConfig;
import timber.log.Timber;

/**
 * Instance-specific debug-mode and release-mode logging and debug utilities
 *
 * @author Randy Picolet
 */

public abstract class LoggingComponent {

    /** Short name for convenience */
    public final static boolean DEBUG = BuildConfig.DEBUG;

    /** Semantic flag */
    public final static boolean TRACE = true;

    /**     */
    public final static boolean NO_TRACE = false;

    // Class-level release-mode logging tag (debug-mode tags are provided by Timber)
    private final String releaseTag = this.getClass().getSimpleName() + ": ";

    // Empty method text flag
    @DebugOnly
    private static final String EMPTY_TEXT = "";

    // Unknown method text flag
    @DebugOnly
    private static final String UNKNOWN_TEXT = "?";

    // String of blanks for message indentation;
    @DebugOnly
    private static final CharSequence INDENTS = "                                ";

    //
    @DebugOnly
    private static final int MAX_INDENT = INDENTS.length();

    // Component instance/logging ID counter
    @DebugOnly
    private static int logIdCounter = 0;

    // Process-scope instance logging ID
    @DebugOnly
    private final int logId = logIdCounter++;

    //
    @DebugOnly
    private final String logIdText = "@" + logId + "->";

    //
    @DebugOnly
    private final int logIdTextLength = logIdText.length();

    // Thread-local method text
    @DebugOnly
    private ThreadLocal<String> threadMethodText = new ThreadLocal<String>() {
        @Override protected String initialValue() {
            return EMPTY_TEXT;
        }
    };

    //
    @DebugOnly
    private static ThreadLocal<Integer> threadLoggingIndent = new ThreadLocal<Integer>() {
        @Override protected Integer initialValue() {
            return 0;
        }
    };

    //
    @DebugOnly
    private static final int INITIAL_MAX_MESSAGE_LENGTH = 120;

    //
    @DebugOnly
    private ThreadLocal<StringBuilder> threadMessageBuilder = new ThreadLocal<StringBuilder>() {
        @Override protected StringBuilder initialValue() {
            return new StringBuilder(INITIAL_MAX_MESSAGE_LENGTH);
        }
    };

    // Method tracing enable
    @DebugOnly
    private boolean isTracingEnabled = false;

    //
    // General logging context
    //

    /**     */
    protected String getReleaseTag() {
        return releaseTag;
    }

    /**     */
    @DebugOnly
    public final int getLogId() {
        return logId;
    }

    //
    // Method entry tracing
    //

    /**     */
    @DebugOnly
    public void isTracingEnabled(boolean enabled) {
        isTracingEnabled = enabled;
    }

    /**     */
    @DebugOnly
    public boolean isTracingEnabled() {
        return isTracingEnabled;
    }

    /**
     *
     * @param methodName
     * @param trace
     */
    @DebugOnly
    protected final void enter(String methodName, boolean trace) {
        enter(methodName);
        if (trace & isTracingEnabled) {
            logV("entry...");
        }
    }

    //
    //
    //

    /** Set current method text/indent for this thread */
    @DebugOnly
    protected final void enter(String methodName) {
        String nonNullMethodName = methodName;
        if (methodName == null) {
            nonNullMethodName = EMPTY_TEXT;
        }
        if (nonNullMethodName == EMPTY_TEXT) {
            nonNullMethodName = UNKNOWN_TEXT;
        }
        String methodText = threadMethodText.get();
        if (methodText != null) {
            threadMethodText.remove();
        }
        threadMethodText.set(nonNullMethodName + "(): ");
        threadLoggingIndent.set(threadLoggingIndent.get() + 1);
    }

    /**  Unset the current method text/indent for this thread */
    @DebugOnly
    protected final void exit() {
        threadMethodText.set(UNKNOWN_TEXT);
        int loggingIndent = threadLoggingIndent.get() - 1;
        threadLoggingIndent.set(loggingIndent < 0 ? 0 : loggingIndent);
    }

    //
    // Assertions
    //

    /**
     *
     * @param condition
     * @param errMsg
     */
    @DebugOnly
    public final void debugAssert(boolean condition, String errMsg) {
        if (!condition) {
            logAndThrowError(errMsg);
            logE(errMsg);
            throw new Error(releaseTag + "." + threadMethodText + errMsg);
        }
    }

    /**
     *
     * @param instance
     * @param refName
     */
    @DebugOnly
    public final void debugAssertNull(Object instance, String refName) {
        if (instance != null) {
            logAndThrowError(refName + ": should be null!");
        }
    }

    /**
     *
     * @param instance
     * @param refName
     */
    @DebugOnly
    public final void debugAssertNonNull(Object instance, String refName) {
        if (instance == null) {
            logAndThrowNullError(refName);
        }
    }

    /**
     *
     * @param condition
     * @param errMsg
     */
    public final void releaseAssert(boolean condition, String errMsg) {
        if (!condition) {
            logAndThrowError(errMsg);
        }
    }

    /**
     *
     * @param instance
     * @param refName
     */
    public final void releaseAssertNull(Object instance, String refName) {
        if (instance != null) {
            logAndThrowError(refName + ": should be null!");
        }
    }

    /**
     *
     * @param instance
     * @param refName
     */
    public final void releaseAssertNonNull(Object instance, String refName) {
        if (instance == null) {
            logAndThrowNullError(refName);
        }
    }

    //
    // Error handlers
    //

    /**     */
    @DebugOnly
    public final void debugError(String errMsg) {
        logAndThrowError(errMsg);
    }

    /**     */
    public final void releaseError(String errMsg) {
        logAndThrowError(errMsg);
    }

    /**     */
    public final void logAndThrowNullError(String errorContext) {
        logAndThrowError(errorContext + ": null reference!");
    }

    /**     */
    public final void logAndThrowError(String errMsg) {
        logE(errMsg);
        throw new Error(releaseTag + "." + threadMethodText + errMsg);
    }

    //
    // Basic log messaging
    //

    /** Log a verbose message with optional format args. */
    @DebugOnly
    public void logV(String message, Object... args) {
        Timber.v(prependContext(message), args);
    }

    /** Log a verbose exception and a message with optional format args. */
    @DebugOnly
    public void logV(Throwable t, String message, Object... args) {
        Timber.v(t, prependContext(message), args);
    }

    /** Log a verbose exception. */
    @DebugOnly
    public void logV(Throwable t) {
        Timber.v(t);
    }

    /** Log a debug message with optional format args. */
    @DebugOnly
    public void logD(String message, Object... args) {
        Timber.d(prependContext(message), args);
    }

    /** Log a debug exception and a message with optional format args. */
    @DebugOnly
    public void logD(Throwable t, String message, Object... args) {
        Timber.d(t, prependContext(message), args);
    }

    /** Log a debug exception. */
    @DebugOnly
    public void logD(Throwable t) {
        Timber.d(t);
    }

    /** Log an info message with optional format args. */
    public void logI(String message, Object... args) {
        Timber.i(prependContext(message), args);
    }

    /** Log an info exception and a message with optional format args. */
    public void logI(Throwable t, String message, Object... args) {
        Timber.i(t, prependContext(message), args);
    }

    /** Log an info exception. */
    public void logI(Throwable t) {
        Timber.i(t);
    }

    /** Log a warning message with optional format args. */
    public void logW(String message, Object... args) {
        Timber.w(prependContext(message), args);
    }

    /** Log a warning exception and a message with optional format args. */
    public void logW(Throwable t, String message, Object... args) {
        Timber.w(t, prependContext(message), args);
    }

    /** Log a warning exception. */
    public void logW(Throwable t) {
        Timber.w(t);
    }

    /** Log an error message with optional format args. */
    public void logE(String message, Object... args) {
        Timber.e(prependContext(message), args);
    }

    /** Log an error exception and a message with optional format args. */
    public void logE(Throwable t, String message, Object... args) {
        Timber.e(t, prependContext(message), args);
    }

    /** Log an error exception. */
    public void logE(Throwable t) {
        Timber.e(t);
    }

    /** Log an assert message with optional format args. */
    public void logWtf(String message, Object... args) {
        Timber.wtf(prependContext(message), args);
    }

    /** Log an assert exception and a message with optional format args. */
    public void logWtf(Throwable t, String message, Object... args) {
        Timber.wtf(t, prependContext(message), args);
    }

    /** Log an assert exception. */
    public void logWtf(Throwable t) {
        Timber.wtf(t);
    }

    protected String prependContext(String message) {
        if (DEBUG) {
            int indents = threadLoggingIndent.get();
            if (indents > MAX_INDENT) {
                indents = MAX_INDENT;
            }
            String methodText = threadMethodText.get();
            int length = indents + logIdTextLength + methodText.length() + message.length();
            return threadMessageBuilder.get()
                    .append(INDENTS, 0, indents)
                    .append(logId)
                    .append(threadMethodText.get())
                    .append(message)
                    .toString();
        }
        return releaseTag + message;
    }
}
