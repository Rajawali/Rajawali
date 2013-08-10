/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali.math;

import rajawali.math.vector.Vector3;
import rajawali.util.ArrayUtils;

/**
 * Encapsulates a column major 4x4 Matrix.
 * 
 * Rewritten August 8, 2013 by Jared Woolston with heavy influence from libGDX
 * @see https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Matrix4.java
 * 
 * @author dennis.ippel
 * @author Jared Woolston (jwoolston@tenkiv.com)
 *
 */
public final class Matrix4 {
	
	//Matrix indices as column major notation (Column x Row)
	public static final int M00 = 0;  // 0;
	public static final int M01 = 4;  // 1;
	public static final int M02 = 8;  // 2;
	public static final int M03 = 12; // 3;
	public static final int M10 = 1;  // 4;
	public static final int M11 = 5;  // 5;
	public static final int M12 = 9;  // 6;
	public static final int M13 = 13; // 7;
	public static final int M20 = 2;  // 8;
	public static final int M21 = 6;  // 9;
	public static final int M22 = 10; // 10;
	public static final int M23 = 14; // 11;
	public static final int M30 = 3;  // 12;
	public static final int M31 = 7;  // 13;
	public static final int M32 = 11; // 14;
	public static final int M33 = 15; // 15;
	    
	private double[] m = new double[16]; //The matrix values
	private double[] mTmp = new double[16]; //A scratch matrix 
	private float[] mFloat = new float[16]; //A float copy of the values, used for sending to GL.
	
	//--------------------------------------------------
	// Constructors
	//--------------------------------------------------
	
	/**
	 * Constructs a default identity {@link Matrix4}.
	 */
	public Matrix4() {
		identity();
	}
	
	/**
	 * Constructs a new {@link Matrix4} based on the given matrix.
	 * 
	 * @param matrix {@link Matrix4} The matrix to clone.
	 */
	public Matrix4(final Matrix4 matrix) {
		setAll(matrix);
	}
	
	/**
	 * Constructs a new {@link Matrix4} based on the provided double array. The array length
	 * must be greater than or equal to 16 and the array will be copied from the 0 index.
	 * 
	 * @param matrix double array containing the values for the matrix in column major order.
	 * The array is not modified or referenced after this constructor completes.
	 */
	public Matrix4(double[] matrix) {
		setAll(matrix);
	}
	
	/**
	 * Constructs a new {@link Matrix4} based on the provided float array. The array length
	 * must be greater than or equal to 16 and the array will be copied from the 0 index.
	 * 
	 * @param matrix float array containing the values for the matrix in column major order.
	 * The array is not modified or referenced after this constructor completes.
	 */
	public Matrix4(float[] matrix) {
		this(ArrayUtils.convertFloatsToDoubles(matrix));
	}
	
	/**
	 * Constructs a {@link Matrix4} based on the rotation represented by the provided {@link Quaternion}.
	 * 
	 * @param quat {@link Quaternion} The {@link Quaternion} to be copied.
	 */
	public Matrix4(final Quaternion quat) {
		setAll(quat);
	}
	
	public Matrix4(double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33) {
		setAll(m00, m01, m02, m03,
	            m10, m11, m12, m13,
	            m20, m21, m22, m23,
	            m30, m31, m32, m33);
	}
	
	
	
	//--------------------------------------------------
	// Modification methods
	//--------------------------------------------------
	
