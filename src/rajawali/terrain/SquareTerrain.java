package rajawali.terrain;

import android.graphics.Bitmap;
import android.graphics.Color;
import rajawali.materials.SimpleMaterial;
import rajawali.math.Plane;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Sphere;

/**
 * This is a generic Terrain class for Rajawali.
 * 
 * 
 * @author Ivan Battistella (info@fenicesoftware.com)
 */
public class SquareTerrain extends Terrain {

	private static final boolean debug = false;

	private float[][] mTerrain;
	private float[][] mTemperature;

	@SuppressWarnings("unused")
	private Vector3[][] mNormals; // for future use
	private int mDivisions;
	private float mXScale;
	private float mZScale;
	private float mOneOverXScale;
	private float mOneOverZScale;

	private float mMinH;
	private float mMaxH;
	private float mMinT;
	private float mMaxT;

	private Sphere mV0;
	private Sphere mV1;
	private Sphere mV2;
	private Sphere mNor;
	private Sphere mNorBase;

	/**
	 * Create Parameters object for calling TerrainGenerator Note: Bitmap can be recycled after calling TerrainGenerator
	 * 
	 * @param hMapBitmap
	 */
	public static Parameters createParameters(Bitmap hMapBitmap) {
		return new Parameters(hMapBitmap);
	}

	/**
	 * Terrain Parameters for TerrainGenerator
	 * 
	 * @author Ivan Battistella (info@fenicesoftware.com)
	 * 
	 */
	public static class Parameters {

		protected Bitmap heightMapBitmap;
		protected int divisions = 128;
		protected Vector3 scale = new Vector3(1f, 1f, 1f);
		protected float minTemp = 0f;

		protected float maxTemp = 100f;
		protected Bitmap colorMapBitmap = null;
		protected float textureMult = 1f;
		protected int basecolor = Color.BLUE;
		protected int middlecolor = Color.GREEN;
		protected int upcolor = Color.WHITE;

		// bmp, 256, new Vector3(1f,54f,1f), 0f, 100f, 8f, basecolor, middlecolor, upcolor
		/**
		 * Create SquareTerrain Parameters object for calling TerrainGenerator Note: Bitmap can be recycled after
		 * calling TerrainGenerator
		 * 
		 * @param hMapBitmap
		 */
		protected Parameters(Bitmap hMapBitmap) {
			heightMapBitmap = hMapBitmap;
		}

		/**
		 * Square grid dimension
		 * 
		 * @param value
		 */
		public void setDivisions(int value) {

			if (!((value != 0) && ((value & (value - 1)) == 0))) {
				throw new RuntimeException("Divisions must be value^2");
			}
			this.divisions = value;
		}

		/**
		 * Set the scale of Terrain Note: sy is multiplied by the height map (0:1)
		 * 
		 * @param sx
		 * @param sy
		 * @param sz
		 */
		public void setScale(float sx, float sy, float sz) {
			scale.setAll(sx, sy, sz);
		}

		/**
		 * Set the minimum range of Temperature
		 * 
		 * @param value
		 */
		public void setMinTemp(float value) {
			this.minTemp = value;
		}

		/**
		 * Set the maximum range of Temperature
		 * 
		 * @param value
		 */
		public void setMaxTemp(float value) {
			this.maxTemp = value;
		}

		/**
		 * set the color Map Bitmap used for interpolate Height Color for example for draw static shadow or stains on
		 * the ground (use Alpha) Note: Bitmap can be recycled after calling TerrainGenerator
		 * 
		 * @param value
		 */
		public void setColorMapBitmap(Bitmap value) {
			this.colorMapBitmap = value;
		}

		/**
		 * The Texture concentraction
		 * 
		 * @param value
		 */
		public void setTextureMult(float value) {
			this.textureMult = value;
		}

		/**
		 * The base color for Height Color computing (See setUpcolor or setMiddleColor)
		 * 
		 * @param value
		 */
		public void setBasecolor(int value) {
			this.basecolor = value;
		}

		/**
		 * The middle color for Height Color computing (See setBasecolor or setUpColor)
		 * 
		 * @param value
		 */
		public void setMiddleColor(int value) {
			this.middlecolor = value;
		}

		/**
		 * The up color for Height Color computing (See setBasecolor or setMiddleColor)
		 * 
		 * @param value
		 */
		public void setUpColor(int value) {
			this.upcolor = value;
		}

		public int getDivisions() {
			return this.divisions;
		}

		public Vector3 getScale() {
			return scale.clone();
		}

