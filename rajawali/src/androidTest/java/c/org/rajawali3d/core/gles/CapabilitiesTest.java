package c.org.rajawali3d.engine.gles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.surface.gles.GLESCapabilities;
import c.org.rajawali3d.surface.gles.extensions.EXTTextureFilterAnisotropic;
import c.org.rajawali3d.surface.gles.extensions.GLExtension;
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
public class CapabilitiesTest extends GlTestCase {

    // The following values are taken from the minimum specifications of GL ES 2.0
    // See <a href="https://www.khronos.org/opengles/sdk/docs/man/xhtml/glGet.xml>glGet</a>
    private static final int maxTextureSize = 64;
    private static final int maxCombinedTextureImageUnits = 8;
    private static final int maxCubeMapTextureSize = 16;
    private static final int maxFragmentUniformVectors = 16;
    private static final int maxRenderbufferSize = 1;
    private static final int maxTextureImageUnits = 8;
    private static final int maxVaryingVectors = 8;
    private static final int maxVertexAttribs = 8;
    private static final int maxVertexTextureImageUnits = 0;
    private static final int maxVertexUniformVectors = 128;
    private static final int maxViewportWidth = 0;
    private static final int maxViewportHeight = 0;
    private static final int minAliasedLineWidth = 1;
    private static final int maxAliasedLineWidth = 1;
    private static final int minAliasedPointSize = 1;
    private static final int maxAliasedPointSize = 1;

