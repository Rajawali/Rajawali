package rajawali.parser;

import java.io.IOException;
import java.io.InputStream;

import rajawali.BaseObject3D;
import rajawali.animation.mesh.AAnimationObject3D;
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
	
	public AAnimationObject3D getParsedAnimationObject() {
		return null;
	}
	
	protected String readString(InputStream stream) throws IOException {
        String result = new String();
        byte inByte;
        while ((inByte = (byte) stream.read()) != 0)
                result += (char) inByte;
        return result;
	}
	
	protected int readInt(InputStream stream) throws IOException {
	        return stream.read() | (stream.read() << 8) | (stream.read() << 16)
	                        | (stream.read() << 24);
	}
	
	protected int readShort(InputStream stream) throws IOException {
	        return (stream.read() | (stream.read() << 8));
	}
	
	protected float readFloat(InputStream stream) throws IOException {
	        return Float.intBitsToFloat(readInt(stream));
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
