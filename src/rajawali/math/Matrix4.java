package rajawali.math;

public final class Matrix4 {
	private float[] m; 
	private float[] mTmp;
	
	public Matrix4() {
		m = new float[16];
		mTmp = new float[16];
	}
	
	public Matrix4(Matrix4 other) {
		this();
		other.toFloatArray(mTmp);
		set(mTmp);
	}
	
	public Matrix4(float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33) {
		this();
		setAll(m00, m01, m02, m03,
	            m10, m11, m12, m13,
	            m20, m21, m22, m23,
	            m30, m31, m32, m33);
	}
	
	public void setAll(float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33) {
		m[0] = m00;		m[1] = m01;		m[2] = m02;		m[3] = m03;
		m[4] = m10;		m[5] = m11;		m[6] = m12;		m[7] = m13;
		m[8] = m20;		m[9] = m21;		m[10] = m22;	m[11] = m23;
		m[12] = m30;	m[13] = m31;	m[14] = m32;	m[15] = m33;
	}
	
	public void set(float[] other) {
		System.arraycopy(other, 0, m, 0, 16);
	}
	
	public void identity() {
		m[0] = 1;	m[1] = 0;	m[2] = 0;	m[3] = 0;
		m[4] = 0;	m[5] = 1;	m[6] = 0;	m[7] = 0;
		m[8] = 0;	m[9] = 0;	m[10] = 1;	m[11] = 0;
		m[12] = 0;	m[13] = 0;	m[14] = 0;	m[15] = 1;
	}
	
	public void zero() {
		for(int i=0; i<16; ++i) {
			m[i] = 0;
		}
	}
	
	public float determinant() {
		float n11 = m[0], n12 = m[1], n13 = m[2], n14 = m[3],
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
        float m00 = m[0], 	m01 = m[1], 	m02 = m[2], 	m03 = m[3];
        float m10 = m[4], 	m11 = m[5], 	m12 = m[6], 	m13 = m[7];
        float m20 = m[8], 	m21 = m[9], 	m22 = m[10], 	m23 = m[11];
        float m30 = m[12], 	m31 = m[13], 	m32 = m[14], 	m33 = m[15];

        float v0 = m20 * m31 - m21 * m30;
        float v1 = m20 * m32 - m22 * m30;
        float v2 = m20 * m33 - m23 * m30;
        float v3 = m21 * m32 - m22 * m31;
        float v4 = m21 * m33 - m23 * m31;
        float v5 = m22 * m33 - m23 * m32;

        float t00 = + (v5 * m11 - v4 * m12 + v3 * m13);
        float t10 = - (v5 * m10 - v2 * m12 + v1 * m13);
        float t20 = + (v4 * m10 - v2 * m11 + v0 * m13);
        float t30 = - (v3 * m10 - v1 * m11 + v0 * m12);

        float invDet = 1 / (t00 * m00 + t10 * m01 + t20 * m02 + t30 * m03);

        float d00 = t00 * invDet;
        float d10 = t10 * invDet;
        float d20 = t20 * invDet;
        float d30 = t30 * invDet;

        float d01 = - (v5 * m01 - v4 * m02 + v3 * m03) * invDet;
        float d11 = + (v5 * m00 - v2 * m02 + v1 * m03) * invDet;
        float d21 = - (v4 * m00 - v2 * m01 + v0 * m03) * invDet;
        float d31 = + (v3 * m00 - v1 * m01 + v0 * m02) * invDet;

        v0 = m10 * m31 - m11 * m30;
        v1 = m10 * m32 - m12 * m30;
        v2 = m10 * m33 - m13 * m30;
        v3 = m11 * m32 - m12 * m31;
        v4 = m11 * m33 - m13 * m31;
        v5 = m12 * m33 - m13 * m32;

        float d02 = + (v5 * m01 - v4 * m02 + v3 * m03) * invDet;
        float d12 = - (v5 * m00 - v2 * m02 + v1 * m03) * invDet;
        float d22 = + (v4 * m00 - v2 * m01 + v0 * m03) * invDet;
        float d32 = - (v3 * m00 - v1 * m01 + v0 * m02) * invDet;

        v0 = m21 * m10 - m20 * m11;
        v1 = m22 * m10 - m20 * m12;
        v2 = m23 * m10 - m20 * m13;
        v3 = m22 * m11 - m21 * m12;
        v4 = m23 * m11 - m21 * m13;
        v5 = m23 * m12 - m22 * m13;

        float d03 = - (v5 * m01 - v4 * m02 + v3 * m03) * invDet;
        float d13 = + (v5 * m00 - v2 * m02 + v1 * m03) * invDet;
        float d23 = - (v4 * m00 - v2 * m01 + v0 * m03) * invDet;
        float d33 = + (v3 * m00 - v1 * m01 + v0 * m02) * invDet;

        return new Matrix4(
            d00, d01, d02, d03,
            d10, d11, d12, d13,
            d20, d21, d22, d23,
            d30, d31, d32, d33);
    }
	
	public void transform(final Number3D position, final Number3D scale, final Quaternion orientation)
    {
        orientation.toRotationMatrix(mTmp);

        m[0] = scale.x * mTmp[0]; m[1] = scale.y * mTmp[1]; m[2] = scale.z * mTmp[2]; m[3] = position.x;
        m[4] = scale.x * mTmp[4]; m[5] = scale.y * mTmp[5]; m[6] = scale.z * mTmp[6]; m[7] = position.y;
        m[8] = scale.x * mTmp[8]; m[9] = scale.y * mTmp[9]; m[10] = scale.z * mTmp[10]; m[11] = position.z;
        m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1;
    }
	
