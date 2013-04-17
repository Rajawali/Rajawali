package rajawali.animation.mesh;

import android.opengl.GLES20;
import android.os.SystemClock;

import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.SerializedObject3D;

public class VertexAnimationObject3D extends AAnimationObject3D {

	public VertexAnimationObject3D() {
		super();
	}

	public VertexAnimationObject3D(SerializedObject3D ser) {
		super(ser);
		float[][] v = ser.getFrameVertices();
		float[][] n = ser.getFrameNormals();
		String[] frameNames = ser.getFrameNames();

		mNumFrames = v.length;

		for (int i = 0; i < mNumFrames; ++i) {
			VertexAnimationFrame frame = new VertexAnimationFrame();
			frame.getGeometry().setVertices(v[i]);
			frame.getGeometry().setNormals(n[i]);
			frame.getGeometry().createVertexAndNormalBuffersOnly();
			frame.setName(frameNames[i]);
			mFrames.add(frame);
		}
	}

	public void setShaderParams(Camera camera) {
		super.setShaderParams(camera);

		long now = SystemClock.uptimeMillis();

		// Calculate interpolation and frame delta (if playing)
		if (isPlaying()) {
			mInterpolation += (float) (now - mStartTime) * mFps / 1000;
			mCurrentFrameIndex += (int) mInterpolation; // advance frame if interpolation >= 1
			if (mCurrentFrameIndex > mEndFrameIndex) {
				if (mLoop) {
					mCurrentFrameIndex -= mStartFrameIndex;
					mCurrentFrameIndex %= mEndFrameIndex - mStartFrameIndex;
					mCurrentFrameIndex += mStartFrameIndex;
				} else {
					mCurrentFrameIndex = mEndFrameIndex;
					pause();
				}
			}
			mInterpolation -= (int) mInterpolation; // clamp to [0, 1)
		}

		// Update geometry (if current frame is different from before)
		Geometry3D currentGeometry = ((VertexAnimationFrame) mFrames.get(mCurrentFrameIndex)).getGeometry();
		if (mGeometry.getVertexBufferInfo() != currentGeometry.getVertexBufferInfo()) {
			mGeometry.setVertexBufferInfo(currentGeometry.getVertexBufferInfo());
			mGeometry.setNormalBufferInfo(currentGeometry.getNormalBufferInfo());
		}

		// Find geometry for next frame in sequence
		Geometry3D nextGeometry = currentGeometry;
		int nextFrame = mCurrentFrameIndex + 1;
		if (nextFrame > mEndFrameIndex) {
			if (mLoop) {
				nextFrame = mStartFrameIndex;
			} else {
				nextFrame = mEndFrameIndex;
			}
		}
		if (nextFrame >= 0 && nextFrame < mNumFrames) {
			nextGeometry = ((VertexAnimationFrame) mFrames.get(nextFrame)).getGeometry();
		}

		// Set shader parameters
		mMaterial.setInterpolation(mInterpolation);
		mMaterial.setNextFrameVertices(nextGeometry.getVertexBufferInfo().bufferHandle);
		mMaterial.setNextFrameNormals(nextGeometry.getNormalBufferInfo().bufferHandle);

		mStartTime = now;
	}

	public void reload() {
		for (int i = 0; i < mNumFrames; i++) {
			mFrames.get(i).getGeometry().reload();
		}
		super.reload();
	}

	public VertexAnimationObject3D clone(boolean copyMaterial) {
		VertexAnimationObject3D clone = new VertexAnimationObject3D();
		clone.getGeometry().copyFromGeometry3D(mGeometry);
		clone.isContainer(mIsContainerOnly);
		if (copyMaterial)
			clone.setMaterial(mMaterial, false);
		for (int i = 0; i < mNumFrames; ++i) {
			clone.addFrame(getFrame(i));
		}
		clone.setRotation(getRotation());
		clone.setScale(getScale());
		clone.setFps(mFps);
		clone.mElementsBufferType = mGeometry.areOnlyShortBuffersSupported() ? GLES20.GL_UNSIGNED_SHORT : GLES20.GL_UNSIGNED_INT;
		return clone;
	}

	public VertexAnimationObject3D clone() {
		return clone(true);
	}
}