	/**
	 * Sets the elements of this {@link Matrix4} based on the elements of the provided {@link Matrix4}.
	 * 
	 * @param matrix {@link Matrix4} to copy.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setAll(final Matrix4 matrix) {
		matrix.toArray(m);
		return this;
	}

	/**
	 * Sets the elements of this {@link Matrix4} based on the provided double array.
	 * The array length must be greater than or equal to 16 and the array will be copied 
	 * from the 0 index.
	 * 
	 * @param matrix double array containing the values for the matrix in column major order.
	 * The array is not modified or referenced after this constructor completes.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setAll(double[] matrix) {
		System.arraycopy(matrix, 0, m, 0, 16);
		return this;
	}
	
	/**
	 * Sets the elements of this {@link Matrix4} based on the rotation represented by
	 * the provided {@link Quaternion}. 
	 * 
	 * @param quat {@link Quaternion} The {@link Quaternion} to represent.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setAll(final Quaternion quat) {
		quat.toRotationMatrix(m);
		return this;
	}
	
	/**
	 * Sets the elements of this {@link Matrix4} based on the rotation represented by
	 * the provided quaternion elements. 
	 * 
	 * @param w double The w component of the quaternion.
	 * @param x double The x component of the quaternion.
	 * @param y double The y component of the quaternion.
	 * @param z double The z component of the quaternion.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setAll(double w, double x, double y, double z) {
		final double xx = x * x;
		final double xy = x * y;
		final double xz = x * z;
		final double xw = x * w;
		final double yy = y * y;
		final double yz = y * z;
		final double yw = y * w;
		final double zz = z * z;
		final double zw = z * w;
		//Set matrix from quaternion
		m[M00] = 1 - 2 * (yy + zz);
		m[M01] = 2 * (xy - zw);
		m[M02] = 2 * (xz + yw);
		m[M03] = 0;
		m[M10] = 2 * (xy + zw);
		m[M11] = 1 - 2 * (xx + zz);
		m[M12] = 2 * (yz - xw);
		m[M12] = 0;
		m[M20] = 2 * (xz - yw);
		m[M21] = 2 * (yz + xw);
		m[M22] = 1 - 2 * (xx + yy);
		m[M23] = 0;
		m[M30] = 0;
		m[M31] = 0;
		m[M32] = 0;
		m[M33] = 1;
		return this;
	}
	
	/**
	 * Sets the four columns of this {@link Matrix4} which correspond to the x-, y-, and z- 
	 * axis of the vector space this {@link Matrix4} creates as well as the 4th column representing
	 * the translation of any point that is multiplied by this {@link Matrix4}.
	 * 
	 * @param xAxis {@link Vector3} The x axis.
	 * @param yAxis {@link Vector3} The y axis.
	 * @param zAxis {@link Vector3} The z axis.
	 * @param pos {@link Vector3} The translation vector.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setAll(final Vector3 xAxis, final Vector3 yAxis, final Vector3 zAxis, final Vector3 pos) {
		m[M00] = xAxis.x; m[M01] = xAxis.y; m[M02] = xAxis.z;
		m[M10] = yAxis.x; m[M11] = yAxis.y; m[M12] = yAxis.z;
		m[M20] = -zAxis.x; m[M21] = -zAxis.y; m[M22] = -zAxis.z;
		m[M03] = pos.x; m[M13] = pos.y; m[M23] = pos.z;
		m[M30] = 0; m[M31] = 0; m[M32] = 0; m[M33] = 1;
		return this;
	}

	public void setAll(double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33) {
		m[0] = m00;		m[1] = m01;		m[2] = m02;		m[3] = m03;
		m[4] = m10;		m[5] = m11;		m[6] = m12;		m[7] = m13;
		m[8] = m20;		m[9] = m21;		m[10] = m22;	m[11] = m23;
		m[12] = m30;	m[13] = m31;	m[14] = m32;	m[15] = m33;
	}
	
	/**
	 * Sets this {@link Matrix4} to an identity matrix.
	 * 
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 identity() {
		m[M00] = 1;	m[M10] = 0;	m[M20] = 0;	m[M30] = 0;
		m[M01] = 0;	m[M11] = 1;	m[M21] = 0;	m[M31] = 0;
		m[M02] = 0;	m[M12] = 0;	m[M22] = 1;	m[M32] = 0;
		m[M03] = 0;	m[M13] = 0;	m[M23] = 0;	m[M33] = 1;
		return this;
	}
	
	/**
	 * Sets all elements of this {@link Matrix4} to zero.
	 * 
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 zero() {
		for (int i=0; i<16; ++i) {
			m[i] = 0;
		}
		return this;
	}
	
	/**
	 * Calculate the determinant of this {@link Matrix4}.
	 * 
	 * @return double The determinant.
	 */
	public double determinant() {
		return (
			m[M30] * m[M21] * m[M12] * m[M03]-
			m[M20] * m[M31] * m[M12] * m[M03]-
			m[M30] * m[M11] * m[M22] * m[M03]+
			m[M10] * m[M31] * m[M22] * m[M03]+

			m[M20] * m[M11] * m[M32] * m[M03]-
			m[M10] * m[M21] * m[M32] * m[M03]-
			m[M30] * m[M21] * m[M02] * m[M13]+
			m[M20] * m[M31] * m[M02] * m[M13]+

			m[M30] * m[M01] * m[M22] * m[M13]-
			m[M00] * m[M31] * m[M22] * m[M13]-
			m[M20] * m[M01] * m[M32] * m[M13]+
			m[M00] * m[M21] * m[M32] * m[M13]+

			m[M30] * m[M11] * m[M02] * m[M23]-
			m[M10] * m[M31] * m[M02] * m[M23]-
			m[M30] * m[M01] * m[M12] * m[M23]+
			m[M00] * m[M31] * m[M12] * m[M23]+

			m[M10] * m[M01] * m[M32] * m[M23]-
			m[M00] * m[M11] * m[M32] * m[M23]-
			m[M20] * m[M11] * m[M02] * m[M33]+
			m[M10] * m[M21] * m[M02] * m[M33]+

			m[M20] * m[M01] * m[M12] * m[M33]-
			m[M00] * m[M21] * m[M12] * m[M33]-
			m[M10] * m[M01] * m[M22] * m[M33]+
			m[M00] * m[M11] * m[M22] * m[M33]
		);
	}
	
