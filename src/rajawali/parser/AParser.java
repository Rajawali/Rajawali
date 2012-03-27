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
	
	public void parse() {
	}

	public BaseObject3D getParsedObject() {
		return mRootObject;
	}
	
	protected class MaterialDef {
		public String name;
		public int ambientColor;
		public int diffuseColor;
		public int specularColor;
		public float specularCoefficient;
		public float alpha;
		public String ambientTexture;
		public String diffuseTexture;
		public String specularColorTexture;
		public String specularHightlightTexture;
		public String alphaTexture;
		public String bumpTexture;
	}
}
