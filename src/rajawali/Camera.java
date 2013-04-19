package rajawali;

import rajawali.math.MathUtil;
import rajawali.math.Number3D;
import rajawali.math.Number3D.Axis;
import rajawali.math.Quaternion;
import android.opengl.Matrix;

public class Camera extends ATransformable3D {
	protected float[] mVMatrix = new float[16];
	protected float[] mInvVMatrix = new float[16];
	protected float[] mRotationMatrix = new float[16];
	protected float[] mProjMatrix = new float[16];
	protected float mNearPlane = 1.0f;
	protected float mFarPlane = 120.0f;
	protected float mFieldOfView = 45;
	protected Number3D mUpAxis;
	protected boolean mUseRotationMatrix = false;
	protected float[] mRotateMatrixTmp = new float[16];
	protected float[] mTmpMatrix = new float[16];
	protected float[] mCombinedMatrix=new float[16];
	public Frustum mFrustum;
	
	protected int mFogColor = 0xdddddd;
	protected float mFogNear = 5;
	protected float mFogFar = 25;
	protected boolean mFogEnabled = false;
	
	// Camera's localized vectors
	protected Number3D mRightVector;
	protected Number3D mUpVector;
	protected Number3D mLookVector;
	
	protected Quaternion mLocalOrientation;

	public Camera() {
		super();
		mLocalOrientation = Quaternion.getIdentity();
		mUpAxis = new Number3D(0, 1, 0);
		mIsCamera = true;
		mFrustum = new Frustum();
	}

	public float[] getViewMatrix() {
		if (mLookAt != null) {
			Matrix.setLookAtM(mVMatrix, 0, mPosition.x, mPosition.y,
					mPosition.z, mLookAt.x, mLookAt.y, mLookAt.z, mUpAxis.x, mUpAxis.y,
					mUpAxis.z);
			
			mLocalOrientation.fromEuler(mRotation.y, mRotation.z, mRotation.x);
			mLocalOrientation.toRotationMatrix(mRotationMatrix);
			Matrix.multiplyMM(mVMatrix, 0, mRotationMatrix, 0, mVMatrix, 0);
		} else {
			if (mUseRotationMatrix == false && mRotationDirty) {
				setOrientation();
				mOrientation.toRotationMatrix(mRotationMatrix);
				mRotationDirty = false;
			}
			Matrix.setIdentityM(mTmpMatrix, 0);
			Matrix.setIdentityM(mVMatrix, 0);
			Matrix.translateM(mTmpMatrix, 0, -mPosition.x, -mPosition.y, -mPosition.z);
			Matrix.multiplyMM(mVMatrix, 0, mRotationMatrix, 0, mTmpMatrix, 0);
		}
		return mVMatrix;
	}
	
	public void updateFrustum(float[] pMatrix,float[] vMatrix) {
		Matrix.multiplyMM(mCombinedMatrix, 0, pMatrix, 0, vMatrix, 0);
		invertM(mTmpMatrix, 0, mCombinedMatrix, 0);
		mFrustum.update(mCombinedMatrix);
	}

	protected void rotateM(float[] m, int mOffset, float a, float x, float y,
			float z) {
		Matrix.setIdentityM(mRotateMatrixTmp, 0);
		Matrix.setRotateM(mRotateMatrixTmp, 0, a, x, y, z);
		System.arraycopy(m, 0, mTmpMatrix, 0, 16);
		Matrix.multiplyMM(m, mOffset, mTmpMatrix, mOffset, mRotateMatrixTmp, 0);
	}

	public void setRotationMatrix(float[] m) {
		mRotationMatrix = m;
	}

	public void setProjectionMatrix(int width, int height) {
		float ratio = (float) width / height;
		float frustumH = MathUtil.tan(getFieldOfView() / 360.0f * MathUtil.PI)
				* getNearPlane();
		float frustumW = frustumH * ratio;

		Matrix.frustumM(mProjMatrix, 0, -frustumW, frustumW, -frustumH,
				frustumH, getNearPlane(), getFarPlane());
	}
	