	/**
	 * Inverts this {@link Matrix4}.
	 * 
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 inverse() {
        boolean success = Matrix.invertM(mTmp, 0, m, 0);
        System.arraycopy(mTmp, 0, m, 0, 16);
        if (!success) throw new IllegalStateException("This is a non-invertable matrix");
        return this;
    }
	
	/**
	 * Transposes this {@link Matrix4}.
	 * 
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 transpose() {
		Matrix.transposeM(mTmp, 0, m, 0);
		System.arraycopy(mTmp, 0, m, 0, 16);
		return this;
    }
	
	/**
	 * Multiplies this {@link Matrix4} with the given one, storing the result in this {@link Matrix}.
	 * <pre>
	 * A.multiply(B) results in A = AB.
	 * </pre>
	 * 
	 * @param matrix {@link Matrix4} The RHS {@link Matrix4}.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 multiply(final Matrix4 matrix) {
		matrix.toArray(mTmp);
		Matrix.multiplyMM(mTmp, 0, m, 0, matrix.getDoubleValues(), 0);
		return this;
    }
	
	public Vector3 multiply(final Vector3 v) {
		 Vector3 r = new Vector3();

         double inv = 1.0f / ( m[12] * v.x + m[13] * v.y + m[14] * v.z + m[15] );

         r.x = ( m[0] * v.x + m[1] * v.y + m[2] * v.z + m[3] ) * inv;
         r.y = ( m[4] * v.x + m[5] * v.y + m[6] * v.z + m[7] ) * inv;
         r.z = ( m[8] * v.x + m[8] * v.y + m[10] * v.z + m[11] ) * inv;

         return r;
	}
	
	public Matrix4 multiply(final double value) {
		return new Matrix4(
	            value*m[0], value*m[1], value*m[2], value*m[3],
	            value*m[4], value*m[5], value*m[6], value*m[7],
	            value*m[8], value*m[9], value*m[10], value*m[11],
	            value*m[12], value*m[13], value*m[14], value*m[15]);
	}
	
	/**
	 * Adds a translation to this {@link Matrix4} based on the provided {@link Vector3}.
	 * 
	 * @param vec {@link Vector3} describing the translation components.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 translate(final Vector3 vec) {
		m[M03] += vec.x;
		m[M13] += vec.y;
		m[M23] += vec.z;
		return this;
	}
	
	/**
	 * Adds a translation to this {@link Matrix4} based on the provided components.
	 * 
	 * @param x double The x component of the translation.
	 * @param y double The y component of the translation.
	 * @param z double The z component of the translation.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 translate(double x, double y, double z) {
		m[M03] += x;
		m[M13] += y;
		m[M23] += z;
		return this;
	}
	
	/**
	 * Scales this {@link Matrix4} based on the provided components.
	 * 
	 * @param vec {@link Vector3} describing the scaling on each axis.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 scale(final Vector3 vec) {
		m[M00] *= vec.x;
		m[M11] *= vec.y;
		m[M22] *= vec.z;
		return this;
	}
	
	/**
	 * Scales this {@link Matrix4} based on the provided components.
	 * 
	 * @param x double The x component of the scaling.
	 * @param y double The y component of the scaling.
	 * @param z double The z component of the scaling.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 scale(double x, double y, double z) {
		m[M00] *= x;
		m[M11] *= y;
		m[M22] *= z;
		return this;
	}
	
	/**
	 * Scales this {@link Matrix4} along all three axis by the provided value.
	 * 
	 * @param s double The scaling factor.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 scale(double s) {
		m[M00] *= s;
		m[M11] *= s;
		m[M22] *= s;
		return this;
	}
	
	/**
	 * Sets the translation of this {@link Matrix4} based on the provided {@link Vector3}.
	 * 
	 * @param vec {@link Vector3} describing the translation components.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setTranslation(final Vector3 vec) {
		m[M03] = vec.x;
		m[M13] = vec.y;
		m[M23] = vec.z;
		return this;
	}
	
	/**
	 * Sets translation of this {@link Matrix4} based on the provided components.
	 * 
	 * @param x double The x component of the translation.
	 * @param y double The y component of the translation.
	 * @param z double The z component of the translation.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setTranslation(double x, double y, double z) {
		m[M03] = x;
		m[M13] = y;
		m[M23] = z;
		return this;
	}
	
	/**
	 * Sets this {@link Matrix4} to a translation matrix based on the provided {@link Vector3}.
	 * 
	 * @param vec {@link Vector3} describing the translation components.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setToTranslation(final Vector3 vec) {
		identity();
		m[M03] = vec.x;
		m[M13] = vec.y;
		m[M23] = vec.z;
		return this;
	}
	
	/**
	 * Sets this {@link Matrix4} to a translation matrix based on the provided components.
	 * 
	 * @param x double The x component of the translation.
	 * @param y double The y component of the translation.
	 * @param z double The z component of the translation.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setToTranslation(double x, double y, double z) {
		identity();
		m[M03] = x;
		m[M13] = y;
		m[M23] = z;
		return this;
	}
	
	/**
	 * Sets this {@link Matrix4} to a scale matrix based on the provided {@link Vector3}.
	 * 
	 * @param vec {@link Vector3} describing the scaling components.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setToScale(final Vector3 vec) {
		identity();
        m[M00] = vec.x;
        m[M11] = vec.y;
        m[M22] = vec.z;
        return this;
    }
	
	/**
	 * Sets this {@link Matrix4} to a scale matrix based on the provided components.
	 * 
	 * @param x double The x component of the translation.
	 * @param y double The y component of the translation.
	 * @param z double The z component of the translation.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setToScale(double x, double y, double z) {
		identity();
        m[M00] = x;
        m[M11] = y;
        m[M22] = z;
        return this;
    }
	
	/**
	 * Sets this {@link Matrix4} to a translation and scaling matrix.
	 * 
	 * @param translation {@link Vector3} specifying the translation components.
	 * @param scaling {@link Vector3} specifying the scaling components.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setToTranslationAndScaling(Vector3 translation, Vector3 scaling) {
		identity();
		m[M03] = translation.x;
		m[M13] = translation.y;
		m[M23] = translation.z;
		m[M00] = scaling.x;
		m[M11] = scaling.y;
		m[M22] = scaling.z;
		return this;
	}
	
	/**
	 * Sets this {@link Matrix4} to a translation and scaling matrix.
	 * 
	 * @param tx double The x component of the translation.
	 * @param ty double The y component of the translation.
	 * @param tz double The z component of the translation.
	 * @param sx double The x component of the scaling.
	 * @param sy double The y component of the scaling.
	 * @param sz double The z component of the scaling.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setToTranslationAndScaling(double tx, double ty, double tz, double sx, double sy, double sz) {
		identity();
		m[M03] = tx;
		m[M13] = ty;
		m[M23] = tz;
		m[M00] = sx;
		m[M11] = sy;
		m[M22] = sz;
		return this;
	}
	
	public void transform(final Vector3 position, final Vector3 scale, final Quaternion orientation)
    {
        orientation.toRotationMatrix(mTmp);

        m[0] = scale.x * mTmp[0]; m[1] = scale.y * mTmp[1]; m[2] = scale.z * mTmp[2]; m[3] = position.x;
        m[4] = scale.x * mTmp[4]; m[5] = scale.y * mTmp[5]; m[6] = scale.z * mTmp[6]; m[7] = position.y;
        m[8] = scale.x * mTmp[8]; m[9] = scale.y * mTmp[9]; m[10] = scale.z * mTmp[10]; m[11] = position.z;
        m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1;
    }
	
	public void inverseTransform(final Vector3 position, final Vector3 scale, final Quaternion orientation)
    {
        Vector3 invTranslate = position.inverse();
        Vector3 invScale = new Vector3(1 / scale.x, 1 / scale.y, 1 / scale.z);
        Quaternion invRot = orientation.invertAndCreate();

        invTranslate.setAll(invRot.multiply(invTranslate));
        invTranslate.multiply(invScale);
        invRot.toRotationMatrix(mTmp);

        m[0] = invScale.x * mTmp[0]; m[1] = invScale.x * mTmp[1]; m[2] = invScale.x * mTmp[2]; m[3] = invTranslate.x;
        m[4] = invScale.y * mTmp[4]; m[5] = invScale.y * mTmp[5]; m[6] = invScale.y * mTmp[6]; m[7] = invTranslate.y;
        m[8] = invScale.z * mTmp[8]; m[9] = invScale.z * mTmp[9]; m[10] = invScale.z * mTmp[10]; m[11] = invTranslate.z;		
        m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1;
    }
	
	public Vector3 transform(final Vector3 v) {
		return new Vector3(
				v.x * m[0] + v.y * m[4] + v.z * m[8] + m[12],
				v.x * m[1] + v.y * m[5] + v.z * m[9] + m[13],
				v.x * m[2] + v.y * m[6] + v.z * m[10] + m[14]
				);
	}
	
	public Matrix4 add(Matrix4 m2) {
		m2.toArray(mTmp);
        return new Matrix4(
	        m[0] + mTmp[0],
	        m[1] + mTmp[1],
	        m[2] + mTmp[2],
	        m[3] + mTmp[3],
	
	        m[4] + mTmp[4],
	        m[5] + mTmp[5],
	        m[6] + mTmp[6],
	        m[7] + mTmp[7],
	
	        m[8] + mTmp[8],
	        m[9] + mTmp[9],
	        m[10] + mTmp[10],
	        m[11] + mTmp[11],
	
	        m[12] + mTmp[12],
	        m[13] + mTmp[13],
	        m[14] + mTmp[14],
	        m[15] + mTmp[15]
	       );
	}
	
	public Matrix4 subtract(final Matrix4 m2)
    {
        m2.toArray(mTmp);
        return new Matrix4(
        		m[0] - mTmp[0],
        		m[1] - mTmp[1],
        		m[2] - mTmp[2],
        		m[3] - mTmp[3],

        		m[4] - mTmp[4],
        		m[5] - mTmp[5],
        		m[6] - mTmp[6],
        		m[7] - mTmp[7],

        		m[8] - mTmp[8],
        		m[9] - mTmp[9],
        		m[10] - mTmp[10],
        		m[11] - mTmp[11],

        		m[12] - mTmp[12],
        		m[13] - mTmp[13],
        		m[14] - mTmp[14],
        		m[15] - mTmp[15]
        );
    }
	

    //--------------------------------------------------
  	// Component fetch methods
  	//--------------------------------------------------
    
    /**
     * Creates a new {@link Vector3} representing the translation component
     * of this {@link Matrix4}.
     * 
     * @return {@link Vector3} representing the translation.
     */
    public Vector3 getTranslation() {
    	return new Vector3(m[M03], m[M13], m[M23]);
    }
    