		public float getMinTemp() {
			return this.minTemp;
		}

		public float getMaxTemp(float value) {
			return this.maxTemp;
		}

		public Bitmap getHeightMapBitmap() {
			return this.heightMapBitmap;
		}

		public Bitmap getColorMapBitmap() {
			return this.colorMapBitmap;
		}

		public float getTextureMult() {
			return this.textureMult;
		}

		public int getBasecolor() {
			return this.basecolor;
		}

		public int getMiddleColor() {
			return this.middlecolor;
		}

		public int getUpColor() {
			return this.upcolor;
		}
	}

	/**
	 * Represents a Square Terrain centered at the center
	 * 
	 * @param divisions
	 *            Matrix dimension
	 * @param terrain
	 *            Altitude matrix
	 * @param temperature
	 *            Temperature matrix
	 * @param xScale
	 *            the scale of the x component
	 * @param zScale
	 *            the scale of the z component
	 */
	protected SquareTerrain(int divisions, float[][] terrain, Vector3[][] normals, float[][] temperature,
			float xScale, float zScale) {
		mDivisions = divisions;
		mTerrain = terrain;
		mTemperature = temperature;
		mNormals = normals;

		mXScale = xScale;
		mZScale = zScale;

		mOneOverXScale = 1 / xScale;
		mOneOverZScale = 1 / zScale;

		for (int i = 0; i <= divisions; ++i)
			for (int j = 0; j <= divisions; ++j)
			{
				if (terrain[i][j] < mMinH)
					mMinH = terrain[i][j];
				else if (terrain[i][j] > mMaxH)
					mMaxH = terrain[i][j];

				if (temperature[i][j] < mMinT)
					mMinT = temperature[i][j];
				else if (temperature[i][j] > mMaxT)
					mMaxT = temperature[i][j];

			}

		if (debug) {
			SimpleMaterial mat = new SimpleMaterial();
			mat.setUseSingleColor(true);
			mV0 = new Sphere(0.2f, 8, 8);
			mV1 = new Sphere(0.2f, 8, 8);
			mV2 = new Sphere(0.2f, 8, 8);
			mNor = new Sphere(0.2f, 8, 8);
			mNorBase = new Sphere(0.2f, 8, 8);

			mV0.setMaterial(mat);
			mV1.setMaterial(mat);
			mV2.setMaterial(mat);
			mNor.setMaterial(mat);
			mNorBase.setMaterial(mat);
			mNor.setColor(Color.MAGENTA);
			mNorBase.setColor(Color.BLACK);
			mV0.setColor(Color.RED);
			mV1.setColor(Color.GREEN);
			mV2.setColor(Color.BLUE);
			this.addChild(mV0);
			this.addChild(mV1);
			this.addChild(mV2);
			this.addChild(mNor);
			this.addChild(mNorBase);

		}
	}

	/**
	 * Terrain matrix dimension (Square)
	 * 
	 * @return terrain matrix dimension
	 */
	public int getDivisions() {
		return mDivisions;
	}

	/**
	 * returns the maximum extension in X
	 */
	public float getExtensionX() {
		return mDivisions * mXScale;
	}

	/**
	 * returns the maximum extension in Z
	 * 
	 * @return
	 */
	public float getExtensionZ() {
		return mDivisions * mZScale;
	}

	/**
	 * the minimum Altitude
	 * 
	 * @return the minimum Altitude
	 */
	public float getMinAltitude() {
		return mMinH;
	}

	/**
	 * the maximum Altitude
	 * 
	 * @return the maximum Altitude
	 */
	public float getMaxAltitude() {
		return mMaxH;
	}

	/**
	 * the minimum Temperature
	 * 
	 * @return the minimum Temperature
	 */
	public float getMinTemperature() {
		return mMinT;
	}

	/**
	 * the maximum Temperature
	 * 
	 * @return the maximum Temperature
	 */
	public float getMaxTemperature() {
		return mMaxT;
	}

