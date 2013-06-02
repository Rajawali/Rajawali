package rajawali.primitives;

import android.opengl.Matrix;
import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Camera2D;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;

/**
 * A screen quad is a plane that covers the whole screen. When used in conjunction with
 * {@link Camera2D} you'll get a pixel perfect screen filling plane. This is perfect for
 * things like image slide shows or fragment shader only apps and live wallpapers.
 * <p>
 * Usage:
 * </p>
 * <pre><code>
 * // -- Use the 2D camera
 * getCurrentScene().switchCamera(new Camera2D());
 * ScreenQuad screenQuad = new ScreenQuad();
 * SimpleMaterial material = new SimpleMaterial();
 * screenQuad.setMaterial(material);
 * </code></pre>
 * 
 * If you want to show square images without distortion you'll need to resize the quad
 * when the surface changes:
 * 
 * <pre><code>
 * public void onSurfaceChanged(GL10 gl, int width, int height) {
 * 	super.onSurfaceChanged(gl, width, height);
 * 	if(width < height)
 * 		screenQuad.setScale((float)height / (float)width, 1, 0);
 * 	else
 * 		screenQuad.setScale(1, (float)width / (float)height, 0);
 * }
 * </code></pre>
 * 
 * @author dennis.ippel
 *
 */
public class ScreenQuad extends BaseObject3D {
	private Camera2D mCamera;
	private float[] mVPMatrix;
	/**
	 * Creates a new ScreenQuad.
	 */
	public ScreenQuad()
	{
		super();
		init();
	}

	private void init() {
		mCamera = new Camera2D();
		mCamera.setProjectionMatrix(0, 0);
		mVPMatrix = new float[16];
		
		float[] vertices = new float[] {
				-.5f, .5f, 0,
				.5f, .5f, 0,
				.5f, -.5f, 0,
				-.5f, -.5f, 0
		};
		float[] textureCoords = new float[] {
				0, 0, 1, 0, 1, 1, 0, 1
		};
		float[] normals = new float[] {
				0, 0, 1,
				0, 0, 1,
				0, 0, 1,
				0, 0, 1
		};
		int[] indices = new int[] { 0, 2, 1, 0, 3, 2 };
		
		setData(vertices, normals, textureCoords, null, indices);
		
		vertices = null;
		normals = null;
		textureCoords = null;
		indices = null;
		
		mEnableDepthTest = false;
		mEnableDepthMask = false;
	}
	
	public void render(Camera camera, float[] vpMatrix, float[] projMatrix, float[] vMatrix, final float[] parentMatrix,
			ColorPickerInfo pickerInfo) {
		float[] pMatrix = mCamera.getProjectionMatrix();
		float[] viewMatrix = mCamera.getViewMatrix();
		Matrix.multiplyMM(mVPMatrix, 0, pMatrix, 0, viewMatrix, 0);
		super.render(mCamera, mVPMatrix, projMatrix, viewMatrix, null, null);
	}
}