    /**
     * Creates a new {@link Vector3} representing the scaling component
     * of this {@link Matrix4}.
     * 
     * @return {@link Vector3} representing the scaling.
     */
    public Vector3 getScaling() {
    	final double x = Math.sqrt(m[M00]*m[M00] + m[M01]*m[M01] + m[M02]*m[M02]);
    	final double y = Math.sqrt(m[M10]*m[M10] + m[M11]*m[M11] + m[M12]*m[M12]);
    	final double z = Math.sqrt(m[M20]*m[M20] + m[M21]*m[M21] + m[M22]*m[M22]);
    	return new Vector3(x, y, z);
    }
    
    //--------------------------------------------------
  	// Creation methods
  	//--------------------------------------------------
    
    /**
	 * Creates a new {@link Matrix4} representing a translation.
	 * 
	 * @param vec {@link Vector3} describing the translation components.
	 * @return A new {@link Matrix4} representing the translation only.
	 */
	public static Matrix4 createTranslationMatrix(final Vector3 vec) {
    	Matrix4 ret = new Matrix4();
    	ret.translate(vec);
    	return ret;
    }

	/**
	 * Creates a new {@link Matrix4} representing a translation.
	 * 
	 * @param x double The x component of the translation.
	 * @param y double The y component of the translation.
	 * @param z double The z component of the translation.
	 * @return A new {@link Matrix4} representing the translation only.
	 */
    public static Matrix4 createTranslationMatrix(double x, double y, double z) {
    	Matrix4 ret = new Matrix4();
    	ret.translate(x, y, z);
    	return ret;
    }

