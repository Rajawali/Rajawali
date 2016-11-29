package c.org.rajawali3d.gl.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.opengl.GLES20;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.textures.TextureDataReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.R;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@RequiresDevice
@LargeTest
public class OESCompressedETC1RGB8Test extends GlTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp(OESCompressedETC1RGB8Test.class.getName());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void load() throws Exception {
        final boolean[] runTest = new boolean[]{true};
        final boolean[] thrown = new boolean[]{false};
        final GLExtension[] extension = new GLExtension[1];
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    Capabilities.getInstance().verifyExtension(OESCompressedETC1RGB8.name);
                } catch (Capabilities.UnsupportedCapabilityException ignored) {
                    runTest[0] = false;
                }
                if (runTest[0]) {
                    try {
                        extension[0] = OESCompressedETC1RGB8.load();
                    } catch (Exception ignored) {
                        thrown[0] = true;
                    }
                }
            }
        });
        if (runTest[0]) {
            assertFalse(thrown[0]);
            assertNotNull(extension[0]);
            assertTrue("Loaded extension: " + extension[0], extension[0] instanceof OESCompressedETC1RGB8);
        }
    }

    @Test
    public void getName() throws Exception {
        final boolean[] runTest = new boolean[]{true};
        final boolean[] thrown = new boolean[]{false};
        final GLExtension[] extension = new GLExtension[1];
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    Capabilities.getInstance().verifyExtension(OESCompressedETC1RGB8.name);
                } catch (Capabilities.UnsupportedCapabilityException ignored) {
                    runTest[0] = false;
                }
                if (runTest[0]) {
                    try {
                        extension[0] = OESCompressedETC1RGB8.load();
                    } catch (Exception ignored) {
                        thrown[0] = true;
                    }
                }
            }
        });
        if (runTest[0]) {
            assertFalse(thrown[0]);
            assertNotNull(extension[0]);
            assertEquals(OESCompressedETC1RGB8.name, extension[0].getName());
        }
    }

    @Test
    public void createReferenceFromResourceId() throws Exception {
        final boolean[] runTest = new boolean[]{true};
        final boolean[] thrown = new boolean[]{false};
        final TextureDataReference[] references = new TextureDataReference[1];
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    Capabilities.getInstance().verifyExtension(OESCompressedETC1RGB8.name);
                } catch (Capabilities.UnsupportedCapabilityException ignored) {
                    runTest[0] = false;
                }
                if (runTest[0]) {
                    try {
                       final OESCompressedETC1RGB8 extension = OESCompressedETC1RGB8.load();
                        references[0] = extension.createReferenceFromResourceId(getContext().getResources(),
                            R.raw.rajawali_tex_mip_0)[0];
                    } catch (Exception ignored) {
                        thrown[0] = true;
                    }
                }
            }
        });
        if (runTest[0]) {
            assertFalse(thrown[0]);
            final TextureDataReference reference = references[0];
            assertNotNull(reference);
            assertTrue(reference.hasBuffer());
            assertFalse(reference.hasBitmap());
            assertFalse(reference.isDestroyed());
            assertEquals(GLES20.GL_RGB, reference.getPixelFormat());
            assertEquals(GLES20.GL_UNSIGNED_BYTE, reference.getDataType());
        }
    }

    @Test
    public void createReferenceFromResourceIds() throws Exception {
        final boolean[] runTest = new boolean[]{true};
        final boolean[] thrown = new boolean[]{false};
        final TextureDataReference[] references = new TextureDataReference[4];
        final int[] ids = new int[]{R.raw.rajawali_tex_mip_0, R.raw.rajawali_tex_mip_1,
            R.raw.rajawali_tex_mip_2, R.raw.rajawali_tex_mip_3};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    Capabilities.getInstance().verifyExtension(OESCompressedETC1RGB8.name);
                } catch (Capabilities.UnsupportedCapabilityException ignored) {
                    runTest[0] = false;
                }
                if (runTest[0]) {
                    try {
                        final OESCompressedETC1RGB8 extension = OESCompressedETC1RGB8.load();
                        final TextureDataReference[] array = extension.createReferenceFromResourceId(
                            getContext().getResources(), ids);
                        if (array.length != 4) {
                            throw new IllegalStateException("Received wrong length reference array.");
                        }
                        System.arraycopy(array, 0, references, 0, 4);
                    } catch (Exception ignored) {
                        thrown[0] = true;
                    }
                }
            }
        });
        if (runTest[0]) {
            assertFalse(thrown[0]);
            for (int i = 0; i < 4; ++i) {
                final TextureDataReference reference = references[i];
                assertNotNull(reference);
                assertTrue(reference.hasBuffer());
                assertFalse(reference.hasBitmap());
                assertFalse(reference.isDestroyed());
                assertEquals(GLES20.GL_RGB, reference.getPixelFormat());
                assertEquals(GLES20.GL_UNSIGNED_BYTE, reference.getDataType());
            }
        }
    }
}