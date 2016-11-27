package c.org.rajawali3d.gl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.opengl.GLES20;
import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.gl.Capabilities.UnsupportedCapabilityException;
import c.org.rajawali3d.gl.extensions.AMDCompressedATCTexture;
import c.org.rajawali3d.gl.extensions.EXTDebugMarker;
import c.org.rajawali3d.gl.extensions.EXTTextureFilterAnisotropic;
import c.org.rajawali3d.gl.extensions.GLExtension;
import c.org.rajawali3d.gl.extensions.OESCompressedETC1RGB8;
import c.org.rajawali3d.gl.extensions.OESTexture3D;
import c.org.rajawali3d.gl.extensions.OESTextureCompressionASTC;
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
public class CapabilitiesGLTest extends GlTestCase {

    // The following values are taken from the minimum specifications of GL ES 2.0
    // See <a href="https://www.khronos.org/opengles/sdk/docs/man/xhtml/glGet.xml>glGet</a>
    private static final int maxTextureSize               = 64;
    private static final int maxCombinedTextureImageUnits = 8;
    private static final int maxCubeMapTextureSize        = 16;
    private static final int maxFragmentUniformVectors    = 16;
    private static final int maxRenderbufferSize          = 1;
    private static final int maxTextureImageUnits         = 8;
    private static final int maxVaryingVectors            = 8;
    private static final int maxVertexAttribs             = 8;
    private static final int maxVertexTextureImageUnits   = 0;
    private static final int maxVertexUniformVectors      = 128;
    private static final int maxViewportWidth             = 0;
    private static final int maxViewportHeight            = 0;
    private static final int minAliasedLineWidth          = 1;
    private static final int maxAliasedLineWidth          = 1;
    private static final int minAliasedPointSize          = 1;
    private static final int maxAliasedPointSize          = 1;

