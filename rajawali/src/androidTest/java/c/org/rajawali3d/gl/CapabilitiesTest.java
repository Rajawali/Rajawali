package c.org.rajawali3d.gl;

import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import c.org.rajawali3d.GlTestCase;
import c.org.rajawali3d.gl.extensions.EXTTextureFilterAnisotropic;
import c.org.rajawali3d.gl.extensions.GLExtension;

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
public class CapabilitiesTest extends GlTestCase {

    private static final int maxTextureSize = 0;
    private static final int maxCombinedTextureImageUnits = 0;
    private static final int maxCubeMapTextureSize = 0;
    private static final int maxFragmentUniformVectors = 0;
    private static final int maxRenderbufferSize = 0;
    private static final int maxTextureImageUnits = 0;
    private static final int maxVaryingVectors = 0;
    private static final int maxVertexAttribs = 0;
    private static final int maxVertexTextureImageUnits = 0;
    private static final int maxVertexUniformVectors = 0;
    private static final int maxViewportWidth = 0;
    private static final int maxViewportHeight = 0;
    private static final int minAliasedLineWidth = 0;
    private static final int maxAliasedLineWidth = 0;
    private static final int minAliasedPointSize = 0;
    private static final int maxAliasedPointSize = 0;

    @Before
    public void setUp() throws Exception {
        super.setUp("CapabilitiesTest");
        Capabilities.getInstance();
        Capabilities.checkGLVersion();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void getInstance() throws Exception {
        final Capabilities[] output = new Capabilities[]{null};
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
        final int[] output = new int[]{-1};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getEGLMajorVersion();
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
                output[0] = Capabilities.getEGLMinorVersion();
            }
        });
        assertTrue(2 <= output[0]);
        assertTrue(4 >= output[1]);
    }

    @Test
    public void getGLESMajorVersion() throws Exception {
        final int[] output = new int[]{-1};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getGLESMajorVersion();
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
                output[0] = Capabilities.getInstance().getVendor();
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
                output[0] = Capabilities.getInstance().getRenderer();
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
                output[0] = Capabilities.getInstance().getVersion();
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
                output[0] = Capabilities.getInstance().getExtensions();
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
        final boolean[] thrown = new boolean[]{false};
        final GLExtension[] output = new GLExtension[]{null};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    output[0] = Capabilities.getInstance().loadExtension(EXTTextureFilterAnisotropic.name);
                } catch (Capabilities.UnsupportedCapabilityException e) {
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
                    Capabilities.getInstance().usingExtension(EXTTextureFilterAnisotropic.load());
                } catch (Capabilities.UnsupportedCapabilityException e) {
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
                output[0] = Capabilities.getInstance().getMaxTextureSize();
            }
        });
        assertEquals(maxTextureSize, output[0]);
    }

    @Test
    public void getMaxCombinedTextureUnits() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxCombinedTextureUnits();
            }
        });
        assertEquals(maxCombinedTextureImageUnits, output[0]);
    }

    @Test
    public void getMaxCubeMapTextureSize() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxCubeMapTextureSize();
            }
        });
        assertEquals(maxCubeMapTextureSize, output[0]);
    }

    @Test
    public void getMaxFragmentUniformVectors() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxFragmentUniformVectors();
            }
        });
        assertEquals(maxFragmentUniformVectors, output[0]);
    }

    @Test
    public void getMaxRenderbufferSize() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxRenderbufferSize();
            }
        });
        assertEquals(maxRenderbufferSize, output[0]);
    }

    @Test
    public void getMaxTextureImageUnits() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxTextureImageUnits();
            }
        });
        assertEquals(maxTextureImageUnits, output[0]);
    }

    @Test
    public void getMaxVaryingVectors() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxVaryingVectors();
            }
        });
        assertEquals(maxVaryingVectors, output[0]);
    }

    @Test
    public void getMaxVertexAttribs() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxVertexAttribs();
            }
        });
        assertEquals(maxVertexAttribs, output[0]);
    }

    @Test
    public void getMaxVertexTextureImageUnits() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxVertexTextureImageUnits();
            }
        });
        assertEquals(maxVertexTextureImageUnits, output[0]);
    }

    @Test
    public void getMaxVertexUniformVectors() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxVertexUniformVectors();
            }
        });
        assertEquals(maxVertexUniformVectors, output[0]);
    }

    @Test
    public void getMaxViewportWidth() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxViewportWidth();
            }
        });
        assertEquals(maxViewportWidth, output[0]);
    }

    @Test
    public void getMaxViewportHeight() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxViewportHeight();
            }
        });
        assertEquals(maxViewportHeight, output[0]);
    }

    @Test
    public void getMinAliasedLineWidth() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMinAliasedLineWidth();
            }
        });
        assertEquals(minAliasedLineWidth, output[0]);
    }

    @Test
    public void getMaxAliasedLineWidth() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxAliasedLineWidth();
            }
        });
        assertEquals(maxAliasedLineWidth, output[0]);
    }

    @Test
    public void getMinAliasedPointSize() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMinAliasedPointSize();
            }
        });
        assertEquals(minAliasedPointSize, output[0]);
    }

    @Test
    public void getMaxAliasedPointSize() throws Exception {
        final int[] output = new int[]{Integer.MIN_VALUE};
        runOnGlThreadAndWait(new Runnable() {
            @Override
            public void run() {
                output[0] = Capabilities.getInstance().getMaxAliasedPointSize();
            }
        });
        assertEquals(maxAliasedPointSize, output[0]);
    }

    @Test
    public void testToString() throws Exception {
        final String[] output = new String[]{null};
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