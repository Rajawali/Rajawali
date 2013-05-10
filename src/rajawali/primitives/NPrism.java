package rajawali.primitives;

import rajawali.BaseObject3D;
import rajawali.math.Number3D;
import android.util.Log;

/**
 * Basic primitive allowing for the creation of an n-sided regular
 * polygonal cone, as a frustum or to a point with a specified slant
 * angle or aspect ratio. The cone is created about the positive
 * y axis with the vanishing point at (0, height, 0).
 * 
 * NOTE: This still needs a lot of work. Normals and texture coordinates are not correct.
 * 
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class NPrism extends BaseObject3D {

	protected int mSideCount = 3;
	protected double mRadiusBase;
	protected double mRadiusTop;
	protected double mHeight;
	protected double mFrustumHeight;

	private static final Number3D UP = new Number3D(0, 1, 0);
	private static final Number3D DOWN = new Number3D(0, -1, 0);

	private static final String TAG = "NCone";

	/**
	 * Creates a terminated prism.
	 * 
	 * @param sides Integer number of sides to the prism.
	 * @param radius Double the radius of the base.
	 * @param height Double the height of the prism.
	 */
	public NPrism(int sides, double radius, double height) {
		if (sides > 3) mSideCount = sides;
		mRadiusBase = radius;
		mRadiusTop = 0;
		mFrustumHeight = height;
		mHeight = height;
		init();
	}

	/**
	 * Creates a frustum like prism.
	 * 
	 * @param sides Integer number of sides to the prism.
	 * @param radiusTop Double the radius of the top.
	 * @param radiusBase Double the radius of the base.
	 * @param height Double the height of the prism.
	 */
	public NPrism(int sides, double radiusTop, double radiusBase, double height) {
		if (sides > 3) mSideCount = sides;
		mRadiusTop = radiusTop;
		mRadiusBase = radiusBase;
		mFrustumHeight = height;
		mHeight = height;
		init();
	}

	private void init() {
		int vertex_count = 6*mSideCount + 2;
		int tri_count = 4*mSideCount;
		int top_center_index = 3*vertex_count - 6;
		int bottom_center_index = 3*vertex_count - 3;
		Log.d(TAG, "Vertex Count: " + vertex_count);
		Log.d(TAG, "Tri Count: " + tri_count);

		int offset = 0;
		int triangle = 0;
		int index = 0;
		float[] vertices = new float[3*vertex_count];
		float[] normals = new float[3*vertex_count];
		float[] texture = new float[2*vertex_count];
		float[] colors = new float[4*vertex_count];
		int[] indices = new int[3*tri_count];
		
		double angle_delta = 2*Math.PI/mSideCount;
		double angle = 0;
		double x = 1.0f, y = 1.0f, z = 1.0f;
		double u = 0, v = 1;
		double u_delta = 1.0/mSideCount;
		double h = mHeight;
		double MAG = Math.sqrt(Math.pow((mRadiusTop - mRadiusBase), 2.0) + Math.pow(h, 2.0));
		Number3D temp_normal = new Number3D();
		if (mSideCount % 2 == 0) angle = angle_delta/2.0;

		//Populate the vertices
		int base_index;
		angle = (mSideCount % 2 == 0) ? angle = angle_delta/2.0 : 0;
		
		x = mRadiusTop*Math.cos(angle);
		z = mRadiusTop*Math.sin(angle);
		for (int side = 0; side < mSideCount; ++side) {
			base_index = 3*triangle;
			//Handle the top
			y = mHeight;
			v = 0;
			temp_normal.x = (float) (h*Math.cos(angle + angle_delta/2)/MAG);
			temp_normal.y = (float) (h/MAG);
			temp_normal.z = (float) (h*Math.sin(angle + angle_delta/2)/MAG);
			temp_normal.normalize();
			temp_normal.z = -temp_normal.z;

			vertices[offset] = (float) x;
			texture[2*index] = (float) u;
			texture[2*index+1] = (float) v;
			normals[offset++] = temp_normal.x;
			vertices[offset] = (float) y;
			normals[offset++] = temp_normal.y;
			vertices[offset] = (float) z;
			normals[offset++] = temp_normal.z;
			indices[base_index+2] = index++;

			y = 0;
			v = 1;
			x *= (mRadiusBase/mRadiusTop);
			z *= (mRadiusBase/mRadiusTop);
			vertices[offset] = (float) x;
			texture[2*index] = (float) u;
			texture[2*index+1] = (float) v;
			normals[offset++] = temp_normal.x;
			vertices[offset] = (float) y;
			normals[offset++] = temp_normal.y;
			vertices[offset] = (float) z;
			normals[offset++] = temp_normal.z;
			indices[base_index+1] = index++;

			angle += angle_delta;
			u += u_delta;
			x = mRadiusBase*Math.cos(angle);
			z = mRadiusBase*Math.sin(angle);
			vertices[offset] = (float) x;
			texture[2*index] = (float) u;
			texture[2*index+1] = (float) v;
			normals[offset++] = temp_normal.x;
			vertices[offset] = (float) y;
			normals[offset++] = temp_normal.y;
			vertices[offset] = (float) z;
			normals[offset++] = temp_normal.z;
			indices[base_index] = index++;
			++triangle;
			base_index = 3*triangle;
			y = mHeight;
			v = 0;
			x *= (mRadiusTop/mRadiusBase);
			z *= (mRadiusTop/mRadiusBase);
			vertices[offset] = (float) x;
			texture[2*index] = (float) u;
			texture[2*index+1] = (float) v;
			normals[offset++] = temp_normal.x;
			vertices[offset] = (float) y;
			normals[offset++] = temp_normal.y;
			vertices[offset] = (float) z;
			normals[offset++] = temp_normal.z;
			indices[base_index+2] = index - 3;
			indices[base_index+1] = index - 1;
			indices[base_index] = index++;
			++triangle;
		}

		int offset_holder = offset;
		//Add in the top center
		offset = top_center_index;
		vertices[offset] = 0.0f;
		normals[offset++] = UP.x;
		vertices[offset] = (float) mHeight;
		normals[offset++] = UP.y;
		vertices[offset] = 0.0f;
		normals[offset++] = UP.z;
		texture[12*mSideCount] = 0.5f;
		texture[12*mSideCount+1] = 0.5f;
		//Add in the base center
		offset = bottom_center_index;
		vertices[offset] = 0.0f;
		normals[offset++] = DOWN.x;
		vertices[offset] = 0.0f;
		normals[offset++] = DOWN.y;
		vertices[offset] = 0.0f;
		normals[offset++] = DOWN.z;
		texture[12*mSideCount+2] = 0.5f;
		texture[12*mSideCount+3] = 0.5f;

		offset = offset_holder;
		angle = (mSideCount % 2 == 0) ? angle = angle_delta/2.0 : 0;
		y = mHeight;
		for (int side = 0; side < mSideCount; ++side) {
			base_index = 3*triangle;
			x = mRadiusTop*Math.cos(angle);
			z = mRadiusTop*Math.sin(angle);
			u = Math.cos(angle);
			v = Math.sin(angle);
			//Handle the top
			vertices[offset] = (float) x;
			texture[2*index] = (float) u;
			texture[2*index+1] = (float) v;
			normals[offset++] = UP.x;
			vertices[offset] = (float) y;
			normals[offset++] = UP.y;
			vertices[offset] = (float) z;
			normals[offset++] = UP.z;

			indices[base_index+2] = vertex_count - 2;
			indices[base_index+1] = index;
			if (side == (mSideCount-1)) {
				indices[base_index] = 4*mSideCount;
			} else {
				indices[base_index] = ++index; //Moving to the next vertex
			}
			++triangle;
			angle += angle_delta;
		}

		angle = (mSideCount % 2 == 0) ? angle = angle_delta/2.0 : 0;
		y = 0;
		for (int side = 0; side < mSideCount; ++side) {
			base_index = 3*triangle;
			x = mRadiusBase*Math.cos(angle);
			z = mRadiusBase*Math.sin(angle);
			u = -Math.cos(angle);
			v = Math.sin(angle);
			//Handle the bottom
			vertices[offset] = (float) x;
			texture[2*index] = (float) u;
			texture[2*index+1] = (float) v;
			normals[offset++] = DOWN.x;
			vertices[offset] = (float) y;
			normals[offset++] = DOWN.y;
			vertices[offset] = (float) z;
			normals[offset++] = DOWN.z;

			indices[base_index+2] = ++index;
			indices[base_index+1] = vertex_count - 1;
			if (side == (mSideCount-1)) {
				indices[base_index] = 5*mSideCount;
			} else {
				indices[base_index] = indices[base_index+2] + 1;
			}
			Log.d(TAG, "Triangle["+triangle+"]: [" + indices[base_index] + ", " + indices[base_index+1] + ", " + indices[base_index+2]);
			angle += angle_delta;
			++triangle;
		}

		//Populate the colors
		for (int i = 0, j = 4*vertex_count; i < j; ++i) {
			colors[i] = 1.0f;
		}

		setData(vertices, normals, texture, colors, indices);
	}
}