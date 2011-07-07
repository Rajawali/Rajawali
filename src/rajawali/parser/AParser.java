package rajawali.parser;

import rajawali.BaseObject3D;
import rajawali.materials.TextureManager;
import android.content.res.Resources;


public abstract class AParser implements IParser {
	protected TextureManager mTextureManager;
	protected Resources mResources;
	protected int mResourceId;
	
	protected BaseObject3D mRootObject;

	public AParser(Resources resources, TextureManager textureManager, int resourceId) {
		mTextureManager = textureManager;
		mResources = resources;
		mResourceId = resourceId;
		mRootObject = new BaseObject3D();
	}
	
	@Override
	public void parse() {
	}

	@Override
	public BaseObject3D getParsedObject() {
		return mRootObject;
	}
}
