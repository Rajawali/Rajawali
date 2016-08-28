package org.rajawali3d;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.rajawali3d.WorldParameters;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class WorldParametersTest {

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(new WorldParameters());
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Test
    public void testSetWorldAxes() throws Exception {
        WorldParameters.setWorldAxes(Vector3.Y, Vector3.Z, Vector3.X);
        assertEquals(WorldParameters.FORWARD_AXIS, Vector3.X);
        assertEquals(WorldParameters.NEG_FORWARD_AXIS, Vector3.NEG_X);
        assertEquals(WorldParameters.RIGHT_AXIS, Vector3.Y);
        assertEquals(WorldParameters.NEG_RIGHT_AXIS, Vector3.NEG_Y);
        assertEquals(WorldParameters.UP_AXIS, Vector3.Z);
        assertEquals(WorldParameters.NEG_UP_AXIS, Vector3.NEG_Z);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Test(expected = IllegalArgumentException.class)
    public void testSetWorldAxesNotOrthogonal() throws Exception {
        WorldParameters.setWorldAxes(Vector3.Y, Vector3.Y, Vector3.X);
    }
}