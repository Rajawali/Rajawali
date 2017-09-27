package org.rajawali3d;

import static org.junit.Assert.*;
import org.junit.*;

import org.rajawali3d.primitives.ScreenQuad;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class ScreenQuadTest {
    ScreenQuad screenQuad;

    @Before
    public void setup() throws Exception {
        screenQuad = new ScreenQuad(false);
    }

    @After
    public void teardown() throws Exception {
        screenQuad = null;
    }

    @Test
    public void testVertices() throws Exception {
	float[] expected = new float[] {
		-0.5f, -0.5f, 0,
		-0.5f,  0.5f, 0,
		 0.5f, -0.5f, 0,
		 0.5f,  0.5f, 0,
	};
	float[] result = new float[4*3];
	screenQuad.getGeometry().getVertices().get(result);
	for(int i=0; i<result.length; i++) {
		assertEquals(expected[i], result[i], 1e-10);
	}
    }

    @Test
    public void testTextureCoords() throws Exception {
	float[] expected = new float[] {
		0, 1,
		0, 0,
		1, 1,
		1, 0,
	};
	float[] result = new float[4*2];
	screenQuad.getGeometry().getTextureCoords().get(result);
	for(int i=0; i<result.length; i++) {
		assertEquals(expected[i], result[i], 1e-10);
	}
    }
}
