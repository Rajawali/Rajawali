package rajawali.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import rajawali.BaseObject3D;
import rajawali.animation.mesh.AAnimationObject3D;
import rajawali.materials.TextureManager;
import rajawali.renderer.RajawaliRenderer;
import android.content.res.Resources;
import android.os.Environment;


public abstract class AParser implements IParser {
	protected TextureManager mTextureManager;
	protected Resources mResources;
	protected int mResourceId;
	protected String mFileOnSDCard;
	protected File mFile;
	
	protected BaseObject3D mRootObject;

	public AParser(RajawaliRenderer renderer, String fileOnSDCard) {
		this(renderer.getContext().getResources(), renderer.getTextureManager(), 0);
		mFileOnSDCard = fileOnSDCard;
	}
	
	public AParser(Resources resources, TextureManager textureManager, int resourceId) {
		mTextureManager = textureManager;
		mResources = resources;
		mResourceId = resourceId;
		mRootObject = new BaseObject3D();
	}
	
	public void parse() {
		if(mFileOnSDCard != null) {
			File sdcard = Environment.getExternalStorageDirectory();
			mFile = new File(sdcard, mFileOnSDCard);
		}
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
	
	protected String getOnlyFileName(String fileName) {
		String fName = new String(fileName);
		int indexOf = fName.lastIndexOf("\\");
		if(indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		indexOf = fName.lastIndexOf("/");
		if(indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		return fName.toLowerCase().replaceAll("\\s", "_");
	}
	
	protected String getFileNameWithoutExtension(String fileName) {
		String fName = fileName.substring(0, fileName.lastIndexOf("."));
		int indexOf = fName.lastIndexOf("\\");
		if(indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		indexOf = fName.lastIndexOf("/");
		if(indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		return fName.toLowerCase().replaceAll("\\s", "_");
	}
}
