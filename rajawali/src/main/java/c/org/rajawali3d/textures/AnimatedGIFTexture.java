/**
 * Copyright 2013 Dennis Ippel
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package c.org.rajawali3d.textures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import org.rajawali3d.util.RajLog;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import c.org.rajawali3d.annotations.GLThread;
import c.org.rajawali3d.textures.annotation.Type;
import c.org.rajawali3d.util.ByteBufferBackedInputStream;


/**
 * Creates a texture from an animated GIF.
 *
 * @author dennis.ippel
 *
 */
@SuppressWarnings("WeakerAccess")
public class AnimatedGIFTexture extends SingleTexture2D {

    private Canvas canvas;
    private Movie movie;
    private Bitmap bitmap;
    private Bitmap gifBitmap;
    private int width;
    private int height;
    private long startTime;

    @FloatRange(from = 0) private float scaleFactor = 1.0f;

    /**
     * Constructs a new {@link AnimatedGIFTexture} from the provided {@link AnimatedGIFTexture}.
     *
     * @param other {@link AnimatedGIFTexture} The source texture.
     *
     * @throws TextureException thrown if there is an error copying the texture.
     */
    public AnimatedGIFTexture(@NonNull AnimatedGIFTexture other) throws TextureException {
        super(other);
    }

    /**
     * Constructs a new {@link AnimatedGIFTexture} with the specified name and type.
     *
     * @param type {@link Type.TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     */
    public AnimatedGIFTexture(@Type.TextureType int type, @NonNull String name) {
        super(type, name);
    }

    /**
     * Constructs a new {@link AnimatedGIFTexture} with data provided by the Android resource id. The texture name is
     * set by querying Android for the resource name.
     *
     * @param context    {@link Context} The application context.
     * @param type       {@link Type.TextureType} The texture usage type.
     * @param resourceId {@code int} The Android resource id to load from.
     *
     * @throws TextureException thrown if there is an error loading the GIF texture.
     */
    public AnimatedGIFTexture(@NonNull Context context, @Type.TextureType int type, @DrawableRes int resourceId)
        throws TextureException {
        this(type, context.getResources().getResourceName(resourceId));
        setTextureDataFromResourceId(context, resourceId);
    }

    /**
     * Constructs a new {@link AnimatedGIFTexture} with the provided data.
     *
     * @param type {@link Type.TextureType} The texture usage type.
     * @param name {@link String} The texture name.
     * @param data {@link TextureDataReference} The texture data.
     *
     * @throws TextureException thrown if there is an error loading the GIF texture.
     */
    public AnimatedGIFTexture(@Type.TextureType int type, @NonNull String name, @NonNull TextureDataReference data)
        throws TextureException {
        this(type, name);
        setTextureData(data);
        loadGIF();
    }

    /**
     * Copies all properties and data from another {@link AnimatedGIFTexture}.
     *
     * @param other The other {@link AnimatedGIFTexture}.
     *
     * @throws TextureException Thrown if an error occurs during any part of the texture copy process.
     */
    public void setFrom(@NonNull AnimatedGIFTexture other) throws TextureException {
        super.setFrom(other);
        gifBitmap = other.getGifBitmap();
        canvas = other.getCanvas();
        movie = other.getMovie();
        width = other.getWidth();
        height = other.getHeight();
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public AnimatedGIFTexture clone() {
        try {
            return new AnimatedGIFTexture(this);
        } catch (TextureException e) {
            RajLog.e(e.getMessage());
            return null;
        }
    }

    @NonNull
    @Override
    public TextureDataReference setTextureDataFromResourceId(@NonNull Context context,
                                                             @RawRes @DrawableRes int resourceId)
        throws TextureException {
        return loadGIF(context, resourceId);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Bitmap getGifBitmap() {
        return gifBitmap;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Movie getMovie() {
        return movie;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void rewind() {
        startTime = SystemClock.uptimeMillis();
    }

    public void setScaleFactor(@FloatRange(from = 0) float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void update() throws TextureException {
        if (movie == null || movie.duration() == 0) return;
        long now = SystemClock.uptimeMillis();
        int relTime = (int) ((now - startTime) % movie.duration());
        movie.setTime(relTime);
        gifBitmap.eraseColor(Color.TRANSPARENT);
        movie.draw(canvas, 0, 0);
        bitmap = Bitmap.createScaledBitmap(gifBitmap, (int) (scaleFactor * width), (int) (scaleFactor * height), false);
        super.replace();
    }

    @GLThread
    @Override
    void remove() throws TextureException {
        if (gifBitmap != null) {
            gifBitmap.recycle();
            gifBitmap = null;
        }

        canvas = null;
        movie = null;

        super.remove();
    }

    @GLThread
    @Override
    void reset() throws TextureException {
        super.reset();

        if (gifBitmap != null) {
            gifBitmap.recycle();
            gifBitmap = null;
        }

        canvas = null;
        movie = null;
    }

    @NonNull
    private TextureDataReference loadGIF(@NonNull Context context, @RawRes @DrawableRes int resourceId)
        throws TextureException {
        ByteBuffer buffer;
        try {
            final byte[] read = new byte[8192];
            final BufferedInputStream inputStream = new BufferedInputStream(context.getResources()
                .openRawResource(resourceId), 8192);
            buffer = ByteBuffer.allocate(inputStream.available());
            int readCount;
            while ((readCount = inputStream.read(read)) > 0) {
                buffer.put(read, 0, readCount);
            }
            buffer.compact().rewind();
        } catch (IOException e) {
            throw new TextureException("Error while reading GIF resource.", e);
        }
        movie = Movie.decodeStream(new ByteBufferBackedInputStream(buffer));
        width = movie.width();
        height = movie.height();

        gifBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        canvas = new Canvas(gifBitmap);
        movie.draw(canvas, 0, 0);
        bitmap = Bitmap.createScaledBitmap(gifBitmap, (int) (scaleFactor * width), (int) (scaleFactor * height), false);
        return new TextureDataReference(null, buffer, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, width, height);
    }

    private void loadGIF() throws TextureException {
        if (getTextureData() == null) {
            throw new TextureException("Loading GIF failed due to null texture data.");
        }
        if (!getTextureData().hasBuffer()) {
            throw new TextureException("GIF Textures require a data reference backed by a byte buffer.");
        }
        movie = Movie.decodeStream(new ByteBufferBackedInputStream(getTextureData().getByteBuffer()));
        width = movie.width();
        height = movie.height();

        gifBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        canvas = new Canvas(gifBitmap);
        movie.draw(canvas, 0, 0);
        bitmap = Bitmap.createScaledBitmap(gifBitmap, (int) (scaleFactor * width), (int) (scaleFactor * height), false);
    }
}