package org.rajawali3d.math;

import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class MathUtilTest {

    @Test
    public void testMixFloats() {
	float x,y,a;
	x = 24f;
	y = 42f;
	a = 1f;
	assertEquals(MathUtil.mix(x,y,a), 42, 1e-14);
	x = 42f;
	y = 24f;
	a = 0f;
	assertEquals(MathUtil.mix(x,y,a), 42, 1e-14);
	x = 24f;
	y = 42f;
	a = 0.5f;
	assertEquals(MathUtil.mix(x,y,a), 33, 1e-14);
	x = 42f;
	y = 24f;
	a = 0.5f;
	assertEquals(MathUtil.mix(x,y,a), 33, 1e-14);
	x = -24f;
	y = -42f;
	a = 1f;
	assertEquals(MathUtil.mix(x,y,a), -42, 1e-14);
	x = -42f;
	y = -24f;
	a = 0f;
	assertEquals(MathUtil.mix(x,y,a), -42, 1e-14);
	x = -24f;
	y = -42f;
	a = 0.5f;
	assertEquals(MathUtil.mix(x,y,a), -33, 1e-14);
	x = -42f;
	y = -24f;
	a = 0.5f;
	assertEquals(MathUtil.mix(x,y,a), -33, 1e-14);
    }

    @Test
    public void testMixDoubles() {
    	double x,y,a;
    	x = 24;
	y = 42;
	a = 1;
	assertEquals(MathUtil.mix(x,y,a), 42, 1e-14);
	x = 42;
	y = 24;
	a = 0;
	assertEquals(MathUtil.mix(x,y,a), 42, 1e-14);
	x = 24;
	y = 42;
	a = 0.5;
	assertEquals(MathUtil.mix(x,y,a), 33, 1e-14);
	x = 42;
	y = 24;
	a = 0.5;
	assertEquals(MathUtil.mix(x,y,a), 33, 1e-14);
	x = -24f;
	y = -42f;
	a = 1f;
	assertEquals(MathUtil.mix(x,y,a), -42, 1e-14);
	x = -42f;
	y = -24f;
	a = 0f;
	assertEquals(MathUtil.mix(x,y,a), -42, 1e-14);
	x = -24;
	y = -42;
	a = 0.5;
	assertEquals(MathUtil.mix(x,y,a), -33, 1e-14);
	x = -42;
	y = -24;
	a = 0.5;
	assertEquals(MathUtil.mix(x,y,a), -33, 1e-14);
    }

}