    @Before
    public void setUp() throws Exception {
        super.setUp(CapabilitiesGLTest.class.getSimpleName());
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                Capabilities.getInstance();
                //Capabilities.checkGLVersion();
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                Capabilities.clearInstance();
                Capabilities.getInstance();
            }
        });
        super.tearDown();
    }

    @Test
    public void getInstance() throws Exception {
        final Capabilities[] output = new Capabilities[]{ null };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance();
            }
        });
        assertNotNull(output[0]);
    }

    @Test
    public void getEGLMajorVersion() throws Exception {
        final int[] output = new int[]{ -1 };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getEGLMajorVersion();
            }
        });
        assertEquals(1, output[0]);
        /*output[0] = -1;
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                Capabilities.clearInstance();
                output[0] = Capabilities.getEGLMajorVersion();
            }
        });
        assertEquals(1, output[0]);*/
    }

    @Test
    public void getEGLMinorVersion() throws Exception {
        final int[] output = new int[]{ -1 };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getEGLMinorVersion();
            }
        });
        assertTrue("Received EGL Minor Version: " + output[0], 0 <= output[0]);
        assertTrue("Received EGL Minor Version: " + output[0], 4 >= output[0]);
        /*output[0] = -1;
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                Capabilities.clearInstance();
                output[0] = Capabilities.getEGLMinorVersion();
            }
        });
        assertTrue("Received EGL Minor Version: " + output[0], 0 <= output[0]);
        assertTrue("Received EGL Minor Version: " + output[0], 4 >= output[0]);*/
    }

    @Test
    public void getGLESMajorVersion() throws Exception {
        final int[] output = new int[]{ -1 };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getGLESMajorVersion();
            }
        });
        assertTrue(2 == output[0] || 3 == output[0]);
        /*output[0] = -1;
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                Capabilities.clearInstance();
                output[0] = Capabilities.getGLESMajorVersion();
            }
        });
        assertTrue(2 == output[0] || 3 == output[0]);*/
    }

    @Test
    public void getVendor() throws Exception {
        final String[] output = new String[]{ null };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getVendor();
            }
        });
        assertNotNull(output[0]);
    }

    @Test
    public void getRenderer() throws Exception {
        final String[] output = new String[]{ null };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getRenderer();
            }
        });
        assertNotNull(output[0]);
    }

    @Test
    public void getVersion() throws Exception {
        final String[] output = new String[]{ null };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getVersion();
            }
        });
        assertNotNull(output[0]);
    }

    @Test
    public void getExtensions() throws Exception {
        final String[][] output = new String[][]{ null };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getExtensions();
            }
        });
        assertNotNull(output[0]);
    }

    @Test
    public void verifyExtension() throws Exception {
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                final String[] extensions = Capabilities.getInstance().getExtensions();
                if (extensions.length > 0) {
                    try {
                        Capabilities.getInstance().verifyExtension(extensions[0]);
                    } catch (Capabilities.UnsupportedCapabilityException e) {
                        thrown[0] = true;
                    }
                }
            }
        });
        assertFalse(thrown[0]);
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                final String[] extensions = Capabilities.getInstance().getExtensions();
                if (extensions.length > 0) {
                    try {
                        Capabilities.getInstance().verifyExtension("SOME_NON_EXISTENT_EXTENSION");
                    } catch (Capabilities.UnsupportedCapabilityException e) {
                        thrown[0] = true;
                    }
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void loadExtension() throws Exception {
        final boolean[] thrown = new boolean[]{ false };
        final StringBuilder builder = new StringBuilder();
        final GLExtension[] output = new GLExtension[]{ null };
        final boolean[] check = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                if (GLES20.glGetString(GLES20.GL_EXTENSIONS).contains(EXTDebugMarker.name)) {
                    check[0] = true;
                    try {
                        output[0] = Capabilities.getInstance().loadExtension(EXTDebugMarker.name);
                    } catch (Capabilities.UnsupportedCapabilityException e) {
                        thrown[0] = true;
                        builder.append(e.getMessage());
                    }
                }
            }
        });
        assertFalse(builder.toString(), thrown[0]);
        if (check[0]) {
            assertNotNull(output[0]);
            assertTrue(output[0] instanceof EXTDebugMarker);
        }
    }

    @Test
    public void loadAllExtensions() throws Exception {
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    Capabilities.getInstance().loadExtension(AMDCompressedATCTexture.name);
                } catch (UnsupportedCapabilityException e) {
                    e.printStackTrace();
                }
                try {
                    Capabilities.getInstance().loadExtension(OESCompressedETC1RGB8.name);
                } catch (UnsupportedCapabilityException e) {
                    e.printStackTrace();
                }
                try {
                    Capabilities.getInstance().loadExtension(OESTextureCompressionASTC.name);
                } catch (UnsupportedCapabilityException e) {
                    e.printStackTrace();
                }
                try {
                    Capabilities.getInstance().loadExtension(EXTDebugMarker.name);
                } catch (UnsupportedCapabilityException e) {
                    e.printStackTrace();
                }
                try {
                    Capabilities.getInstance().loadExtension(EXTTextureFilterAnisotropic.name);
                } catch (UnsupportedCapabilityException e) {
                    e.printStackTrace();
                }
                try {
                    Capabilities.getInstance().loadExtension(OESTexture3D.name);
                } catch (UnsupportedCapabilityException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadBadExtension() throws Exception {
        final boolean[] thrown = new boolean[]{ false, false };
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                try {
                    Capabilities.getInstance().loadExtension("NON_EXISTENT_EXTENSION");
                } catch (UnsupportedCapabilityException e) {
                    e.printStackTrace();
                    thrown[0] = true;
                } catch (IllegalArgumentException e) {
                    thrown[1] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertTrue(thrown[1]);
    }

    @Test
    public void usingExtension() throws Exception {
        final boolean[] thrown = new boolean[]{ false };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    Capabilities.getInstance().usingExtension(EXTDebugMarker.load());
                } catch (Capabilities.UnsupportedCapabilityException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
    }

    @Test
    public void getMaxTextureSize() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxTextureSize();
            }
        });
        assertTrue(maxTextureSize <= output[0]);
    }

    @Test
    public void getMaxCombinedTextureUnits() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxCombinedTextureUnits();
            }
        });
        assertTrue(maxCombinedTextureImageUnits <= output[0]);
    }

    @Test
    public void getMaxCubeMapTextureSize() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxCubeMapTextureSize();
            }
        });
        assertTrue(maxCubeMapTextureSize <= output[0]);
    }

    @Test
    public void getMaxFragmentUniformVectors() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxFragmentUniformVectors();
            }
        });
        assertTrue(maxFragmentUniformVectors <= output[0]);
    }

    @Test
    public void getMaxRenderbufferSize() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxRenderbufferSize();
            }
        });
        assertTrue(maxRenderbufferSize <= output[0]);
    }

    @Test
    public void getMaxTextureImageUnits() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxTextureImageUnits();
            }
        });
        assertTrue(maxTextureImageUnits <= output[0]);
    }

    @Test
    public void getMaxVaryingVectors() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxVaryingVectors();
            }
        });
        assertTrue(maxVaryingVectors <= output[0]);
    }

    @Test
    public void getMaxVertexAttribs() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxVertexAttribs();
            }
        });
        assertTrue(maxVertexAttribs <= output[0]);
    }

    @Test
    public void getMaxVertexTextureImageUnits() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxVertexTextureImageUnits();
            }
        });
        assertTrue(maxVertexTextureImageUnits <= output[0]);
    }

    @Test
    public void getMaxVertexUniformVectors() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxVertexUniformVectors();
            }
        });
        assertTrue(maxVertexUniformVectors <= output[0]);
    }

    @Test
    public void getMaxViewportWidth() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxViewportWidth();
            }
        });
        assertTrue(maxViewportWidth <= output[0]);
    }

    @Test
    public void getMaxViewportHeight() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxViewportHeight();
            }
        });
        assertTrue(maxViewportHeight <= output[0]);
    }

    @Test
    public void getMinAliasedLineWidth() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMinAliasedLineWidth();
            }
        });
        assertTrue(minAliasedLineWidth >= output[0]);
    }

    @Test
    public void getMaxAliasedLineWidth() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxAliasedLineWidth();
            }
        });
        assertTrue(maxAliasedLineWidth <= output[0]);
    }

    @Test
    public void getMinAliasedPointSize() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMinAliasedPointSize();
            }
        });
        assertTrue(minAliasedPointSize >= output[0]);
    }

    @Test
    public void getMaxAliasedPointSize() throws Exception {
        final int[] output = new int[]{ Integer.MIN_VALUE };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxAliasedPointSize();
            }
        });
        assertTrue(maxAliasedPointSize <= output[0]);
    }

    @Test
    public void testToString() throws Exception {
        final String[] output = new String[]{ null };
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().toString();
            }
        });
        assertNotNull(output[0]);
        assertFalse(output[0].isEmpty());
    }
}