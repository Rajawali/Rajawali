package org.rajawali3d;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class Object3Dtest {

    private Object3D obj;

    @Before
    public void setup() {
        obj = new Object3D();
    }

    @After
    public void teardown() {
        obj = null;
    }

    @Test
    public void testConstructor() {
        assertNotNull(obj);
    }

    @Test
    public void testGetBoundingBox() {
        assertFalse(obj.hasBoundingVolume());
        assertNotNull(obj.getBoundingBox());
        assertNotNull(obj.getTransformedBoundingVolume());
        assertTrue(obj.hasBoundingVolume());
    }
}
