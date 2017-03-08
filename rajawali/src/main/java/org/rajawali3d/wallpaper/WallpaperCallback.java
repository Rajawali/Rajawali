package org.rajawali3d.wallpaper;

import android.view.MotionEvent;

/**
 * @author Randy Picolet
 */

public interface WallpaperCallback {
    /**
     * NOTE: Only relevant when rendering a live wallpaper.
     *
     * Called to inform you of the wallpaper's offsets changing within its contain, corresponding to the container's
     * call to WallpaperManager.setWallpaperOffsets().
     *
     * @param xOffset
     * @param yOffset
     * @param xOffsetStep
     * @param yOffsetStep
     * @param xPixelOffset
     * @param yPixelOffset
     */
    void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
                          float yOffsetStep, int xPixelOffset, int yPixelOffset);

    /**
     * Called as the user performs touch-screen interaction with the window that is currently showing this wallpaper.
     * Note that the events you receive here are driven by the actual application the user is interacting with,
     * so if it is slow you will get fewer move events.
     *
     * @param event {@link MotionEvent} The touch event.
     */
    void onTouchEvent(MotionEvent event);
}
