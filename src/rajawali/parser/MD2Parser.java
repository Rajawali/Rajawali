package rajawali.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Stack;

import rajawali.animation.mesh.AAnimationObject3D;
import rajawali.animation.mesh.IAnimationFrame;
import rajawali.animation.mesh.VertexAnimationFrame;
import rajawali.animation.mesh.VertexAnimationObject3D;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.TextureManager;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.LittleEndianDataInputStream;
import rajawali.util.RajLog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MD2Parser extends AMeshParser implements IAnimatedMeshParser {

	private MD2Header mHeader;
	private String mCurrentTextureName;
	private Stack<IAnimationFrame> mFrames;
	private Bitmap mTexture;
	private VertexAnimationObject3D mObject;
	private float[][] mFrameVerts;
	private int[] mIndices;
	private float[] mTextureCoords;

	public MD2Parser(RajawaliRenderer renderer, String fileOnSDCard) {
		super(renderer, fileOnSDCard);
	}

	public MD2Parser(RajawaliRenderer renderer, int resourceId) {
		this(renderer.getContext().getResources(), renderer.getTextureManager(), resourceId);
	}

	public MD2Parser(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, textureManager, resourceId);
	}

	public AAnimationObject3D getParsedAnimationObject() {
		return (AAnimationObject3D) mRootObject;
	}

	public MD2Parser parse() throws ParsingException {
		super.parse();
		BufferedInputStream stream = null;
		if (mFile == null) {
			InputStream fileIn = mResources.openRawResource(mResourceId);
			stream = new BufferedInputStream(fileIn);
		} else {
			try {
				stream = new BufferedInputStream(new FileInputStream(mFile));
			} catch (FileNotFoundException e) {
				RajLog.e("[" + getClass().getCanonicalName() + "] Could not find file.");
				throw new ParsingException(e);
			}
		}

		mObject = new VertexAnimationObject3D();
		mObject.setFps(10);

		mHeader = new MD2Header();

		try {
			mHeader.parse(stream);
			mFrames = new Stack<IAnimationFrame>();

			for (int i = 0; i < mHeader.numFrames; ++i)
				mFrames.add(new VertexAnimationFrame());

			byte[] bytes = new byte[mHeader.offsetEnd - 68];
			stream.read(bytes);

			getMaterials(stream, bytes);
			float[] texCoords = getTexCoords(stream, bytes);

			getFrames(stream, bytes);
			getTriangles(stream, bytes, texCoords);

			mObject.setFrames(mFrames);

			IAnimationFrame firstFrame = mFrames.get(0);
			mObject.getGeometry().copyFromGeometry3D(firstFrame.getGeometry());
			mObject.setData(firstFrame.getGeometry().getVertexBufferInfo(), firstFrame.getGeometry()
					.getNormalBufferInfo(), mTextureCoords, null, mIndices);
			mObject.setMaterial(new DiffuseMaterial(true));
			mObject.setColor(0xffffffff);
			if (mTexture != null)
				mObject.addTexture(mTextureManager.addTexture(mTexture));
			stream.close();
		} catch (Exception e) {
			throw new ParsingException(e);
		}
		mObject.isContainer(false);
		mRootObject = mObject;

		return this;
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
			StringBuffer textureName = new StringBuffer(skinPath.toLowerCase(Locale.ENGLISH));
			mCurrentTextureName = textureName.toString().trim();
			if (mFile != null)
				continue;
			int dotIndex = textureName.lastIndexOf(".");
			if (dotIndex > -1)
				textureName = new StringBuffer(textureName.substring(0, dotIndex));

			mCurrentTextureName = textureName.toString();
		}
		is.close();
		if (mFile == null) {
			if (mCurrentTextureName == null) {
				RajLog.e("[" + getClass().getCanonicalName()
						+ "] No texture name was specified. No material will be created.");
				return;
			}
			int identifier = mResources.getIdentifier(mCurrentTextureName, "drawable",
					mResources.getResourcePackageName(mResourceId));
			mTexture = BitmapFactory.decodeResource(mResources, identifier);
		} else {
			try {
				String filePath = mFile.getParent() + File.separatorChar + mCurrentTextureName;
				mTexture = BitmapFactory.decodeFile(filePath);
			} catch (Exception e) {
				RajLog.e("[" + getClass().getCanonicalName() + "] Could not find file " + mCurrentTextureName);
				e.printStackTrace();
				return;
			}
		}
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
			coords[buffIndex] = (float) is.readShort() / (float) mHeader.skinWidth;
			coords[buffIndex + 1] = (float) is.readShort() / (float) mHeader.skinHeight;
		}
		is.close();
		return coords;
	}

	private void getFrames(BufferedInputStream stream, byte[] bytes)
			throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(bytes,
				mHeader.offsetFrames - 68, bytes.length - mHeader.offsetFrames);
		LittleEndianDataInputStream is = new LittleEndianDataInputStream(ba);

		mFrameVerts = new float[mHeader.numFrames][];

		for (int i = 0; i < mHeader.numFrames; i++) {
			float scaleX = is.readFloat();
			float scaleZ = is.readFloat();
			float scaleY = is.readFloat();
			float translateX = is.readFloat();
			float translateZ = is.readFloat();
			float translateY = is.readFloat();
			String name = is.readString(16);
			IAnimationFrame frame = mFrames.get(i);

			if (name.indexOf("_") > 0)
				name = name.subSequence(0, name.lastIndexOf("_")).toString();
			else
				name = name.trim().replaceAll("[0-9]{1,2}$", "");
			frame.setName(name);

			float vertices[] = new float[mHeader.numVerts * 3];
			int index = 0;

			for (int j = 0; j < mHeader.numVerts; j++) {
				vertices[index + 0] = scaleX * is.readUnsignedByte() + translateX;
				vertices[index + 2] = scaleZ * is.readUnsignedByte() + translateZ;
				vertices[index + 1] = scaleY * is.readUnsignedByte() + translateY;
				index += 3;
				is.readUnsignedByte();

			}
			mFrameVerts[i] = vertices;
		}
		is.close();
	}

	private void getTriangles(BufferedInputStream stream, byte[] bytes, float[] texCoords)
			throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(bytes,
				mHeader.offsetTriangles - 68, bytes.length
						- mHeader.offsetTriangles);
		LittleEndianDataInputStream is = new LittleEndianDataInputStream(ba);
		int[] indices = new int[mHeader.numTriangles * 3];
		int[] uvIndices = new int[mHeader.numTriangles * 3];
		int index = 0, uvIndex = 0;

		for (int i = 0; i < mHeader.numTriangles; i++) {
			indices[index++] = is.readShort();
			indices[index++] = is.readShort();
			indices[index++] = is.readShort();
			uvIndices[uvIndex++] = is.readShort();
			uvIndices[uvIndex++] = is.readShort();
			uvIndices[uvIndex++] = is.readShort();
		}
		is.close();

		short newVertexIndex = (short) mHeader.numVerts;
		int numIndices = indices.length;
		Stack<VertexIndices> changedIndices = new Stack<MD2Parser.VertexIndices>();

		for (int i = 0; i < numIndices; i++) {
			for (int j = i + 1; j < numIndices; j++)
			{
				if (indices[i] == indices[j] && uvIndices[i] != uvIndices[j])
				{
					changedIndices.add(new VertexIndices((short) j, indices[j], newVertexIndex));

					for (int k = j + 1; k < numIndices; k++) {
						if (indices[j] == indices[k] && uvIndices[j] == uvIndices[k]) {
							indices[k] = newVertexIndex;
						}
					}

					indices[j] = newVertexIndex;
					newVertexIndex++;
				}
			}
		}

		int[] cIndices = new int[changedIndices.size()];
		for (int j = 0; j < changedIndices.size(); j++)
			cIndices[j] = changedIndices.get(j).oldVertexIndex;

		float[] reorderedTexCoords = new float[(mHeader.numVerts + changedIndices.size()) * 2];

		for (int i = 0; i < indices.length; i++) {
			int fid = indices[i];
			int uvid = uvIndices[i];

			reorderedTexCoords[fid * 2] = texCoords[uvid * 2];
			reorderedTexCoords[fid * 2 + 1] = texCoords[uvid * 2 + 1];
		}

		mTextureCoords = reorderedTexCoords;
		mIndices = indices;

		for (int i = 0; i < mHeader.numFrames; ++i) {
			VertexAnimationFrame frame = (VertexAnimationFrame) mFrames.get(i);
			duplicateAndAppendVertices(i, cIndices);
			frame.getGeometry().setVertices(mFrameVerts[i]);
			frame.getGeometry().setNormals(frame.calculateNormals(indices));
			frame.getGeometry().createVertexAndNormalBuffersOnly();
		}
	}

	public void duplicateAndAppendVertices(int frameNumber, int[] indices) {
		float[] frameVerts = mFrameVerts[frameNumber];
		int offset = frameVerts.length;
		float[] newVerts = new float[offset + (indices.length * 3)];

		for (int i = 0; i < indices.length; i++) {
			int vi = offset + (i * 3);
			int ovi = indices[i] * 3;
			newVerts[vi] = frameVerts[ovi];
			newVerts[vi + 1] = frameVerts[ovi + 1];
			newVerts[vi + 2] = frameVerts[ovi + 2];
		}

		System.arraycopy(frameVerts, 0, newVerts, 0, offset);
		mFrameVerts[frameNumber] = newVerts;
	}

	private class VertexIndices {

		@SuppressWarnings("unused")
		public int index;
		public int oldVertexIndex;
		@SuppressWarnings("unused")
		public int newVertexIndex;

		public VertexIndices(int index, int oldVertexIndex, int newVertexIndex) {
			this.index = index;
			this.oldVertexIndex = oldVertexIndex;
			this.newVertexIndex = newVertexIndex;
		}
	}

	private class MD2Header {

		public int id;
		public int version;
		public int skinWidth;
		public int skinHeight;
		@SuppressWarnings("unused")
		public int frameSize;
		public int numSkins;
		public int numVerts;
		public int numTexCoord;
		public int numTriangles;
		@SuppressWarnings("unused")
		public int numGLCommands;
		public int numFrames;
		public int offsetSkins;
		public int offsetTexCoord;
		public int offsetTriangles;
		public int offsetFrames;
		@SuppressWarnings("unused")
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
