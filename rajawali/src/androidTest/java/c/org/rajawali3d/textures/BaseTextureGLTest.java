package c.org.rajawali3d.textures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.Capabilities.UnsupportedCapabilityException;
import c.org.rajawali3d.gl.extensions.EXTTextureFilterAnisotropic;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@RequiresDevice
@LargeTest
public class BaseTextureGLTest extends GlTestCase {

    private static final class TestableBaseTexture extends BaseTexture {

        @Override public BaseTexture clone() {
            return null;
        }

        @Override void add() throws TextureException {

        }

        @Override void remove() throws TextureException {

        }

        @Override void replace() throws TextureException {

        }

        @Override void reset() throws TextureException {

        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp(getClass().getSimpleName());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void setMaxAnisotropy() throws Exception {
        final TestableBaseTexture texture = new TestableBaseTexture();
        final boolean[] thrown = new boolean[]{ false };
        final boolean[] available = new boolean[]{ true };
        final StringBuilder error = new StringBuilder();
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    texture.setMaxAnisotropy(5.0f);
                } catch (UnsupportedCapabilityException e) {
                    e.printStackTrace();
                    thrown[0] = true;
                    error.append(e.getMessage());
                }
            }
        });
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    Capabilities.getInstance().verifyExtension(EXTTextureFilterAnisotropic.name);
                } catch (UnsupportedCapabilityException ignored) {
                    available[0] = false;
                }
            }
        });

        if (available[0]) {
            assertFalse(error.toString(), thrown[0]);
            assertEquals(Float.floatToIntBits(5.0f), Float.floatToIntBits(texture.getMaxAnisotropy()));

            final float[] max = new float[]{ 1.0f };
            try {
                max[0] = ((EXTTextureFilterAnisotropic) Capabilities.getInstance().loadExtension
                        (EXTTextureFilterAnisotropic.name)).getMaxSupportedAnisotropy();
                texture.setMaxAnisotropy(20.0f);
            } catch (UnsupportedCapabilityException e) {
                e.printStackTrace();
                thrown[0] = true;
            }

            assertFalse(thrown[0]);
            assertEquals(Float.floatToIntBits(max[0]), Float.floatToIntBits(texture.getMaxAnisotropy()));
        }
    }

    @Test
    public void generateTextureId() throws Exception {
        final int[] id = new int[]{ 0 };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                id[0] = BaseTexture.generateTextureId();
            }
        });
        assertTrue(id[0] > 0);
    }
}