	 /**
     * Inverts a 4 x 4 matrix.
     *
     * @param mInv the array that holds the output inverted matrix
     * @param mInvOffset an offset into mInv where the inverted matrix is
     *        stored.
     * @param m the input array
     * @param mOffset an offset into m where the matrix is stored.
     * @return true if the matrix could be inverted, false if it could not.
     */
    public static boolean invertM(float[] mInv, int mInvOffset, float[] m,
            int mOffset) {
        // Invert a 4 x 4 matrix using Cramer's Rule

        // transpose matrix
        final float src0  = m[mOffset +  0];
        final float src4  = m[mOffset +  1];
        final float src8  = m[mOffset +  2];
        final float src12 = m[mOffset +  3];

        final float src1  = m[mOffset +  4];
        final float src5  = m[mOffset +  5];
        final float src9  = m[mOffset +  6];
        final float src13 = m[mOffset +  7];

        final float src2  = m[mOffset +  8];
        final float src6  = m[mOffset +  9];
        final float src10 = m[mOffset + 10];
        final float src14 = m[mOffset + 11];

        final float src3  = m[mOffset + 12];
        final float src7  = m[mOffset + 13];
        final float src11 = m[mOffset + 14];
        final float src15 = m[mOffset + 15];

        // calculate pairs for first 8 elements (cofactors)
        final float atmp0  = src10 * src15;
        final float atmp1  = src11 * src14;
        final float atmp2  = src9  * src15;
        final float atmp3  = src11 * src13;
        final float atmp4  = src9  * src14;
        final float atmp5  = src10 * src13;
        final float atmp6  = src8  * src15;
        final float atmp7  = src11 * src12;
        final float atmp8  = src8  * src14;
        final float atmp9  = src10 * src12;
        final float atmp10 = src8  * src13;
        final float atmp11 = src9  * src12;

        // calculate first 8 elements (cofactors)
        final float dst0  = (atmp0 * src5 + atmp3 * src6 + atmp4  * src7)
                          - (atmp1 * src5 + atmp2 * src6 + atmp5  * src7);
        final float dst1  = (atmp1 * src4 + atmp6 * src6 + atmp9  * src7)
                          - (atmp0 * src4 + atmp7 * src6 + atmp8  * src7);
        final float dst2  = (atmp2 * src4 + atmp7 * src5 + atmp10 * src7)
                          - (atmp3 * src4 + atmp6 * src5 + atmp11 * src7);
        final float dst3  = (atmp5 * src4 + atmp8 * src5 + atmp11 * src6)
                          - (atmp4 * src4 + atmp9 * src5 + atmp10 * src6);
        final float dst4  = (atmp1 * src1 + atmp2 * src2 + atmp5  * src3)
                          - (atmp0 * src1 + atmp3 * src2 + atmp4  * src3);
        final float dst5  = (atmp0 * src0 + atmp7 * src2 + atmp8  * src3)
                          - (atmp1 * src0 + atmp6 * src2 + atmp9  * src3);
        final float dst6  = (atmp3 * src0 + atmp6 * src1 + atmp11 * src3)
                          - (atmp2 * src0 + atmp7 * src1 + atmp10 * src3);
        final float dst7  = (atmp4 * src0 + atmp9 * src1 + atmp10 * src2)
                          - (atmp5 * src0 + atmp8 * src1 + atmp11 * src2);

        // calculate pairs for second 8 elements (cofactors)
        final float btmp0  = src2 * src7;
        final float btmp1  = src3 * src6;
        final float btmp2  = src1 * src7;
        final float btmp3  = src3 * src5;
        final float btmp4  = src1 * src6;
        final float btmp5  = src2 * src5;
        final float btmp6  = src0 * src7;
        final float btmp7  = src3 * src4;
        final float btmp8  = src0 * src6;
        final float btmp9  = src2 * src4;
        final float btmp10 = src0 * src5;
        final float btmp11 = src1 * src4;

        // calculate second 8 elements (cofactors)
        final float dst8  = (btmp0  * src13 + btmp3  * src14 + btmp4  * src15)
                          - (btmp1  * src13 + btmp2  * src14 + btmp5  * src15);
        final float dst9  = (btmp1  * src12 + btmp6  * src14 + btmp9  * src15)
                          - (btmp0  * src12 + btmp7  * src14 + btmp8  * src15);
        final float dst10 = (btmp2  * src12 + btmp7  * src13 + btmp10 * src15)
                          - (btmp3  * src12 + btmp6  * src13 + btmp11 * src15);
        final float dst11 = (btmp5  * src12 + btmp8  * src13 + btmp11 * src14)
                          - (btmp4  * src12 + btmp9  * src13 + btmp10 * src14);
        final float dst12 = (btmp2  * src10 + btmp5  * src11 + btmp1  * src9 )
                          - (btmp4  * src11 + btmp0  * src9  + btmp3  * src10);
        final float dst13 = (btmp8  * src11 + btmp0  * src8  + btmp7  * src10)
                          - (btmp6  * src10 + btmp9  * src11 + btmp1  * src8 );
        final float dst14 = (btmp6  * src9  + btmp11 * src11 + btmp3  * src8 )
                          - (btmp10 * src11 + btmp2  * src8  + btmp7  * src9 );
        final float dst15 = (btmp10 * src10 + btmp4  * src8  + btmp9  * src9 )
                          - (btmp8  * src9  + btmp11 * src10 + btmp5  * src8 );

        // calculate determinant
        final float det =
                src0 * dst0 + src1 * dst1 + src2 * dst2 + src3 * dst3;

        if (det == 0.0f) {
            return false;
        }

        // calculate matrix inverse
        final float invdet = 1.0f / det;
        mInv[     mInvOffset] = dst0  * invdet;
        mInv[ 1 + mInvOffset] = dst1  * invdet;
        mInv[ 2 + mInvOffset] = dst2  * invdet;
        mInv[ 3 + mInvOffset] = dst3  * invdet;

        mInv[ 4 + mInvOffset] = dst4  * invdet;
        mInv[ 5 + mInvOffset] = dst5  * invdet;
        mInv[ 6 + mInvOffset] = dst6  * invdet;
        mInv[ 7 + mInvOffset] = dst7  * invdet;

        mInv[ 8 + mInvOffset] = dst8  * invdet;
        mInv[ 9 + mInvOffset] = dst9  * invdet;
        mInv[10 + mInvOffset] = dst10 * invdet;
        mInv[11 + mInvOffset] = dst11 * invdet;

        mInv[12 + mInvOffset] = dst12 * invdet;
        mInv[13 + mInvOffset] = dst13 * invdet;
        mInv[14 + mInvOffset] = dst14 * invdet;
        mInv[15 + mInvOffset] = dst15 * invdet;

        return true;
    }