	/**
	 * Terrain contains point
	 * 
	 * @return Terrain contains point
	 */
	public boolean contains(float x, float z) {

		float xx = ((x - mPosition.x) * mOneOverXScale + (float) mDivisions * 0.5f);
		float zz = ((z - mPosition.z) * mOneOverZScale + (float) mDivisions * 0.5f);

		if (xx >= 0f && zz >= 0f) {
			if (xx < mDivisions && zz < mDivisions) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return the percentage Altitude of Terrain matrix
	 */
	public float getPercAltitude(int i, int j) {
		float alt = mTerrain[i][j];
		return (alt - mMinH) / (mMaxH - mMinH);
	}

	private Plane mTmpPlane = new Plane();
	private Vector3 mTmpV0 = new Vector3();
	private Vector3 mTmpV1 = new Vector3();
	private Vector3 mTmpV2 = new Vector3();
	private Vector3 mTmpS = new Vector3();

	private Plane mTmpNorPlane = new Plane();
	private Vector3 mTmpNorV0 = new Vector3();
	private Vector3 mTmpNorV1 = new Vector3();
	private Vector3 mTmpNorV2 = new Vector3();
	private Vector3 mTmpNorS = new Vector3();

	// O--O--O--O--O
	// |A/|\D| /|\ |
	// |/B|C\|/ | \|
	// O--O--O--O--O
	// |\F|G/|\ | /|
	// |E\|/H| \|/ |
	// O--O--O--O--O
	// A
	// indices[xx++]=(i)+(j)*cols; indices[xx++]=(i+1)+(j)*cols; indices[xx++]=(i)+(j+1)*cols;
	// B
	// indices[xx++]=(i+1)+(j)*cols; indices[xx++]=(i+1)+(j+1)*cols; indices[xx++]=(i)+(j+1)*cols;
	// C
	// indices[xx++]=(i+1)+(j)*cols; indices[xx++]=(i+2)+(j+1)*cols; indices[xx++]=(i+1)+(j+1)*cols;
	// D
	// indices[xx++]=(i+1)+(j)*cols; indices[xx++]=(i+2)+(j)*cols; indices[xx++]=(i+2)+(j+1)*cols;
	// E
	// indices[xx++]=(i)+(j+1)*cols; indices[xx++]=(i+1)+(j+2)*cols; indices[xx++]=(i)+(j+2)*cols;
	// F
	// indices[xx++]=(i)+(j+1)*cols; indices[xx++]=(i+1)+(j+1)*cols; indices[xx++]=(i+1)+(j+2)*cols;
	// G
	// indices[xx++]=(i+1)+(j+1)*cols; indices[xx++]=(i+2)+(j+1)*cols; indices[xx++]=(i+1)+(j+2)*cols;
	// H
	// indices[xx++]=(i+2)+(j+1)*cols; indices[xx++]=(i+2)+(j+2)*cols; indices[xx++]=(i+1)+(j+2)*cols;

	/**
	 * the Altitude
	 * 
	 * @return the Altitude at point
	 */
	public float getAltitude(float x, float z) {
		return getInterpolateValue(mTerrain, x, z) + mPosition.y;
	}

	/**
	 * the Altitude
	 * 
	 * @return the Altitude at point
	 */
	private synchronized float getInterpolateValue(float[][] matrix, float x, float z) {

		float xx = ((x - mPosition.x) * mOneOverXScale + (float) mDivisions * 0.5f);
		float zz = ((z - mPosition.z) * mOneOverZScale + (float) mDivisions * 0.5f);

		double fx = Math.floor(xx);
		double fz = Math.floor(zz);

		int i = (int) fx;
		int j = (int) fz;

		if (i < 0) {
			i = 0;
			fx = i;
		} else if (i > mDivisions - 1) {
			i = mDivisions - 1;
			fx = mDivisions - 1;
		}

		if (j < 0) {
			j = 0;
			fz = 0;
		} else if (j > mDivisions - 1) {
			j = mDivisions - 1;
			fz = mDivisions - 1;
		}

		int flagcase = 0;

		if ((i % 2) == 0) {
			if ((j % 2) == 0) {
				// Triangle A/B flagcase=0
				mTmpV0.setAll(fx + 1f, matrix[i + 1][j], fz);
				mTmpV1.setAll(fx, matrix[i][j + 1], fz + 1f);
			}
			else {
				j--;
				fz -= 1f;
				// Triangle E/F flagcase=1
				mTmpV0.setAll(fx, matrix[i][j + 1], fz + 1f);
				mTmpV1.setAll(fx + 1f, matrix[i + 1][j + 2], fz + 2f);
				flagcase = 1;
			}
		}
		else {
			i--;
			fx -= 1f;
			if ((j % 2) == 0) {
				// Triangle C/D flagcase=2
				mTmpV0.setAll(fx + 1f, matrix[i + 1][j], fz);
				mTmpV1.setAll(fx + 2f, matrix[i + 2][j + 1], fz + 1f);
				flagcase = 2;
			}
			else {
				j--;
				fz -= 1f;
				// Triangle G/H flagcase=3
				mTmpV0.setAll(fx + 2f, matrix[i + 2][j + 1], fz + 1f);
				mTmpV1.setAll(fx + 1f, matrix[i + 1][j + 2], fz + 2f);
				flagcase = 3;
			}
		}

		mTmpV2.setAll(fx + 1f, 0f, fz + 1f);
		mTmpS.setAll(xx, 0f, zz);

		boolean useTriInt = intpoint_inside_trigon(mTmpS, mTmpV0, mTmpV1, mTmpV2);
		if (useTriInt) {
			switch (flagcase) {
			case 0: {
				// Triangle B flagcase=0
				mTmpV2.setAll(fx + 1f, matrix[i + 1][j + 1], fz + 1f);
			}
				break;
			case 1: {
				// Triangle F flagcase=1
				mTmpV2.setAll(fx + 1f, matrix[i + 1][j + 1], fz + 1f);
			}
				break;
			case 2: {
				// Triangle C flagcase=2
				mTmpV2.setAll(fx + 1f, matrix[i + 1][j + 1], fz + 1f);
			}
				break;
			case 3: {
				// Triangle G flagcase=3
				mTmpV2.setAll(fx + 1f, matrix[i + 1][j + 1], fz + 1f);
			}
				break;
			}
		} else {
			switch (flagcase) {
			case 0: {
				// Triangle A flagcase=0
				mTmpV2.setAll(fx, matrix[i][j], fz);
			}
				break;
			case 1: {
				// Triangle E flagcase=1
				mTmpV2.setAll(fx, matrix[i][j + 2], fz + 2f);
			}
				break;
			case 2: {
				// Triangle D flagcase=2
				mTmpV2.setAll(fx + 2f, matrix[i + 2][j], fz);
			}
				break;
			case 3: {
				// Triangle H flagcase=3
				mTmpV2.setAll(fx + 2f, matrix[i + 2][j + 2], fz + 2f);
			}
				break;
			}
		}

		mTmpPlane.set(mTmpV0, mTmpV1, mTmpV2);
		float alt;
		if (Math.abs(mTmpPlane.mNormal.y) < 0.00001) {
			alt = Math.min(mTmpV1.y, mTmpV0.y);
			alt = Math.min(alt, mTmpV2.y);
		}
		else {
			alt = (-mTmpPlane.mNormal.x * xx - mTmpPlane.mNormal.z * zz - mTmpPlane.d) / mTmpPlane.mNormal.y;
		}

		if (debug) {

			mV0.setPosition((mTmpV0.x - (float) mDivisions * 0.5f) * mXScale, mTmpV0.y,
					(mTmpV0.z - (float) mDivisions * 0.5f) * mZScale);
			mV1.setPosition((mTmpV1.x - (float) mDivisions * 0.5f) * mXScale, mTmpV1.y,
					(mTmpV1.z - (float) mDivisions * 0.5f) * mZScale);
			mV2.setPosition((mTmpV2.x - (float) mDivisions * 0.5f) * mXScale, mTmpV2.y,
					(mTmpV2.z - (float) mDivisions * 0.5f) * mZScale);

		}

		return alt;

	}

	private boolean intpoint_inside_trigon(Vector3 s, Vector3 a, Vector3 b, Vector3 c)
	{
		float as_x = s.x - a.x;
		float as_y = s.z - a.z;

		boolean s_ab = (b.x - a.x) * as_y - (b.z - a.z) * as_x > 0;

		if ((c.x - a.x) * as_y - (c.z - a.z) * as_x > 0 == s_ab)
			return false;

		if ((c.x - b.x) * (s.z - b.z) - (c.z - b.z) * (s.x - b.x) > 0 != s_ab)
			return false;

		return true;
	}

	/**
	 * the Normal at position
	 * 
	 * @return the point
	 */
	public synchronized void getNormalAt(float x, float z, Vector3 normal) {
		float xx = ((x - mPosition.x) * mOneOverXScale + (float) mDivisions * 0.5f);
		float zz = ((z - mPosition.z) * mOneOverZScale + (float) mDivisions * 0.5f);

		double fx = Math.floor(xx);
		double fz = Math.floor(zz);

		int i = (int) fx;
		int j = (int) fz;

		if (i < 0) {
			i = 0;
			fx = i;
		} else if (i > mDivisions - 1) {
			i = mDivisions - 1;
			fx = mDivisions - 1;
		}

		if (j < 0) {
			j = 0;
			fz = 0;
		} else if (j > mDivisions - 1) {
			j = mDivisions - 1;
			fz = mDivisions - 1;
		}

		int flagcase = 0;

		if ((i % 2) == 0) {
			if ((j % 2) == 0) {
				// Triangle A/B flagcase=0
				mTmpNorV0.setAll(fx + 1f, mTerrain[i + 1][j], fz);
				mTmpNorV1.setAll(fx, mTerrain[i][j + 1], fz + 1f);
			}
			else {
				j--;
				fz -= 1f;
				// Triangle E/F flagcase=1
				mTmpNorV0.setAll(fx, mTerrain[i][j + 1], fz + 1f);
				mTmpNorV1.setAll(fx + 1f, mTerrain[i + 1][j + 2], fz + 2f);
				flagcase = 1;
			}
		}
		else {
			i--;
			fx -= 1f;
			if ((j % 2) == 0) {
				// Triangle C/D flagcase=2
				mTmpNorV0.setAll(fx + 1f, mTerrain[i + 1][j], fz);
				mTmpNorV1.setAll(fx + 2f, mTerrain[i + 2][j + 1], fz + 1f);
				flagcase = 2;
			}
			else {
				j--;
				fz -= 1f;
				// Triangle G/H flagcase=3
				mTmpNorV0.setAll(fx + 2f, mTerrain[i + 2][j + 1], fz + 1f);
				mTmpNorV1.setAll(fx + 1f, mTerrain[i + 1][j + 2], fz + 2f);
				flagcase = 3;
			}
		}

		mTmpNorV2.setAll(fx + 1f, 0f, fz + 1f);
		mTmpNorS.setAll(xx, 0f, zz);

		boolean useTriInt = intpoint_inside_trigon(mTmpNorS, mTmpNorV0, mTmpNorV1, mTmpNorV2);
		if (useTriInt) {
			switch (flagcase) {
			case 0: {
				// Triangle B flagcase=0
				mTmpNorV2.setAll(fx + 1f, mTerrain[i + 1][j + 1], fz + 1f);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.mNormal);
			}
				break;
			case 1: {
				// Triangle F flagcase=1
				mTmpNorV2.setAll(fx + 1f, mTerrain[i + 1][j + 1], fz + 1f);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.mNormal);
			}
				break;
			case 2: {
				// Triangle C flagcase=2
				mTmpNorV2.setAll(fx + 1f, mTerrain[i + 1][j + 1], fz + 1f);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.mNormal);
				normal.multiply(-1f);
			}
				break;
			case 3: {
				// Triangle G flagcase=3
				mTmpNorV2.setAll(fx + 1f, mTerrain[i + 1][j + 1], fz + 1f);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.mNormal);
				normal.multiply(-1f);
			}
				break;
			}
		} else {
			switch (flagcase) {
			case 0: {
				// Triangle A flagcase=0
				mTmpNorV2.setAll(fx, mTerrain[i][j], fz);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.mNormal);
				normal.multiply(-1f);
			}
				break;
			case 1: {
				// Triangle E flagcase=1
				mTmpNorV2.setAll(fx, mTerrain[i][j + 2], fz + 2f);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.mNormal);
				normal.multiply(-1f);
			}
				break;
			case 2: {
				// Triangle D flagcase=2
				mTmpNorV2.setAll(fx + 2f, mTerrain[i + 2][j], fz);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.mNormal);
			}
				break;
			case 3: {
				// Triangle H flagcase=3
				mTmpNorV2.setAll(fx + 2f, mTerrain[i + 2][j + 2], fz + 2f);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.mNormal);
			}
				break;
			}
		}

		if (debug) {
			mNorBase.setPosition(x, getAltitude(x, z), z);
			mNor.setPosition(x + normal.x * 4f, getAltitude(x, z) + normal.y * 4f, z + normal.z * 4f);
		}
	}

	/**
	 * the Temperature at point
	 * 
	 * @return the Temperature
	 */
	public float getTemperature(float x, float z) {
		return getInterpolateValue(mTemperature, x, z);
	}

	public void setScale(float scale) {
		throw new RuntimeException("Not permitted for Terrain Object");
	}

	public void setScale(float scaleX, float scaleY, float scaleZ) {
		throw new RuntimeException("Not permitted for Terrain Object");
	}

	public void setScaleX(float scaleX) {
		throw new RuntimeException("Not permitted for Terrain Object");
	}

	public void setScaleY(float scaleY) {
		throw new RuntimeException("Not permitted for Terrain Object");
	}

	public void setScaleZ(float scaleZ) {
		throw new RuntimeException("Not permitted for Terrain Object");
	}
}