    /**
	 * Creates a new {@link Matrix4} representing a scaling.
	 * 
	 * @param vec {@link Vector3} describing the scaling components.
	 * @return A new {@link Matrix4} representing the scaling only.
	 */
    public static Matrix4 createScaleMatrix(final Vector3 vec) {
    	Matrix4 ret = new Matrix4();
    	ret.setToScale(vec);
    	return ret;
    }

    /**
	 * Creates a new {@link Matrix4} representing a scaling.
	 * 
	 * @param x double The x component of the scaling.
	 * @param y double The y component of the scaling.
	 * @param z double The z component of the scaling.
	 * @return A new {@link Matrix4} representing the scaling only.
	 */
    public static Matrix4 getScaleMatrix(double x, double y, double z) {
        Matrix4 ret = new Matrix4();
        ret.setToScale(x, y, z);
        return ret;
    }
    
    
    
    //--------------------------------------------------
    // Utility methods
    //--------------------------------------------------
    
    /**
     * Copies the backing array of this {@link Matrix4} into a float array and returns it.
     * 
     * @return float array containing a copy of the backing array. The returned array is owned
     * by this {@link Matrix4} and is subject to change as the implementation sees fit.
     */
    public float[] getFloatValues() {
    	ArrayUtils.convertDoublesToFloats(m, mFloat);
    	return mFloat;
    }
    
