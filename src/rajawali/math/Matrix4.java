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
	
	//Matrix indices
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
	public Matrix4(Matrix4 matrix) {
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
	public Matrix4(Quaternion quat) {
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
	public Matrix4 setAll(Matrix4 matrix) {
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
	public Matrix4 setAll(Quaternion quat) {
		quat.toRotationMatrix(m);
		return this;
	}
	
	/**
	 * Sets the elements of this {@link Matrix4} based on the rotation represented by
	 * the provided quaternion elements. This method will produce an intermediate {@link Quaternion}. 
	 * 
	 * @param w double The w component of the quaternion.
	 * @param x double The x component of the quaternion.
	 * @param y double The y component of the quaternion.
	 * @param z double The z component of the quaternion.
	 * @return A reference to this {@link Matrix4} to facilitate chaining.
	 */
	public Matrix4 setAll(double w, double x, double y, double z) {
		Quaternion quat = new Quaternion(w, x, y, z);
		return setAll(quat);
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
	
	public Matrix4 identity() {
		m[M00] = 1;	m[M10] = 0;	m[M20] = 0;	m[M30] = 0;
		m[M01] = 0;	m[M11] = 1;	m[M21] = 0;	m[M31] = 0;
		m[M02] = 0;	m[M12] = 0;	m[M22] = 1;	m[M32] = 0;
		m[M03] = 0;	m[M13] = 0;	m[M23] = 0;	m[M33] = 1;
		return this;
	}
	
	public Matrix4 zero() {
		for(int i=0; i<16; ++i) {
			m[i] = 0;
		}
		return this;
	}
	
	public double determinant() {
		double n11 = m[0], n12 = m[1], n13 = m[2], n14 = m[3],
		n21 = m[4], n22 = m[5], n23 = m[6], n24 = m[7],
		n31 = m[8], n32 = m[9], n33 = m[10], n34 = m[11],
		n41 = m[12], n42 = m[13], n43 = m[14], n44 = m[15];

		return (
			n14 * n23 * n32 * n41-
			n13 * n24 * n32 * n41-
			n14 * n22 * n33 * n41+
			n12 * n24 * n33 * n41+

			n13 * n22 * n34 * n41-
			n12 * n23 * n34 * n41-
			n14 * n23 * n31 * n42+
			n13 * n24 * n31 * n42+

			n14 * n21 * n33 * n42-
			n11 * n24 * n33 * n42-
			n13 * n21 * n34 * n42+
			n11 * n23 * n34 * n42+

			n14 * n22 * n31 * n43-
			n12 * n24 * n31 * n43-
			n14 * n21 * n32 * n43+
			n11 * n24 * n32 * n43+

			n12 * n21 * n34 * n43-
			n11 * n22 * n34 * n43-
			n13 * n22 * n31 * n44+
			n12 * n23 * n31 * n44+

			n13 * n21 * n32 * n44-
			n11 * n23 * n32 * n44-
			n12 * n21 * n33 * n44+
			n11 * n22 * n33 * n44
		);
	}
	
	public Matrix4 inverse()
    {
        double m00 = m[0], 	m01 = m[1], 	m02 = m[2], 	m03 = m[3];
        double m10 = m[4], 	m11 = m[5], 	m12 = m[6], 	m13 = m[7];
        double m20 = m[8], 	m21 = m[9], 	m22 = m[10], 	m23 = m[11];
        double m30 = m[12], 	m31 = m[13], 	m32 = m[14], 	m33 = m[15];

        double v0 = m20 * m31 - m21 * m30;
        double v1 = m20 * m32 - m22 * m30;
        double v2 = m20 * m33 - m23 * m30;
        double v3 = m21 * m32 - m22 * m31;
        double v4 = m21 * m33 - m23 * m31;
        double v5 = m22 * m33 - m23 * m32;

        double t00 = + (v5 * m11 - v4 * m12 + v3 * m13);
        double t10 = - (v5 * m10 - v2 * m12 + v1 * m13);
        double t20 = + (v4 * m10 - v2 * m11 + v0 * m13);
        double t30 = - (v3 * m10 - v1 * m11 + v0 * m12);

        double invDet = 1 / (t00 * m00 + t10 * m01 + t20 * m02 + t30 * m03);

        double d00 = t00 * invDet;
        double d10 = t10 * invDet;
        double d20 = t20 * invDet;
        double d30 = t30 * invDet;

        double d01 = - (v5 * m01 - v4 * m02 + v3 * m03) * invDet;
        double d11 = + (v5 * m00 - v2 * m02 + v1 * m03) * invDet;
        double d21 = - (v4 * m00 - v2 * m01 + v0 * m03) * invDet;
        double d31 = + (v3 * m00 - v1 * m01 + v0 * m02) * invDet;

        v0 = m10 * m31 - m11 * m30;
        v1 = m10 * m32 - m12 * m30;
        v2 = m10 * m33 - m13 * m30;
        v3 = m11 * m32 - m12 * m31;
        v4 = m11 * m33 - m13 * m31;
        v5 = m12 * m33 - m13 * m32;

        double d02 = + (v5 * m01 - v4 * m02 + v3 * m03) * invDet;
        double d12 = - (v5 * m00 - v2 * m02 + v1 * m03) * invDet;
        double d22 = + (v4 * m00 - v2 * m01 + v0 * m03) * invDet;
        double d32 = - (v3 * m00 - v1 * m01 + v0 * m02) * invDet;

        v0 = m21 * m10 - m20 * m11;
        v1 = m22 * m10 - m20 * m12;
        v2 = m23 * m10 - m20 * m13;
        v3 = m22 * m11 - m21 * m12;
        v4 = m23 * m11 - m21 * m13;
        v5 = m23 * m12 - m22 * m13;

        double d03 = - (v5 * m01 - v4 * m02 + v3 * m03) * invDet;
        double d13 = + (v5 * m00 - v2 * m02 + v1 * m03) * invDet;
        double d23 = - (v4 * m00 - v2 * m01 + v0 * m03) * invDet;
        double d33 = + (v3 * m00 - v1 * m01 + v0 * m02) * invDet;

        return new Matrix4(
            d00, d01, d02, d03,
            d10, d11, d12, d13,
            d20, d21, d22, d23,
            d30, d31, d32, d33);
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
	
	public Matrix4 multiply(final Matrix4 m2)
    {
		m2.toArray(mTmp);
		return new Matrix4(
				m[0] * mTmp[0] + m[1] * mTmp[4] + m[2] * mTmp[8] + m[3] * mTmp[12],
				m[0] * mTmp[1] + m[1] * mTmp[5] + m[2] * mTmp[9] + m[3] * mTmp[13],
		        m[0] * mTmp[2] + m[1] * mTmp[6] + m[2] * mTmp[10] + m[3] * mTmp[14],
		        m[0] * mTmp[3] + m[1] * mTmp[7] + m[2] * mTmp[11] + m[3] * mTmp[15],
		
		        m[4] * mTmp[0] + m[5] * mTmp[4] + m[6] * mTmp[8] + m[7] * mTmp[12],
		        m[4] * mTmp[1] + m[5] * mTmp[5] + m[6] * mTmp[9] + m[7] * mTmp[13],
		        m[4] * mTmp[2] + m[5] * mTmp[6] + m[6] * mTmp[10] + m[7] * mTmp[14],
		        m[4] * mTmp[3] + m[5] * mTmp[7] + m[6] * mTmp[11] + m[7] * mTmp[15],
		
		        m[8] * mTmp[0] + m[9] * mTmp[4] + m[10] * mTmp[8] + m[11] * mTmp[12],
		        m[8] * mTmp[1] + m[9] * mTmp[5] + m[10] * mTmp[9] + m[11] * mTmp[13],
		        m[8] * mTmp[2] + m[9] * mTmp[6] + m[10] * mTmp[10] + m[11] * mTmp[14],
		        m[8] * mTmp[3] + m[9] * mTmp[7] + m[10] * mTmp[11] + m[11] * mTmp[15],
		
		        m[12] * mTmp[0] + m[13] * mTmp[4] + m[14] * mTmp[8] + m[15] * mTmp[12],
		        m[12] * mTmp[1] + m[13] * mTmp[5] + m[14] * mTmp[9] + m[15] * mTmp[13],
		        m[12] * mTmp[2] + m[13] * mTmp[6] + m[14] * mTmp[10] + m[15] * mTmp[14],
		        m[12] * mTmp[3] + m[13] * mTmp[7] + m[14] * mTmp[11] + m[15] * mTmp[15]
		);
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
	
	public Matrix4 transpose()
    {
        return new Matrix4(m[0], m[4], m[8], m[12],
                       m[1], m[5], m[9], m[13],
                       m[2], m[6], m[10], m[14],
                       m[3], m[7], m[11], m[15]);
    }
	
	public void setTranslation(final Vector3 v )
    {
        m[3] = v.x;
        m[7] = v.y;
        m[11] = v.z;
    }

    public Vector3 getTranslation()
    {
    	return new Vector3(m[3], m[7], m[11]);
    }
    
    public void makeTrans(final Vector3 v )
    {
        m[0] = 1.0f; m[1] = 0; m[2] = 0; m[3] = v.x;
        m[4] = 0; m[5] = 1.0f; m[6] = 0; m[7] = v.y;
        m[8] = 0; m[9] = 0; m[10] = 1.0f; m[11] = v.z;
        m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1.0f;
    }

    public void makeTrans(double tx, double ty, double tz)
    {
        m[0] = 1.0f; m[1] = 0; m[2] = 0; m[3] = tx;
        m[4] = 0; m[5] = 1.0f; m[6] = 0; m[7] = ty;
        m[8] = 0; m[9] = 0; m[10] = 1.0f; m[11] = tz;
        m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1.0f;
    }

    public static Matrix4 getTranslationMatrix(final Vector3 v)
    {
    	return new Matrix4(
	        1.0f, 	0,		0,		v.x,
	        0, 		1.0f,	0, 		v.y,
	        0, 		0, 		1.0f,	v.z,
	        0, 		0, 		0,		1.0f
        );
    }

    public static Matrix4 getTrans(double x, double y, double z)
    {
    	return new Matrix4(
	        1.0f, 	0,		0,		x,
	        0, 		1.0f,	0,		y,
	        0,		0,		1.0f,	z,
	        0,		0,		0,		1.0f
	    );
    }

    public void setScale(final Vector3 v)
    {
        m[0] = v.x;
        m[5] = v.y;
        m[10] = v.z;
    }

    public static Matrix4 getScaleMatrix(final Vector3 v)
    {
        return new Matrix4(
	        v.x,	0,		0,		0,
	        0,		v.y,	0,		0,
	        0,		0,		v.z,	0,
	        0,		0,		0,		1.0f
	    );
    }

    public static Matrix4 getScaleMatrix(double x, double y, double z)
    {
        return new Matrix4(
	        x, 		0,		0,		0,
	        0,		y,		0,		0,
	        0,		0,		z,		0,
	        0,		0,		0,		1.0f
        );
    }
    
    
    
    //--------------------------------------------------
    // Utility methods
    //--------------------------------------------------
    
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
}