	public void inverseTransform(final Number3D position, final Number3D scale, final Quaternion orientation)
    {
        Number3D invTranslate = position.inverse();
        Number3D invScale = new Number3D(1 / scale.x, 1 / scale.y, 1 / scale.z);
        Quaternion invRot = orientation.inverse();

        invTranslate = invRot.multiply(invTranslate);
        invTranslate.multiply(invScale);
        invRot.toRotationMatrix(mTmp);

        m[0] = invScale.x * mTmp[0]; m[1] = invScale.x * mTmp[1]; m[2] = invScale.x * mTmp[2]; m[3] = invTranslate.x;
        m[4] = invScale.y * mTmp[4]; m[5] = invScale.y * mTmp[5]; m[6] = invScale.y * mTmp[6]; m[7] = invTranslate.y;
        m[8] = invScale.z * mTmp[8]; m[9] = invScale.z * mTmp[9]; m[10] = invScale.z * mTmp[10]; m[11] = invTranslate.z;		
        m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1;
    }
	
	public Number3D transform(final Number3D v) {
		return new Number3D(
				v.x * m[0] + v.y * m[4] + v.z * m[8] + m[12],
				v.x * m[1] + v.y * m[5] + v.z * m[9] + m[13],
				v.x * m[2] + v.y * m[6] + v.z * m[10] + m[14]
				);
	}
	
	public void toFloatArray(float[] floatArray) {
		System.arraycopy(m, 0, floatArray, 0, 16);
	}
	
	public Matrix4 multiply(final Matrix4 m2)
    {
		m2.toFloatArray(mTmp);
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
	
	public Number3D multiply(final Number3D v) {
		 Number3D r = new Number3D();

         float inv = 1.0f / ( m[12] * v.x + m[13] * v.y + m[14] * v.z + m[15] );

         r.x = ( m[0] * v.x + m[1] * v.y + m[2] * v.z + m[3] ) * inv;
         r.y = ( m[4] * v.x + m[5] * v.y + m[6] * v.z + m[7] ) * inv;
         r.z = ( m[8] * v.x + m[8] * v.y + m[10] * v.z + m[11] ) * inv;

         return r;
	}
	
	public Matrix4 multiply(final float value) {
		return new Matrix4(
	            value*m[0], value*m[1], value*m[2], value*m[3],
	            value*m[4], value*m[5], value*m[6], value*m[7],
	            value*m[8], value*m[9], value*m[10], value*m[11],
	            value*m[12], value*m[13], value*m[14], value*m[15]);
	}
	
	public Matrix4 add(Matrix4 m2) {
		m2.toFloatArray(mTmp);
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
        m2.toFloatArray(mTmp);
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
	
	public boolean equals(final Matrix4 m2)
    {
		m2.toFloatArray(mTmp);
        if( 
            m[0] != mTmp[0] || m[1] != mTmp[1] || m[2] != mTmp[2] || m[3] != mTmp[3] ||
            m[4] != mTmp[4] || m[5] != mTmp[5] || m[6] != mTmp[6] || m[7] != mTmp[7] ||
            m[8] != mTmp[8] || m[9] != mTmp[9] || m[10] != mTmp[10] || m[11] != mTmp[11] ||
            m[12] != mTmp[12] || m[13] != mTmp[13] || m[14] != mTmp[14] || m[15] != mTmp[15] )
            return false;
        return true;
    }
	
	public Matrix4 transpose()
    {
        return new Matrix4(m[0], m[4], m[8], m[12],
                       m[1], m[5], m[8], m[13],
                       m[2], m[6], m[9], m[14],
                       m[3], m[7], m[10], m[15]);
    }
	
	public void setTranslation(final Number3D v )
    {
        m[3] = v.x;
        m[7] = v.y;
        m[11] = v.z;
    }

    public Number3D getTranslation()
    {
    	return new Number3D(m[3], m[7], m[11]);
    }
    
    public void makeTrans(final Number3D v )
    {
        m[0] = 1.0f; m[1] = 0; m[2] = 0; m[3] = v.x;
        m[4] = 0; m[5] = 1.0f; m[6] = 0; m[7] = v.y;
        m[8] = 0; m[9] = 0; m[10] = 1.0f; m[11] = v.z;
        m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1.0f;
    }

    public void makeTrans(float tx, float ty, float tz)
    {
        m[0] = 1.0f; m[1] = 0; m[2] = 0; m[3] = tx;
        m[4] = 0; m[5] = 1.0f; m[6] = 0; m[7] = ty;
        m[8] = 0; m[9] = 0; m[10] = 1.0f; m[11] = tz;
        m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1.0f;
    }

    public static Matrix4 getTranslationMatrix(final Number3D v)
    {
    	return new Matrix4(
	        1.0f, 	0,		0,		v.x,
	        0, 		1.0f,	0, 		v.y,
	        0, 		0, 		1.0f,	v.z,
	        0, 		0, 		0,		1.0f
        );
    }

    public static Matrix4 getTrans(float x, float y, float z)
    {
    	return new Matrix4(
	        1.0f, 	0,		0,		x,
	        0, 		1.0f,	0,		y,
	        0,		0,		1.0f,	z,
	        0,		0,		0,		1.0f
	    );
    }

    public void setScale(final Number3D v)
    {
        m[0] = v.x;
        m[5] = v.y;
        m[10] = v.z;
    }

    public static Matrix4 getScaleMatrix(final Number3D v)
    {
        return new Matrix4(
	        v.x,	0,		0,		0,
	        0,		v.y,	0,		0,
	        0,		0,		v.z,	0,
	        0,		0,		0,		1.0f
	    );
    }

    public static Matrix4 getScaleMatrix(float x, float y, float z)
    {
        return new Matrix4(
	        x, 		0,		0,		0,
	        0,		y,		0,		0,
	        0,		0,		z,		0,
	        0,		0,		0,		1.0f
        );
    }
    
}
