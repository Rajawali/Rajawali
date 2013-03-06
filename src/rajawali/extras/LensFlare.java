package rajawali.extras;

import java.util.ArrayList;

import rajawali.materials.TextureInfo;
import rajawali.math.Number3D;

/**
 * LensFlare effects class for adding lens flare to the renderer.
 * 
 * @author Andrew Jo
 */
public class LensFlare {	
	protected ArrayList<FlareInfo> mLensFlares; 
	protected Number3D mPositionScreen;
	protected Number3D mPosition;
	protected boolean mOccluded;
	
	public LensFlare(TextureInfo texture, int size, float distance, Number3D color) {
		mLensFlares = new ArrayList<FlareInfo>();
		mPositionScreen = new Number3D();
		mPosition = new Number3D();
		addLensFlare(texture, size, distance, color);
	}
	
	public void addLensFlare(TextureInfo texture) {
		addLensFlare(texture, -1, 0, new Number3D(1, 1, 1));
	}
	
	public void addLensFlare(TextureInfo texture, int size, float distance, Number3D color) {
		addLensFlare(texture, size, distance, color, 1);
	}
	
	public void addLensFlare(TextureInfo texture, int size, float distance, Number3D color, float opacity) {
		distance = Math.min(distance, Math.max(0, distance));
		mLensFlares.add(new FlareInfo(texture, size, distance, new Number3D(), color, opacity));
	}
	
	public ArrayList<FlareInfo> getLensFlares() {
		return mLensFlares;
	}
	
	public Number3D getPosition() {
		return mPosition;
	}
	
	public Number3D getPositionScreen() {
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
	public void setPosition(Number3D position) {
		mPosition.setAllFrom(position);
	}
	
	public void setPositionScreen(float x, float y, float z) {
		mPositionScreen.setAll(x, y, z);
	}
	
	public void setPositionScreen(Number3D position) {
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
		protected TextureInfo mTexture;
		protected int mSize;
		protected float mDistance;
		protected Number3D mColor;
		protected Number3D mScreenPosition;
		protected float mOpacity;
		protected float mScale;
		protected float mRotation;
		protected float mWantedRotation;
		
		public FlareInfo(TextureInfo texture, int size, float distance, 
				Number3D screenPosition, Number3D color, float opacity) {
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
		
		public Number3D getColor() {
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
		
		public Number3D getScreenPosition() {
			return mScreenPosition;
		}
		
		public int getSize() {
			return mSize;
		}
		
		public TextureInfo getTexture() {
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
		
		public void setColor(Number3D color) {
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
		
		public void setScreenPosition(Number3D screenPosition) {
			mScreenPosition.setAllFrom(screenPosition);
		}
		
		public void setSize(int size) {
			mSize = size;
		}
		
		public void setTexture(TextureInfo texture) {
			mTexture = texture;
		}

		public void setWantedRotation(float wantedRotation) {
			mWantedRotation = wantedRotation;
		}
	}
}