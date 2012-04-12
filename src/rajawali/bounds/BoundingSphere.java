package rajawali.bounds;

import java.nio.FloatBuffer;

import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.materials.SimpleMaterial;
import rajawali.math.Number3D;
import rajawali.primitives.Sphere;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class BoundingSphere implements IBoundingVolume {
	protected Geometry3D mGeometry;
	protected float mRadius;
	protected Number3D mPosition;
	protected Sphere mVisualSphere;
	protected float[] mTmpMatrix = new float[16];
	
	public BoundingSphere() {
		super();
		mPosition = new Number3D();
	}
	
	public BoundingSphere(Geometry3D geometry) {
		this();
		mGeometry = geometry;
		calculateBounds(mGeometry);
	}
	
	public void drawBoundingVolume(Camera camera, float[] projMatrix, float[] vMatrix, float[] mMatrix) {
		if(mVisualSphere == null) {
			mVisualSphere = new Sphere(1, 8, 8);
			mVisualSphere.setMaterial(new SimpleMaterial());
			mVisualSphere.getMaterial().setUseColor(true);
			mVisualSphere.setColor(0xffffff00);
			mVisualSphere.setDrawingMode(GLES20.GL_LINE_LOOP);
		}

		Matrix.setIdentityM(mTmpMatrix, 0);
		mVisualSphere.setPosition(mPosition);
		mVisualSphere.setScale(mRadius);
		mVisualSphere.render(camera, projMatrix, vMatrix, mTmpMatrix, null);
	}
	
	public void transform(float[] matrix) {
		mPosition.setAll(0, 0, 0);
		mPosition.multiply(matrix);
		mPosition.x = -mPosition.x;
	}
	
	public void calculateBounds(Geometry3D geometry) {
		float radius = 0, maxRadius = 0;
		Number3D vertex = new Number3D();
		FloatBuffer vertices = geometry.getVertices();
		vertices.rewind();		
		
		while(vertices.hasRemaining()) {
			vertex.x = vertices.get();
			vertex.y = vertices.get();
			vertex.z = vertices.get();
			
			radius = vertex.length();
			if(radius > maxRadius) maxRadius = radius;
		}
		mRadius = maxRadius;
	}
}