    public void setUpAxis(float x, float y, float z) {
    	mUpAxis.setAll(x, y, z);
    }
    
    public void setUpAxis(Number3D upAxis) {
    	mUpAxis.setAllFrom(upAxis);
    }
    
    public void setUpAxis(Axis upAxis) {
    	if(upAxis == Axis.X)
    		mUpAxis.setAll(1, 0, 0);
    	else if(upAxis == Axis.Y)
    		mUpAxis.setAll(0, 1, 0);
    	else
    		mUpAxis.setAll(0, 0, 1);
    }
    
	public float[] getProjectionMatrix() {
		return mProjMatrix;
	}

	public float getNearPlane() {
		return mNearPlane;
	}

	public void setNearPlane(float nearPlane) {
		this.mNearPlane = nearPlane;
	}

	public float getFarPlane() {
		return mFarPlane;
	}

	public void setFarPlane(float farPlane) {
		this.mFarPlane = farPlane;
	}

	public float getFieldOfView() {
		return mFieldOfView;
	}

	public void setFieldOfView(float fieldOfView) {
		this.mFieldOfView = fieldOfView;
	}

	public boolean getUseRotationMatrix() {
		return mUseRotationMatrix;
	}

	public void setUseRotationMatrix(boolean useRotationMatrix) {
		this.mUseRotationMatrix = useRotationMatrix;
	}

	public int getFogColor() {
		return mFogColor;
	}

	public void setFogColor(int fogColor) {
		this.mFogColor = fogColor;
	}

	public float getFogNear() {
		return mFogNear;
	}

	public void setFogNear(float fogNear) {
		this.mFogNear = fogNear;
	}

	public float getFogFar() {
		return mFogFar;
	}

	public void setFogFar(float fogFar) {
		this.mFogFar = fogFar;
	}

	public boolean isFogEnabled() {
		return mFogEnabled;
	}

	public void setFogEnabled(boolean fogEnabled) {
		this.mFogEnabled = fogEnabled;
	}
}
