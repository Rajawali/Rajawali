package org.rajawali3d.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.test.suitebuilder.annotation.SmallTest;
import org.rajawali3d.geometry.Geometry.BufferType;
import org.junit.Test;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class GeometryTest {

    @Test
    public void testBufferTypeValues() {
        final BufferType[] values = BufferType.values();
        assertNotNull(values);
        assertTrue(values.length == 4);
    }

    @Test
    public void testBufferTypeValueOf() {
        final BufferType floatType = BufferType.valueOf("FLOAT_BUFFER");
        final BufferType intType = BufferType.valueOf("INT_BUFFER");
        final BufferType shortType = BufferType.valueOf("SHORT_BUFFER");
        final BufferType byteType = BufferType.valueOf("BYTE_BUFFER");
        assertNotNull(floatType);
        assertNotNull(intType);
        assertNotNull(shortType);
        assertNotNull(byteType);
        assertEquals(BufferType.FLOAT_BUFFER, floatType);
        assertEquals(BufferType.INT_BUFFER, intType);
        assertEquals(BufferType.SHORT_BUFFER, shortType);
        assertEquals(BufferType.BYTE_BUFFER, byteType);
    }
}