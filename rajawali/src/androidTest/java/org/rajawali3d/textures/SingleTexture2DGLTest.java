package org.rajawali3d.textures;

import android.support.test.filters.LargeTest;
import android.support.test.filters.RequiresDevice;
import android.support.test.runner.AndroidJUnit4;
import c.org.rajawali3d.GlTestCase;
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
public class SingleTexture2DGLTest extends GlTestCase {

    private static class TestableSingleTexture2D extends SingleTexture2D {

        @Override public SingleTexture2D clone() {
            return null;
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
    public void add() throws Exception {

    }

    @Test
    public void remove() throws Exception {

    }

    @Test
    public void replace() throws Exception {

    }

    @Test
    public void reset() throws Exception {

    }

}