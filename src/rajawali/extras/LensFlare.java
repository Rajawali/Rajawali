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
package rajawali.extras;

import java.util.ArrayList;

import rajawali.materials.textures.ASingleTexture;
import rajawali.math.vector.Vector3;

/**
 * LensFlare effects class for adding lens flare to the renderer.
 * 
 * @author Andrew Jo
 */
public class LensFlare {	
	protected ArrayList<FlareInfo> mLensFlares; 
	protected Vector3 mPositionScreen;
	protected Vector3 mPosition;
	protected boolean mOccluded;
	
	public LensFlare(ASingleTexture texture, int size, double distance, Vector3 color) {
		mLensFlares = new ArrayList<FlareInfo>();
		mPositionScreen = new Vector3();
		mPosition = new Vector3();
		addLensFlare(texture, size, distance, color);
	}
	
	public void addLensFlare(ASingleTexture texture) {
		addLensFlare(texture, -1, 0, new Vector3(1, 1, 1));
	}
	
	public void addLensFlare(ASingleTexture texture, int size, double distance, Vector3 color) {
		addLensFlare(texture, size, distance, color, 1);
	}
	
	public void addLensFlare(ASingleTexture texture, int size, double distance, Vector3 color, double opacity) {
		distance = Math.min(distance, Math.max(0, distance));
		mLensFlares.add(new FlareInfo(texture, size, distance, new Vector3(), color, opacity));
	}
	
	public ArrayList<FlareInfo> getLensFlares() {
		return mLensFlares;
	}
	
	public Vector3 getPosition() {
		return mPosition;
	}
	
	public Vector3 getPositionScreen() {
		return mPositionScreen;
	}
	
	public void setPosition(double x, double y, double z) {
		mPosition.setAll(x, y, z);
	}
	
	/**
	 * Sets the world space coordinates of the lens flare.
	 * Make sure this is the same position as the light position for which to apply the lens flare effect.
	 * @param position
	 */
	public void setPosition(Vector3 position) {
		mPosition.setAll(position);
	}
	
	public void setPositionScreen(double x, double y, double z) {
		mPositionScreen.setAll(x, y, z);
	}
	
	public void setPositionScreen(Vector3 position) {
		mPositionScreen.setAll(position);
	}
	
	/**
	 * Updates individual flare element information.
	 * Override this method to achieve custom lens flare behavior.
	 */
	public void updateLensFlares() {
		double vecX = -mPositionScreen.x * 2;
		double vecY = -mPositionScreen.y * 2;
		
		for (int f = 0; f < mLensFlares.size(); f++) {
			FlareInfo flare = mLensFlares.get(f);
			flare.setScreenPosition(mPositionScreen.x + vecX * flare.getDistance(), mPositionScreen.y + vecY * flare.getDistance());
			flare.setWantedRotation(flare.getScreenPosition().x * Math.PI * 0.25);
			flare.setRotation(flare.getRotation() + (flare.getWantedRotation() - flare.getRotation()) * 0.25f);
		}
	}
	
	public class FlareInfo {
		protected ASingleTexture mTexture;
		protected int mSize;
		protected double mDistance;
		protected Vector3 mColor;
		protected Vector3 mScreenPosition;
		protected double mOpacity;
		protected double mScale;
		protected double mRotation;
		protected double mWantedRotation;
		
		public FlareInfo(ASingleTexture texture, int size, double distance, 
				Vector3 screenPosition, Vector3 color, double opacity) {
			mTexture = texture;
			mSize = size;
			mDistance = distance;
			mScreenPosition = screenPosition;
			mColor = color;
			mRotation = 1;
			mScale = 1;
			mOpacity = opacity;
			mWantedRotation = 0;
		}
		
		public Vector3 getColor() {
			return mColor;
		}
		
		public double getDistance() {
			return mDistance;
		}
		
		public double getOpacity() {
			return mOpacity;
		}
		
		public double getRotation() {
			return mRotation;
		}
		
		public double getScale() {
			return mScale;
		}
		
		public Vector3 getScreenPosition() {
			return mScreenPosition;
		}
		
		public int getSize() {
			return mSize;
		}
		
		public ASingleTexture getTexture() {
			return mTexture;
		}
		
		public double getWantedRotation() {
			return mWantedRotation;
		}
		
		public void setColor(double[] color) {
			mColor.x = color[0];
			mColor.y = color[1];
			mColor.z = color[2];
		}
		
		public void setColor(Vector3 color) {
			setColor(new double[] {color.x, color.y, color.z});
		}
		
		public void setDistance(double distance) {
			mDistance = distance;
		}
		
		public void setOpacity(double opacity) {
			mOpacity = opacity;
		}
		
		public void setRotation(double rotation) {
			mRotation = rotation;
		}
		
		public void setScale(double scale) {
			mScale = scale;
		}
		
		public void setScreenPosition(double x, double y) {
			mScreenPosition.x = x;
			mScreenPosition.y = y;
			mScreenPosition.z = 0;
		}
		
		public void setScreenPosition(double[] screenPosition) {
			mScreenPosition.x = screenPosition[0];
			mScreenPosition.y = screenPosition[1];
			mScreenPosition.z = screenPosition[2];
		}
		
		public void setScreenPosition(Vector3 screenPosition) {
			mScreenPosition.setAll(screenPosition);
		}
		
		public void setSize(int size) {
			mSize = size;
		}
		
		public void setTexture(ASingleTexture texture) {
			mTexture = texture;
		}

		public void setWantedRotation(double wantedRotation) {
			mWantedRotation = wantedRotation;
		}
	}
}