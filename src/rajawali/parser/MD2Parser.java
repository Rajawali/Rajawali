package rajawali.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import rajawali.animation.mesh.AAnimationObject3D;
import rajawali.animation.mesh.IAnimationFrame;
import rajawali.animation.mesh.VertexAnimationFrame;
import rajawali.animation.mesh.VertexAnimationObject3D;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.TextureManager;
import rajawali.util.LittleEndianDataInputStream;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MD2Parser extends AParser implements IParser {
	private MD2Header mHeader;
	private String mCurrentTextureName;
	private Stack<IAnimationFrame> mFrames;
	private Bitmap mTexture;
	private VertexAnimationObject3D mObject;

	public MD2Parser(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, textureManager, resourceId);
	}

	@Override
	public AAnimationObject3D getParsedAnimationObject() {	
		return (AAnimationObject3D)mRootObject;
	}

	@Override
	public void parse() {
		InputStream fileIn = mResources.openRawResource(mResourceId);
		BufferedInputStream stream = new BufferedInputStream(fileIn);
		mObject = new VertexAnimationObject3D();

		mHeader = new MD2Header();

		try {
			mHeader.parse(stream);
			mFrames = new Stack<IAnimationFrame>();
			for(int i=0; i<mHeader.numFrames; ++i) 
				mFrames.add(new VertexAnimationFrame());
			byte[] bytes = new byte[mHeader.offsetEnd - 68];
			stream.read(bytes);
			getMaterials(stream, bytes);
			float[] texCoords = getTexCoords(stream, bytes);
			getFrames(stream, bytes);
			getTriangles(stream, bytes, texCoords);
			
			mObject.setFrames(mFrames);
			
			IAnimationFrame firstFrame = mFrames.get(0);
			mObject.getGeometry().setVertices(firstFrame.getGeometry().getVertices()); 
			mObject.getGeometry().setNormals(firstFrame.getGeometry().getNormals());
			mObject.setMaterial(new SimpleMaterial());
			mObject.setColor(0xffffffff);
			mObject.addTexture(mTextureManager.addTexture(mTexture));
		} catch (Exception e) {
			e.printStackTrace();
		}
		mObject.isContainer(false);
		mRootObject = mObject;
	}

	private void getMaterials(BufferedInputStream stream, byte[] bytes)
			throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(bytes,
				mHeader.offsetSkins - 68, bytes.length - mHeader.offsetSkins);
		LittleEndianDataInputStream is = new LittleEndianDataInputStream(ba);

		for (int i = 0; i < mHeader.numSkins; i++) {
			String skinPath = is.readString(64);

			skinPath = skinPath.substring(skinPath.lastIndexOf("/") + 1,
					skinPath.length());
			StringBuffer textureName = new StringBuffer(skinPath.toLowerCase());
			int dotIndex = textureName.lastIndexOf(".");
			if (dotIndex > -1)
				textureName =  new StringBuffer(textureName.substring(0, dotIndex));

			mCurrentTextureName = textureName.toString();
		}
		
		int identifier = mResources.getIdentifier(mCurrentTextureName, "drawable", mResources.getResourcePackageName(mResourceId));
		mTexture = BitmapFactory.decodeResource(mResources, identifier);
	}

	private float[] getTexCoords(BufferedInputStream stream, byte[] bytes)
			throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(bytes,
				mHeader.offsetTexCoord - 68, bytes.length
						- mHeader.offsetTexCoord);
		LittleEndianDataInputStream is = new LittleEndianDataInputStream(ba);

		float[] coords = new float[mHeader.numTexCoord * 2];
		
		int buffIndex = 0;
		for (int i = 0; i < mHeader.numTexCoord; i++) {
			buffIndex = i * 2;
			coords[buffIndex] = (float)is.readShort() / (float)mHeader.skinWidth;
			coords[buffIndex + 1] = (float)is.readShort() / (float)mHeader.skinHeight;
		}
		
		return coords;
	}

	private void getFrames(BufferedInputStream stream, byte[] bytes)
			throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(bytes,
				mHeader.offsetFrames - 68, bytes.length - mHeader.offsetFrames);
		LittleEndianDataInputStream is = new LittleEndianDataInputStream(ba);

		for (int i = 0; i < mHeader.numFrames; i++) {
			float scaleX = is.readFloat();
			float scaleY = is.readFloat();
			float scaleZ = is.readFloat();
			float translateX = is.readFloat();
			float translateY = is.readFloat();
			float translateZ = is.readFloat();
			String name = is.readString(16);
			IAnimationFrame frame = mFrames.get(i);
			
			if(name.indexOf("_") > 0)
				name = name.subSequence(0, name.lastIndexOf("_")).toString();
			else
				name = name.substring(0, 6).replaceAll("[0-9]{1,2}$", "");
			
			float vertices[] = new float[mHeader.numVerts * 3];
			int index = 0;
			
			for (int j = 0; j < mHeader.numVerts; j++) {
				vertices[index++] = scaleX * is.readUnsignedByte() + translateX;
				vertices[index++] = scaleY * is.readUnsignedByte() + translateY;
				vertices[index++] = scaleZ * is.readUnsignedByte() + translateZ;
				is.readUnsignedByte(); // int normalIndex
			}

			frame.getGeometry().setVertices(vertices);
		}
	}

	private void getTriangles(BufferedInputStream stream, byte[] bytes, float[] texCoords)
			throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(bytes,
				mHeader.offsetTriangles - 68, bytes.length
						- mHeader.offsetTriangles);
		LittleEndianDataInputStream is = new LittleEndianDataInputStream(ba);
		short[] indices = new short[mHeader.numTriangles*3];
		float[] reorderedTexCoords = new float[mHeader.numVerts * 2];
		int index = 0;

		for (int i = 0; i < mHeader.numTriangles; i++) {
			short fid1 = (short)is.readUnsignedShort();
			short fid2 = (short)is.readUnsignedShort();
			short fid3 = (short)is.readUnsignedShort();
			int uvid1 = is.readUnsignedShort();
			int uvid2 = is.readUnsignedShort();
			int uvid3 = is.readUnsignedShort();

			indices[index+2] = fid1;
			indices[index+1] = fid2;
			indices[index] = fid3;
			index += 3;

			reorderedTexCoords[(fid1 * 2)] = texCoords[uvid1 * 2];
			reorderedTexCoords[(fid1 * 2)+1] = texCoords[(uvid1 * 2)+1];
			reorderedTexCoords[(fid2 * 2)] = texCoords[uvid2 * 2];
			reorderedTexCoords[(fid2 * 2)+1] = texCoords[(uvid2 * 2)+1];
			reorderedTexCoords[(fid3 * 2)] = texCoords[uvid3 * 2];
			reorderedTexCoords[(fid3 * 2)+1] = texCoords[(uvid3 * 2)+1];
		}

		mObject.getGeometry().setTextureCoords(reorderedTexCoords);
		mObject.getGeometry().setIndices(indices);

		for(int i = 0; i < mHeader.numFrames; ++i) {
			VertexAnimationFrame frame = (VertexAnimationFrame)mFrames.get(i);
			frame.calculateNormals(indices);
		}
	}

	private class MD2Header {
		public int id;
		public int version;
		public int skinWidth;
		public int skinHeight;
		public int frameSize;
		public int numSkins;
		public int numVerts;
		public int numTexCoord;
		public int numTriangles;
		public int numGLCommands;
		public int numFrames;
		public int offsetSkins;
		public int offsetTexCoord;
		public int offsetTriangles;
		public int offsetFrames;
		public int offsetGLCommands;
		public int offsetEnd;

		public void parse(InputStream stream) throws Exception {
			id = readInt(stream);
			version = readInt(stream);

			if (id != 844121161 || version != 8)
				throw new Exception("This is not a valid MD2 file.");

			skinWidth = readInt(stream);
			skinHeight = readInt(stream);
			frameSize = readInt(stream);

			numSkins = readInt(stream);
			numVerts = readInt(stream);
			numTexCoord = readInt(stream);
			numTriangles = readInt(stream);
			numGLCommands = readInt(stream);
			numFrames = readInt(stream);

			offsetSkins = readInt(stream);
			offsetTexCoord = readInt(stream);
			offsetTriangles = readInt(stream);
			offsetFrames = readInt(stream);
			offsetGLCommands = readInt(stream);
			offsetEnd = readInt(stream);
		}
	}
}