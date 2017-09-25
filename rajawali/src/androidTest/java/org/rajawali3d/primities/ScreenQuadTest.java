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
    public void testConstructor() throws Exception {
        assertNotNull(screenQuad);
    }
}
