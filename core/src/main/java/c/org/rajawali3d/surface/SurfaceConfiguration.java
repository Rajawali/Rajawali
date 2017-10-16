package c.org.rajawali3d.surface;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import c.org.rajawali3d.control.RenderControl;

/**
 * @author Randy Picolet
 */

public class SurfaceConfiguration {

    protected double frameRate = RenderControl.USE_DISPLAY_REFRESH_RATE;
    protected SurfaceAntiAliasing surfaceAntiAliasing = SurfaceAntiAliasing.NONE;
    protected int multiSampleCount = 0;
    protected boolean isTransparent = false;
    protected int bitsRed = 5;
    protected int bitsGreen = 6;
    protected int bitsBlue = 5;
    protected int bitsAlpha = 0;
    protected int bitsDepth = 16;

    public SurfaceConfiguration() {
    }

    public SurfaceConfiguration(double frameRate, SurfaceAntiAliasing surfaceAntiAliasing,
                                int multiSampleCount, boolean isTransparent,
                                int bitsRed, int bitsGreen, int bitsBlue, int bitsAlpha, int bitsDepth) {
        setFrameRate(frameRate);
        setSurfaceAntiAliasing(surfaceAntiAliasing);
        setMultiSampleCount(multiSampleCount);
        setColorBits(bitsRed, bitsGreen, bitsBlue,bitsAlpha);
        setBitsDepth(bitsDepth);
    }

    public SurfaceConfiguration setFrameRate(double frameRate) {
        this.frameRate = frameRate;
        return this;
    }

    public double getFrameRate() {
        return frameRate;
    }

    /**
     * Sets the surface-wide anti-aliasing mode, overriding any layout attribute.
     *
     * @param surfaceAntiAliasing {@link SurfaceAntiAliasing} type to apply; default is {@link SurfaceAntiAliasing#NONE}
     * @return this {@link SurfaceConfiguration} to enable chaining of set calls
     */
    public SurfaceConfiguration setSurfaceAntiAliasing(@NonNull SurfaceAntiAliasing surfaceAntiAliasing) {
        this.surfaceAntiAliasing = surfaceAntiAliasing;
        return this;
    }

    /**
     * @return
     */
    public SurfaceAntiAliasing getSurfaceAntiAliasing() {
        return surfaceAntiAliasing;
    }

    /**
     * Sets the sample count when using {@link SurfaceAntiAliasing#MULTI_SAMPLING}, overriding any layout attribute.
     *
     * @param count
     * @return this {@link SurfaceConfiguration} to enable chaining of set calls
     */
    public SurfaceConfiguration setMultiSampleCount(@IntRange(from = 2) int count){
        multiSampleCount = count;
        return this;
    }

    /**
     * @return
     */
    public int getMultiSampleCount() {
        return multiSampleCount;
    }

    /**
     * Enables/Disables a transparent background, overriding any layout attribute. Ignored for surfaces based on
     * {@link android.view.TextureView}.  If enabled, all color components are 8 bits regardless of any values
     * specified elsewhere in this SurfaceConfiguration.
     *
     * @param isTransparent {@code boolean} If true, this {@link SurfaceConfiguration} will be drawn with a transparent
     *                      background. Default is false.
     * @return this {@link SurfaceConfiguration} to enable chaining of set calls
     */
    public SurfaceConfiguration setTransparent(boolean isTransparent) {
        this.isTransparent = isTransparent;
        return this;
    }

    /**
     * @return
     */
    public boolean isTransparent() {
        return isTransparent;
    }


    /**
     * Sets the bit depth for each color/alpha component; ignored if transparent
     *
     * @param bitsRed
     * @param bitsGreen
     * @param bitsBlue
     * @param bitsAlpha
     * @return this {@link SurfaceConfiguration} to enable chaining of set calls
     */
    public SurfaceConfiguration setColorBits(@IntRange(from = 0, to = 8) int bitsRed, @IntRange(from = 0, to = 8) int bitsGreen, @IntRange(from = 0, to = 8) int bitsBlue, @IntRange(from = 0, to = 8) int bitsAlpha) {
        this.bitsRed = bitsRed;
        this.bitsGreen = bitsGreen;
        this.bitsBlue = bitsBlue;
        this.bitsAlpha = bitsAlpha;
        return this;
    }

    /**
     * Sets the red color component bits; ignored if transparent
     *
     * @param bitsRed
     * @return
     */
    public SurfaceConfiguration setBitsRed(@IntRange(from = 0, to = 8) int bitsRed) {
        this.bitsRed = bitsRed;
        return this;
    }

    public int getBitsRed() {
        return bitsRed;
    }

    /**
     * Sets the green color component bits; ignored if transparent
     *
     * @param bitsGreen
     * @return
     */
    public SurfaceConfiguration setBitsGreen(@IntRange(from = 0, to = 8) int bitsGreen) {
       this.bitsGreen = bitsGreen;
        return this;
    }

    public int getBitsGreen() {
        return bitsGreen;
    }

    /**
     * Sets the blue color component bits; ignored if transparent
     *
     * @param bitsBlue
     * @return
     */
    public SurfaceConfiguration setBitsBlue(@IntRange(from = 0, to = 8) int bitsBlue) {
        this.bitsBlue = bitsBlue;
        return this;
    }

    public int getBitsBlue() {
        return bitsBlue;
    }

    /**
     * Sets the alpha color component bits; ignored if transparent
     *
     * @param bitsAlpha
     * @return
     */
    public SurfaceConfiguration setBitsAlpha(@IntRange(from = 0, to = 8) int bitsAlpha) {
        this.bitsAlpha = bitsAlpha;
        return this;
    }

    public int getBitsAlpha() {
        return bitsAlpha;
    }

    /**
     * Sets the bit depth for each color/alpha component; ignored if transparent
     *
     * @param colorBits integer array of [bitsRed, bitsGreen, bitsBlue, bitsAlpha]
     * @return this {@link SurfaceConfiguration} to enable chaining of set calls
     */
    public SurfaceConfiguration setColorBits(int[] colorBits) {
        if (colorBits.length != 4) {
            throw new AssertionError("Illegal colorBits array!");
        }
        return setColorBits(colorBits[0], colorBits[1], colorBits[2], colorBits[3]);
    }

    /**
     * @return integer array of [bitsRed, bitsGreen, bitsBlue, bitsAlpha]
     */
    public int[] getColorBits() {
        return new int[] {bitsRed, bitsGreen, bitsBlue, bitsAlpha};
    }

    // TODO Guessing on the range...
    public SurfaceConfiguration setBitsDepth(@IntRange(from = 0, to = 24) int bitsDepth) {
        this.bitsDepth = bitsDepth;
        return this;
    }

    public int getBitsDepth() {
        return bitsDepth;
    }
}