    @Before
    public void setUp() throws Exception {
        super.setUp(CapabilitiesTest.class.getSimpleName());
        runOnGlThreadAndWait(new Runnable() {
            @Override public void run() {
                GLESCapabilities.getInstance();
                GLESCapabilities.checkGLVersion();
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void getInstance() throws Exception {
        final GLESCapabilities[] output = new GLESCapabilities[]{null};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance();
            }
        });
        assertNotNull(output[0]);
    }

    @Test
    public void getEGLMajorVersion() throws Exception {
        final int[] output = new int[]{-1};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getEGLMajorVersion();
            }
        });
        assertEquals(1, output[0]);
    }

    @Test
    public void getEGLMinorVersion() throws Exception {
        final int[] output = new int[]{-1};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getEGLMinorVersion();
            }
        });
        assertTrue("Received EGL Minor Version: " + output[0], 0 <= output[0]);
        assertTrue("Received EGL Minor Version: " + output[0], 4 >= output[0]);
    }

    @Test
    public void getGLESMajorVersion() throws Exception {
        final int[] output = new int[]{-1};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getGLESMajorVersion();
            }
        });
        assertTrue(2 == output[0] || 3 == output[0]);
    }

    @Test
    public void getVendor() throws Exception {
        final String[] output = new String[]{null};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getVendor();
            }
        });
        assertNotNull(output[0]);
    }

    @Test
    public void getRenderer() throws Exception {
        final String[] output = new String[]{null};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getRenderer();
            }
        });
        assertNotNull(output[0]);
    }

    @Test
    public void getVersion() throws Exception {
        final String[] output = new String[]{null};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getVersion();
            }
        });
        assertNotNull(output[0]);
    }

    @Test
    public void getExtensions() throws Exception {
        final String[][] output = new String[][]{null};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getExtensions();
            }
        });
        assertNotNull(output[0]);
    }

    @Test
    public void verifyExtension() throws Exception {
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                final String[] extensions = GLESCapabilities.getInstance().getExtensions();
                if (extensions.length > 0) {
                    try {
                        GLESCapabilities.getInstance().verifyExtension(extensions[0]);
                    } catch (GLESCapabilities.UnsupportedCapabilityException e) {
                        thrown[0] = true;
                    }
                }
            }
        });
        assertFalse(thrown[0]);
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                final String[] extensions = GLESCapabilities.getInstance().getExtensions();
                if (extensions.length > 0) {
                    try {
                        GLESCapabilities.getInstance().verifyExtension("SOME_NON_EXISTENT_EXTENSION");
                    } catch (GLESCapabilities.UnsupportedCapabilityException e) {
                        thrown[0] = true;
                    }
                }
            }
        });
        assertTrue(thrown[0]);
    }

    @Test
    public void loadExtension() throws Exception {
        final boolean[] thrown = new boolean[]{false};
        final GLExtension[] output = new GLExtension[]{null};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    output[0] = GLESCapabilities.getInstance().loadExtension(EXTTextureFilterAnisotropic.name);
                } catch (GLESCapabilities.UnsupportedCapabilityException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
        assertNotNull(output[0]);
        assertTrue(output[0] instanceof EXTTextureFilterAnisotropic);
    }

    @Test
    public void usingExtension() throws Exception {
        final boolean[] thrown = new boolean[]{false};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    GLESCapabilities.getInstance().usingExtension(EXTTextureFilterAnisotropic.load());
                } catch (GLESCapabilities.UnsupportedCapabilityException e) {
                    thrown[0] = true;
                }
            }
        });
        assertFalse(thrown[0]);
    }

    @Test
    public void getMaxTextureSize() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxTextureSize();
            }
        });
        assertTrue(maxTextureSize <= output[0]);
    }

    @Test
    public void getMaxCombinedTextureUnits() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxCombinedTextureUnits();
            }
        });
        assertTrue(maxCombinedTextureImageUnits <= output[0]);
    }

    @Test
    public void getMaxCubeMapTextureSize() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxCubeMapTextureSize();
            }
        });
        assertTrue(maxCubeMapTextureSize <= output[0]);
    }

    @Test
    public void getMaxFragmentUniformVectors() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxFragmentUniformVectors();
            }
        });
        assertTrue(maxFragmentUniformVectors <= output[0]);
    }

    @Test
    public void getMaxRenderbufferSize() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxRenderbufferSize();
            }
        });
        assertTrue(maxRenderbufferSize <= output[0]);
    }

    @Test
    public void getMaxTextureImageUnits() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxTextureImageUnits();
            }
        });
        assertTrue(maxTextureImageUnits <= output[0]);
    }

    @Test
    public void getMaxVaryingVectors() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxVaryingVectors();
            }
        });
        assertTrue(maxVaryingVectors <= output[0]);
    }

    @Test
    public void getMaxVertexAttribs() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxVertexAttribs();
            }
        });
        assertTrue(maxVertexAttribs <= output[0]);
    }

    @Test
    public void getMaxVertexTextureImageUnits() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxVertexTextureImageUnits();
            }
        });
        assertTrue(maxVertexTextureImageUnits <= output[0]);
    }

    @Test
    public void getMaxVertexUniformVectors() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxVertexUniformVectors();
            }
        });
        assertTrue(maxVertexUniformVectors <= output[0]);
    }

    @Test
    public void getMaxViewportWidth() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxViewportWidth();
            }
        });
        assertTrue(maxViewportWidth <= output[0]);
    }

    @Test
    public void getMaxViewportHeight() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxViewportHeight();
            }
        });
        assertTrue(maxViewportHeight <= output[0]);
    }

    @Test
    public void getMinAliasedLineWidth() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMinAliasedLineWidth();
            }
        });
        assertTrue(minAliasedLineWidth >= output[0]);
    }

    @Test
    public void getMaxAliasedLineWidth() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxAliasedLineWidth();
            }
        });
        assertTrue(maxAliasedLineWidth <= output[0]);
    }

    @Test
    public void getMinAliasedPointSize() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMinAliasedPointSize();
            }
        });
        assertTrue(minAliasedPointSize >= output[0]);
    }

    @Test
    public void getMaxAliasedPointSize() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().getMaxAliasedPointSize();
            }
        });
        assertTrue(maxAliasedPointSize <= output[0]);
    }

    @Test
    public void testToString() throws Exception {
        final String[] output = new String[]{null};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = GLESCapabilities.getInstance().toString();
            }
        });
        assertNotNull(output[0]);
        assertFalse(output[0].isEmpty());
    }
}
