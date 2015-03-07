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
package rajawali.terrain;

import rajawali.materials.Material;
import rajawali.math.Plane;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Sphere;
import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * This is a generic Terrain class for Rajawali.
 * 
 * 
 * @author Ivan Battistella (info@fenicesoftware.com)
 */
public class SquareTerrain extends Terrain {

	private static final boolean debug = false;

	private double[][] mTerrain;
	private double[][] mTemperature;

	@SuppressWarnings("unused")
	private Vector3[][] mNormals; // for future use
	private int mDivisions;
	private double mXScale;
	private double mZScale;
	private double mOneOverXScale;
	private double mOneOverZScale;

	private double mMinH;
	private double mMaxH;
	private double mMinT;
	private double mMaxT;

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
		protected Vector3 scale = new Vector3(1, 1, 1);
		protected double minTemp = 0;

		protected double maxTemp = 100;
		protected Bitmap colorMapBitmap = null;
		protected double textureMult = 1;
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
		public void setScale(double sx, double sy, double sz) {
			scale.setAll(sx, sy, sz);
		}

		/**
		 * Set the minimum range of Temperature
		 * 
		 * @param value
		 */
		public void setMinTemp(double value) {
			this.minTemp = value;
		}

		/**
		 * Set the maximum range of Temperature
		 * 
		 * @param value
		 */
		public void setMaxTemp(double value) {
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
		public void setTextureMult(double value) {
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

		public double getMinTemp() {
			return this.minTemp;
		}

		public double getMaxTemp(double value) {
			return this.maxTemp;
		}

		public Bitmap getHeightMapBitmap() {
			return this.heightMapBitmap;
		}

		public Bitmap getColorMapBitmap() {
			return this.colorMapBitmap;
		}

		public double getTextureMult() {
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
	protected SquareTerrain(int divisions, double[][] terrain, Vector3[][] normals, double[][] temperature,
			double xScale, double zScale) {
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
			Material mat = new Material();
			// TODO mat.useSingleColor(true);
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
	public double getExtensionX() {
		return mDivisions * mXScale;
	}

	/**
	 * returns the maximum extension in Z
	 * 
	 * @return
	 */
	public double getExtensionZ() {
		return mDivisions * mZScale;
	}

	/**
	 * the minimum Altitude
	 * 
	 * @return the minimum Altitude
	 */
	public double getMinAltitude() {
		return mMinH;
	}

	/**
	 * the maximum Altitude
	 * 
	 * @return the maximum Altitude
	 */
	public double getMaxAltitude() {
		return mMaxH;
	}
	
	/**
     	 * Retrive the Terrain Heights Matrix
     	 * 
     	 */
    	public double[][] getHeights(){
        	return mTerrain;
    	}

	/**
	 * the minimum Temperature
	 * 
	 * @return the minimum Temperature
	 */
	public double getMinTemperature() {
		return mMinT;
	}

	/**
	 * the maximum Temperature
	 * 
	 * @return the maximum Temperature
	 */
	public double getMaxTemperature() {
		return mMaxT;
	}

	/**
	 * Terrain contains point
	 * 
	 * @return Terrain contains point
	 */
	public boolean contains(double x, double z) {

		double xx = ((x - mPosition.x) * mOneOverXScale + mDivisions * 0.5);
		double zz = ((z - mPosition.z) * mOneOverZScale + mDivisions * 0.5);

		if (xx >= 0 && zz >= 0) {
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
	public double getPercAltitude(int i, int j) {
		double alt = mTerrain[i][j];
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
	public double getAltitude(double x, double z) {
		return getInterpolateValue(mTerrain, x, z) + mPosition.y;
	}

	/**
	 * the Altitude
	 * 
	 * @return the Altitude at point
	 */
	private synchronized double getInterpolateValue(double[][] matrix, double x, double z) {

		double xx = ((x - mPosition.x) * mOneOverXScale + mDivisions * 0.5);
		double zz = ((z - mPosition.z) * mOneOverZScale + mDivisions * 0.5);

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
				mTmpV0.setAll(fx + 1, matrix[i + 1][j], fz);
				mTmpV1.setAll(fx, matrix[i][j + 1], fz + 1);
			}
			else {
				j--;
				fz -= 1f;
				// Triangle E/F flagcase=1
				mTmpV0.setAll(fx, matrix[i][j + 1], fz + 1);
				mTmpV1.setAll(fx + 1, matrix[i + 1][j + 2], fz + 2);
				flagcase = 1;
			}
		}
		else {
			i--;
			fx -= 1f;
			if ((j % 2) == 0) {
				// Triangle C/D flagcase=2
				mTmpV0.setAll(fx + 1, matrix[i + 1][j], fz);
				mTmpV1.setAll(fx + 2, matrix[i + 2][j + 1], fz + 1);
				flagcase = 2;
			}
			else {
				j--;
				fz -= 1f;
				// Triangle G/H flagcase=3
				mTmpV0.setAll(fx + 2, matrix[i + 2][j + 1], fz + 1);
				mTmpV1.setAll(fx + 1, matrix[i + 1][j + 2], fz + 2);
				flagcase = 3;
			}
		}

		mTmpV2.setAll(fx + 1, 0f, fz + 1);
		mTmpS.setAll(xx, 0f, zz);

		boolean useTriInt = intpoint_inside_trigon(mTmpS, mTmpV0, mTmpV1, mTmpV2);
		if (useTriInt) {
			switch (flagcase) {
			case 0: {
				// Triangle B flagcase=0
				mTmpV2.setAll(fx + 1, matrix[i + 1][j + 1], fz + 1);
			}
				break;
			case 1: {
				// Triangle F flagcase=1
				mTmpV2.setAll(fx + 1, matrix[i + 1][j + 1], fz + 1);
			}
				break;
			case 2: {
				// Triangle C flagcase=2
				mTmpV2.setAll(fx + 1, matrix[i + 1][j + 1], fz + 1);
			}
				break;
			case 3: {
				// Triangle G flagcase=3
				mTmpV2.setAll(fx + 1, matrix[i + 1][j + 1], fz + 1);
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
				mTmpV2.setAll(fx + 2, matrix[i + 2][j], fz);
			}
				break;
			case 3: {
				// Triangle H flagcase=3
				mTmpV2.setAll(fx + 2, matrix[i + 2][j + 2], fz + 2);
			}
				break;
			}
		}

		mTmpPlane.set(mTmpV0, mTmpV1, mTmpV2);
		double alt;
		if (Math.abs(mTmpPlane.getNormal().y) < 0.00001) {
			alt = Math.min(mTmpV1.y, mTmpV0.y);
			alt = Math.min(alt, mTmpV2.y);
		}
		else {
			alt = (-mTmpPlane.getNormal().x * xx - mTmpPlane.getNormal().z * zz - mTmpPlane.getD()) / mTmpPlane.getNormal().y;
		}

		if (debug) {

			mV0.setPosition((mTmpV0.x - mDivisions * 0.5) * mXScale, mTmpV0.y,
					(mTmpV0.z - mDivisions * 0.5) * mZScale);
			mV1.setPosition((mTmpV1.x - mDivisions * 0.5) * mXScale, mTmpV1.y,
					(mTmpV1.z - mDivisions * 0.5) * mZScale);
			mV2.setPosition((mTmpV2.x - mDivisions * 0.5) * mXScale, mTmpV2.y,
					(mTmpV2.z - mDivisions * 0.5) * mZScale);

		}

		return alt;

	}

	private boolean intpoint_inside_trigon(Vector3 s, Vector3 a, Vector3 b, Vector3 c)
	{
		double as_x = s.x - a.x;
		double as_y = s.z - a.z;

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
	public synchronized void getNormalAt(double x, double z, Vector3 normal) {
		double xx = ((x - mPosition.x) * mOneOverXScale + mDivisions * 0.5);
		double zz = ((z - mPosition.z) * mOneOverZScale + mDivisions * 0.5);

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
				mTmpNorV0.setAll(fx + 1, mTerrain[i + 1][j], fz);
				mTmpNorV1.setAll(fx, mTerrain[i][j + 1], fz + 1);
			}
			else {
				j--;
				fz -= 1;
				// Triangle E/F flagcase=1
				mTmpNorV0.setAll(fx, mTerrain[i][j + 1], fz + 1);
				mTmpNorV1.setAll(fx + 1, mTerrain[i + 1][j + 2], fz + 2);
				flagcase = 1;
			}
		}
		else {
			i--;
			fx -= 1;
			if ((j % 2) == 0) {
				// Triangle C/D flagcase=2
				mTmpNorV0.setAll(fx + 1, mTerrain[i + 1][j], fz);
				mTmpNorV1.setAll(fx + 2, mTerrain[i + 2][j + 1], fz + 1);
				flagcase = 2;
			}
			else {
				j--;
				fz -= 1;
				// Triangle G/H flagcase=3
				mTmpNorV0.setAll(fx + 2, mTerrain[i + 2][j + 1], fz + 1);
				mTmpNorV1.setAll(fx + 1, mTerrain[i + 1][j + 2], fz + 2);
				flagcase = 3;
			}
		}

		mTmpNorV2.setAll(fx + 1, 0f, fz + 1);
		mTmpNorS.setAll(xx, 0f, zz);

		boolean useTriInt = intpoint_inside_trigon(mTmpNorS, mTmpNorV0, mTmpNorV1, mTmpNorV2);
		if (useTriInt) {
			switch (flagcase) {
			case 0: {
				// Triangle B flagcase=0
				mTmpNorV2.setAll(fx + 1, mTerrain[i + 1][j + 1], fz + 1);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.getNormal());
			}
				break;
			case 1: {
				// Triangle F flagcase=1
				mTmpNorV2.setAll(fx + 1, mTerrain[i + 1][j + 1], fz + 1);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.getNormal());
			}
				break;
			case 2: {
				// Triangle C flagcase=2
				mTmpNorV2.setAll(fx + 1, mTerrain[i + 1][j + 1], fz + 1);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.getNormal());
				normal.multiply(-1);
			}
				break;
			case 3: {
				// Triangle G flagcase=3
				mTmpNorV2.setAll(fx + 1, mTerrain[i + 1][j + 1], fz + 1);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.getNormal());
				normal.multiply(-1);
			}
				break;
			}
		} else {
			switch (flagcase) {
			case 0: {
				// Triangle A flagcase=0
				mTmpNorV2.setAll(fx, mTerrain[i][j], fz);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.getNormal());
				normal.multiply(-1);
			}
				break;
			case 1: {
				// Triangle E flagcase=1
				mTmpNorV2.setAll(fx, mTerrain[i][j + 2], fz + 2);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.getNormal());
				normal.multiply(-1);
			}
				break;
			case 2: {
				// Triangle D flagcase=2
				mTmpNorV2.setAll(fx + 2, mTerrain[i + 2][j], fz);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.getNormal());
			}
				break;
			case 3: {
				// Triangle H flagcase=3
				mTmpNorV2.setAll(fx + 2, mTerrain[i + 2][j + 2], fz + 2);
				mTmpNorPlane.set(mTmpNorV0, mTmpNorV1, mTmpNorV2);
				normal.setAll(mTmpNorPlane.getNormal());
			}
				break;
			}
		}

		if (debug) {
			mNorBase.setPosition(x, getAltitude(x, z), z);
			mNor.setPosition(x + normal.x * 4, getAltitude(x, z) + normal.y * 4, z + normal.z * 4);
		}
	}

	/**
	 * the Temperature at point
	 * 
	 * @return the Temperature
	 */
	public double getTemperature(double x, double z) {
		return getInterpolateValue(mTemperature, x, z);
	}

	public void setScale(double scale) {
		throw new RuntimeException("Not permitted for Terrain Object");
	}

	public void setScale(double scaleX, double scaleY, double scaleZ) {
		throw new RuntimeException("Not permitted for Terrain Object");
	}

	public void setScaleX(double scaleX) {
		throw new RuntimeException("Not permitted for Terrain Object");
	}

	public void setScaleY(double scaleY) {
		throw new RuntimeException("Not permitted for Terrain Object");
	}

	public void setScaleZ(double scaleZ) {
		throw new RuntimeException("Not permitted for Terrain Object");
	}
}
