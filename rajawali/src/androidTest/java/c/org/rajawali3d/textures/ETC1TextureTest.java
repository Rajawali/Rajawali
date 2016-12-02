package c.org.rajawali3d.textures;

import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rajawali3d.R;

import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.gl.extensions.OESCompressedETC1RGB8;
import c.org.rajawali3d.textures.annotation.Type;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@RequiresDevice
@LargeTest
public class ETC1TextureTest extends GlTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp(ETC1TextureTest.class.getName());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void constructorTypeName() throws Exception {
        final ETC1Texture texture = new ETC1Texture(Type.DIFFUSE, "TEST");
        assertNotNull(texture);
        assertEquals(Type.DIFFUSE, texture.getTextureType());
        assertEquals("TEST", texture.getTextureName());
    }

    @Test
    public void constructorOneId() throws Exception {
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
            final ETC1Texture texture = new ETC1Texture(Type.DIFFUSE, "TEST", reference);
            assertNotNull(texture);
            assertEquals(Type.DIFFUSE, texture.getTextureType());
            assertEquals("TEST", texture.getTextureName());
            assertNotNull(texture.getTextureData());
            assertEquals(1, texture.getTextureData().length);
            assertEquals(reference, texture.getTextureData()[0]);
        }
    }

    @Test
    public void constructorMultipleIds() throws Exception {
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
            final ETC1Texture texture = new ETC1Texture(Type.DIFFUSE, "TEST", references);
            assertNotNull(texture);
            assertEquals(Type.DIFFUSE, texture.getTextureType());
            assertEquals("TEST", texture.getTextureName());
            assertArrayEquals(references, texture.getTextureData());
        }
    }

    @Test
    public void constructorOther() throws Exception {
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
            final ETC1Texture texture = new ETC1Texture(Type.DIFFUSE, "TEST", references);
            final ETC1Texture other = new ETC1Texture(texture);
            assertNotNull(other);
            assertEquals(Type.DIFFUSE, other.getTextureType());
            assertEquals("TEST", other.getTextureName());
            assertArrayEquals(texture.getTextureData(), other.getTextureData());
        }
    }

    @Test
    public void setFrom() throws Exception {
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
            final ETC1Texture texture = new ETC1Texture(Type.DIFFUSE, "TEST", references);
            final ETC1Texture other = new ETC1Texture(Type.NORMAL, "TEST2");
            other.setFrom(texture);
            assertEquals(Type.DIFFUSE, other.getTextureType());
            assertEquals("TEST", other.getTextureName());
            assertArrayEquals(texture.getTextureData(), other.getTextureData());
        }
    }

    @Test
    public void testClone() throws Exception {
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
            final ETC1Texture texture = new ETC1Texture(Type.DIFFUSE, "TEST", references);
            final ETC1Texture other = texture.clone();
            assertNotNull(other);
            assertEquals(Type.DIFFUSE, other.getTextureType());
            assertEquals("TEST", other.getTextureName());
            assertArrayEquals(texture.getTextureData(), other.getTextureData());
        }
    }

    @Test
    public void add() throws Exception {
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
            final ETC1Texture texture = new ETC1Texture(Type.DIFFUSE, "TEST", references);
            runOnGlThreadAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        texture.add();
                    } catch (TextureException e) {
                        e.printStackTrace();
                        thrown[0] = true;
                    }
                }
            });
            assertFalse(thrown[0]);
            assertTrue(texture.getTextureId() > -1);
        }
    }
}