package rajawali.extras;

import java.util.ArrayList;

import rajawali.materials.textures.ASingleTexture;
import rajawali.math.Vector3;

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
	
	public LensFlare(ASingleTexture texture, int size, float distance, Vector3 color) {
		mLensFlares = new ArrayList<FlareInfo>();
		mPositionScreen = new Vector3();
		mPosition = new Vector3();
		addLensFlare(texture, size, distance, color);
	}
	
	public void addLensFlare(ASingleTexture texture) {
		addLensFlare(texture, -1, 0, new Vector3(1, 1, 1));
	}
	
	public void addLensFlare(ASingleTexture texture, int size, float distance, Vector3 color) {
		addLensFlare(texture, size, distance, color, 1);
	}
	
	public void addLensFlare(ASingleTexture texture, int size, float distance, Vector3 color, float opacity) {
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
	
	public void setPosition(float x, float y, float z) {
		mPosition.setAll(x, y, z);
	}
	
	/**
	 * Sets the world space coordinates of the lens flare.
	 * Make sure this is the same position as the light position for which to apply the lens flare effect.
	 * @param position
	 */
	public void setPosition(Vector3 position) {
		mPosition.setAllFrom(position);
	}
	
	public void setPositionScreen(float x, float y, float z) {
		mPositionScreen.setAll(x, y, z);
	}
	
	public void setPositionScreen(Vector3 position) {
		mPositionScreen.setAllFrom(position);
	}
	
	/**
	 * Updates individual flare element information.
	 * Override this method to achieve custom lens flare behavior.
	 */
	public void updateLensFlares() {
		float vecX = -mPositionScreen.x * 2;
		float vecY = -mPositionScreen.y * 2;
		
		for (int f = 0; f < mLensFlares.size(); f++) {
			FlareInfo flare = mLensFlares.get(f);
			flare.setScreenPosition(mPositionScreen.x + vecX * flare.getDistance(), mPositionScreen.y + vecY * flare.getDistance());
			flare.setWantedRotation(flare.getScreenPosition().x * (float)Math.PI * 0.25f);
			flare.setRotation(flare.getRotation() + (flare.getWantedRotation() - flare.getRotation()) * 0.25f);
		}
	}
	
	public class FlareInfo {
		protected ASingleTexture mTexture;
		protected int mSize;
		protected float mDistance;
		protected Vector3 mColor;
		protected Vector3 mScreenPosition;
		protected float mOpacity;
		protected float mScale;
		protected float mRotation;
		protected float mWantedRotation;
		
		public FlareInfo(ASingleTexture texture, int size, float distance, 
				Vector3 screenPosition, Vector3 color, float opacity) {
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
		
		public float getDistance() {
			return mDistance;
		}
		
		public float getOpacity() {
			return mOpacity;
		}
		
		public float getRotation() {
			return mRotation;
		}
		
		public float getScale() {
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
		
		public float getWantedRotation() {
			return mWantedRotation;
		}
		
		public void setColor(float[] color) {
			mColor.x = color[0];
			mColor.y = color[1];
			mColor.z = color[2];
		}
		
		public void setColor(Vector3 color) {
			setColor(new float[] {color.x, color.y, color.z});
		}
		
		public void setDistance(float distance) {
			mDistance = distance;
		}
		
		public void setOpacity(float opacity) {
			mOpacity = opacity;
		}
		
		public void setRotation(float rotation) {
			mRotation = rotation;
		}
		
		public void setScale(float scale) {
			mScale = scale;
		}
		
		public void setScreenPosition(float x, float y) {
			mScreenPosition.x = x;
			mScreenPosition.y = y;
			mScreenPosition.z = 0;
		}
		
		public void setScreenPosition(float[] screenPosition) {
			mScreenPosition.x = screenPosition[0];
			mScreenPosition.y = screenPosition[1];
			mScreenPosition.z = screenPosition[2];
		}
		
		public void setScreenPosition(Vector3 screenPosition) {
			mScreenPosition.setAllFrom(screenPosition);
		}
		
		public void setSize(int size) {
			mSize = size;
		}
		
		public void setTexture(ASingleTexture texture) {
			mTexture = texture;
		}

		public void setWantedRotation(float wantedRotation) {
			mWantedRotation = wantedRotation;
		}
	}
}