    /**
     * Returns the backing array of this {@link Matrix4}.
     * 
     * @return double array containing the backing array. The returned array is owned
     * by this {@link Matrix4} and is subject to change as the implementation sees fit.
     */
    public double[] getDoubleValues() {
    	return m;
    }
    
    /**
     * Create and return a copy of this {@link Matrix4}.
     * 
     * @return {@link Matrix4} The copy.
     */
    public Matrix4 clone() {
    	return new Matrix4(this);
    }
    
    /**
	 * Copies the backing array of this {@link Matrix4} into the provided double array.
	 * 
	 * @param doubleArray double array to store the copy in. Must be at least 16 elements long. 
	 * Entries will be placed starting at the 0 index.
	 */
	public void toArray(double[] doubleArray) {
		System.arraycopy(m, 0, doubleArray, 0, 16);
	}
	
	/**
	 * Determines if this {@link Matrix4} is equivalent to the provided {@link Matrix4}. For this 
	 * to be true each element must match exactly between the two.
	 * 
	 * @param m2 {@link Matrix4} the other matrix.
	 * @return boolean True if they are an exact match.
	 */
	public boolean equals(final Matrix4 m2) {
		m2.toArray(mTmp);
        if ( 
            m[0] != mTmp[0] || m[1] != mTmp[1] || m[2] != mTmp[2] || m[3] != mTmp[3] ||
            m[4] != mTmp[4] || m[5] != mTmp[5] || m[6] != mTmp[6] || m[7] != mTmp[7] ||
            m[8] != mTmp[8] || m[9] != mTmp[9] || m[10] != mTmp[10] || m[11] != mTmp[11] ||
            m[12] != mTmp[12] || m[13] != mTmp[13] || m[14] != mTmp[14] || m[15] != mTmp[15] )
            return false;
        return true;
    }
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + m[M00] + "|" + m[M01] + "|" + m[M02] + "|" + m[M03] + "]\n["
				+ m[M10] + "|" + m[M11] + "|" + m[M12] + "|" + m[M13] + "]\n["
				+ m[M20] + "|" + m[M21] + "|" + m[M22] + "|" + m[M23] + "]\n["
				+ m[M30] + "|" + m[M31] + "|" + m[M32] + "|" + m[M33] + "]\n";
	}
}
