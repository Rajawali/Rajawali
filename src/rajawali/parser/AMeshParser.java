package rajawali.parser;

import rajawali.BaseObject3D;
import rajawali.materials.TextureManager;
import rajawali.renderer.RajawaliRenderer;
import android.content.res.Resources;

public abstract class AMeshParser extends AParser implements IMeshParser {
	protected TextureManager mTextureManager;
	
	protected BaseObject3D mRootObject;

	public AMeshParser(RajawaliRenderer renderer, String fileOnSDCard) {
		super(renderer, fileOnSDCard);
		mRootObject = new BaseObject3D();
	}
	
	public AMeshParser(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, resourceId);
		mTextureManager = textureManager;
		mRootObject = new BaseObject3D();
	}
	
	public AMeshParser parse() throws ParsingException {
		super.parse();
		return this;
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
