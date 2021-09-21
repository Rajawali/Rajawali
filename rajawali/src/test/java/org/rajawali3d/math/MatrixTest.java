package org.rajawali3d.math;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MatrixTest {
    double[] identity = {
        1,0,0,0,
        0,1,0,0,
        0,0,1,0,
        0,0,0,1,
    };

    @Test
    public void testMultiplyMM() {
        double result[] = new double[16];
        Matrix.multiplyMM(result,0,identity,0,identity,0);
        assertTrue(Arrays.equals(identity, result));
    }

    @Test
    public void testMultiplyMV() {
        double vector[] = { 1,1,1,1 };
        double result[] = new double[16];
        Matrix.multiplyMV(result,0,identity,0,vector,0);
        assertTrue(Arrays.equals(identity, result));
    }

    @Test
    public void testTransposeM() {
        double result[] = new double[16];
        Matrix.transposeM(result,0,identity,0);
        assertTrue(Arrays.equals(identity, result));
    }

    @Test
    public void testInvertM() {
        double result[] = new double[16];
        assertTrue(Matrix.invertM(result,0,identity,0));
        assertTrue(Arrays.equals(identity, result));
    }

    @Test
    public void testOrthoM() {
        double expected[] =  {
            2,  0,  0,  0, 
            0,  2,  0,  0, 
            0,  0, -0.02247191011235955, 0, 
           -1, -1, -1.0224719101123596, 1,
        };
        double result[] = new double[16];
        double l = 0;
        double r = 1;
        double b = 0;
        double t = 1;
        double n = 1;
        double f = 90;
        Matrix.orthoM(result,0,l,r,b,t,n,f);
        for(int i=0; i<expected.length; i++) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testFrustumM() {
        double expected[] = {
            2, 0, 0, 0,
            0, 2, 0, 0,
            1, 1, -1.0224719101123596, -1,
            0, 0, -2.0224719101123596, 0,
        };
        double result[] = new double[16];
        double l = 0;
        double r = 1;
        double b = 0;
        double t = 1;
        double n = 1;
        double f = 90;
        Matrix.frustumM(result,0,l,r,b,t,n,f);
        for(int i=0; i<expected.length; i++) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testPerspectiveM() {
        double expected[] = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, -1.0224719101123596, -1,
            0, 0, -2.0224719101123596, 0,
        };
        double result[] = new double[16];
        double fovy = 90;
        double aspect = 1;
        double zNear = 1;
	double zFar = 90;
        Matrix.perspectiveM(result, 0, fovy, aspect, zNear, zFar);
        for(int i=0; i<expected.length; i++) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetIdentityM() {
        double result[] = new double[16];
        Matrix.setIdentityM(result,0);
        assertTrue(Arrays.equals(identity, result));
    }

    @Test
    public void testScaleM() {
        double x = 1;
        double y = 1;
        double z = 1;
        double result[] = new double[16];
        Matrix.setIdentityM(result,0);
        Matrix.scaleM(result,0,identity,0,x,y,z);
        assertTrue(Arrays.equals(identity, result));
    }

    @Test
    public void testScaleMinPlace() {
        double x = 1;
        double y = 1;
        double z = 1;
        double result[] = new double[16];
        Matrix.setIdentityM(result,0);
        Matrix.scaleM(result,0,identity,0,x,y,z);
        assertTrue(Arrays.equals(identity, result));
    }

    @Test
    public void testTranslateM() {
        double x = 0;
        double y = 0;
        double z = 0;
        double result[] = new double[16];
        Matrix.setIdentityM(result,0);
        Matrix.translateM(result,0,identity,0,x,y,z);
        assertTrue(Arrays.equals(identity, result));
    }

    @Test
    public void testTranslateMinPlace() {
        double x = 0;
        double y = 0;
        double z = 0;
        double result[] = new double[16];
        Matrix.setIdentityM(result,0);
        Matrix.translateM(result,0,x,y,z);
        assertTrue(Arrays.equals(identity, result));
    }

    @Test
    public void testRotateM() {
        double angle = 360;
        double x = 1;
        double y = 1;
        double z = 1;
        double result[] = new double[16];
        Matrix.setIdentityM(result,0);
        Matrix.rotateM(result,0,identity,0,angle,x,y,z);
        for(int i=0; i<identity.length; i++) {
            assertEquals(identity[i], result[i], 1e-14);
        }
    }

    @Test
    public void testRotateMinPlace() {
        double angle = 360;
        double x = 1;
        double y = 1;
        double z = 1;
        double result[] = new double[16];
        Matrix.setIdentityM(result,0);
        Matrix.rotateM(result,0,angle,x,y,z);
        for(int i=0; i<identity.length; i++) {
            assertEquals(identity[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetRotateM() {
        double angle = 360;
        double x = 1;
        double y = 1;
        double z = 1;
        double result[] = new double[16];
        Matrix.setIdentityM(result,0);
        Matrix.setRotateM(result,0,angle,x,y,z);
        for(int i=0; i<identity.length; i++) {
            assertEquals(identity[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetRotateEulerM() {
        double x = 360;
        double y = 360;
        double z = 360;
        double result[] = new double[16];
        Matrix.setIdentityM(result,0);
        Matrix.setRotateEulerM(result,0,x,y,z);
        for(int i=0; i<identity.length; i++) {
            assertEquals(identity[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetLookAtM() {
        double result[] = new double[16];
        double eyeX = 1;
        double eyeY = 1;
        double eyeZ = 1;
        double centerX = 0;
        double centerY = 0;
        double centerZ = 0;
        double upX = 0;
        double upY = 0;
        double upZ = 1;
        Matrix.setIdentityM(result,0);
        Matrix.setLookAtM(result, 0,
            eyeX, eyeY, eyeZ,
            centerX, centerY, centerZ, 
            upX, upY, upZ);
        assertFalse(Arrays.equals(identity, result));
    }
}
