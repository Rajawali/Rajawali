package rajawali.parser.md5;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import rajawali.animation.mesh.AAnimationObject3D;
import rajawali.animation.mesh.AnimationSkeleton;
import rajawali.animation.mesh.BoneAnimationObject3D;
import rajawali.animation.mesh.SkeletonJoint;
import rajawali.materials.AMaterial;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.TextureManager;
import rajawali.math.Number3D;
import rajawali.parser.AMeshParser;
import rajawali.parser.IAnimatedMeshParser;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class MD5MeshParser extends AMeshParser implements IAnimatedMeshParser {
	private static final String MD5_VERSION = "MD5Version"; 
	private static final String COMMAND_LINE = "commandline";
	
	private static final String NUM_JOINTS = "numJoints";
	private static final String NUM_MESHES = "numMeshes";
	private static final String NUM_VERTS = "numverts";
	private static final String NUM_TRIS = "numtris";
	private static final String NUM_WEIGHTS = "numweights";
	
	private static final String JOINTS = "joints";
	private static final String MESH = "mesh";
	private static final String SHADER = "shader";
	private static final String VERT = "vert";
	private static final String TRI = "tri";
	private static final String WEIGHT = "weight";
	
	private int mNumJoints;
	private int mNumMeshes;
	private int mMeshIndex = 0;
	
	private MD5Mesh[] mMeshes;
	private SkeletonJoint[] mJoints;
	
	public float[] mBindPoseMatrix;
	public float[][] mInverseBindPoseMatrix;
	
	public MD5MeshParser(RajawaliRenderer renderer, String fileOnSDCard) {
		super(renderer, fileOnSDCard);
	}
	
	public MD5MeshParser(RajawaliRenderer renderer, int resourceId) {
		this(renderer.getContext().getResources(), renderer.getTextureManager(), resourceId);
	}
	
	public MD5MeshParser(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, textureManager, resourceId);
	}
	
	public AAnimationObject3D getParsedAnimationObject() {	
		return (AAnimationObject3D)mRootObject;
	}
	
	@Override
	public MD5MeshParser parse() throws ParsingException {
		super.parse();
		
		BufferedReader buffer = null;
		if(mFile == null) {
			InputStream fileIn = mResources.openRawResource(mResourceId);
			buffer = new BufferedReader(new InputStreamReader(fileIn));
		} else {
			try {
				buffer = new BufferedReader(new FileReader(mFile));
			} catch (FileNotFoundException e) {
				RajLog.e("["+getClass().getCanonicalName()+"] Could not find file.");
				throw new ParsingException(e);
			}
		}
		String line;
		
		try {
			while((line = buffer.readLine()) != null) {
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();
				
				if(numTokens == 0)
					continue;
				String type = parts.nextToken();
				
				if(type.equalsIgnoreCase(MD5_VERSION)) {
					RajLog.d("MD5 Version: " + parts.nextToken());
				} else if(type.equalsIgnoreCase(COMMAND_LINE)) { 
				} else if(type.equalsIgnoreCase(NUM_JOINTS)) {
					mNumJoints = Integer.parseInt(parts.nextToken());
					mJoints = new SkeletonJoint[mNumJoints];
				} else if(type.equalsIgnoreCase(NUM_MESHES)) {
					mNumMeshes = Integer.parseInt(parts.nextToken());
					mMeshes = new MD5Mesh[mNumMeshes];
				} else if(type.equalsIgnoreCase(JOINTS)) {
					parseJoints(buffer);
				} else if(type.equals(MESH)) {
					parseMesh(buffer);
				}
			}
			buffer.close();
			
			buildBindPose();
			buildMeshes();
			calculateNormals();
			createObjects();
		} catch (IOException e) {
			try {
				buffer.close();
			} catch(Exception ex) {}
			throw new ParsingException(e);
		}
		
		return this;
	}
	
	private void parseJoints(BufferedReader buffer) {
		try {
			String line;
			int count = 0;
			
			while((line = buffer.readLine()) != null) {
				SkeletonJoint joint = new SkeletonJoint();
				if(line.length() == 0)
					continue;

				if(line.indexOf('}') > -1) {
					return;
				}
				line = line.replace('\t', ' ');
				
				// -- Bone Name
				int offset = line.lastIndexOf('"');
				joint.setName(line.substring(line.indexOf('"') + 1, offset));

				// -- Parent Index
				offset += 2;
				joint.setParentIndex(Integer.parseInt(line.substring(offset, line.indexOf(' ', offset))));
				
				// -- position
				offset = line.indexOf(')');
				String[] p = line.substring(line.indexOf('(') + 2, offset).split(" ");
				joint.setPosition(Float.parseFloat(p[0]), Float.parseFloat(p[2]), Float.parseFloat(p[1]));
				
				// -- orientation
				p = line.substring(line.indexOf('(', offset) + 2, line.lastIndexOf(')')).split(" ");
				joint.setOrientation(Float.parseFloat(p[0]), Float.parseFloat(p[2]), Float.parseFloat(p[1]));
				joint.getOrientation().computeW();
				
				mJoints[count++] = joint;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseMesh(BufferedReader buffer) {
		try {
			String line;
			MD5Mesh mesh = new MD5Mesh();
			
			while((line = buffer.readLine()) != null) {
				line = line.replace("\t", "");
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();
				if(line.indexOf('}') > -1) {
					mMeshes[mMeshIndex++] = mesh;
					return;
				}
				if(numTokens == 0 || line.indexOf('}') > -1)
					continue;

				String type = parts.nextToken();
				
				if(type.equalsIgnoreCase(SHADER)) {
					mesh.shader = parts.nextToken();
					mesh.shader = mesh.shader.replace("\"", "");
					if(mesh.shader.length() == 0) continue;
					
					int lastDelim = mesh.shader.lastIndexOf("/");
					if(lastDelim == -1)
						lastDelim = mesh.shader.lastIndexOf("\\");
					if(lastDelim > -1)
						mesh.shader = mesh.shader.substring(lastDelim + 1, mesh.shader.length());
					
					int dot = mesh.shader.lastIndexOf(".");
					if(dot > -1)
						mesh.shader = mesh.shader.substring(0, dot);
				} else if(type.equalsIgnoreCase(NUM_VERTS)) {
					mesh.numVerts = Integer.parseInt(parts.nextToken());
					mesh.verts = new MD5Vert[mesh.numVerts];
				} else if(type.equalsIgnoreCase(VERT)) {
					int index = Integer.parseInt(parts.nextToken());
					MD5Vert vert = new MD5Vert();

					// -- ignore '('
					parts.nextToken();
					vert.texU = Float.parseFloat(parts.nextToken());
					vert.texV = Float.parseFloat(parts.nextToken());

					// -- ignore ')'
					parts.nextToken();
					vert.weightIndex = Integer.parseInt(parts.nextToken());
					vert.weightElem = Integer.parseInt(parts.nextToken());
					mesh.numWeightElems += vert.weightElem;
					
					mesh.maxNumWeights = Math.max(mesh.maxNumWeights, vert.weightElem);// MAXIMUM 

					mesh.verts[index] = vert;
				} else if(type.equalsIgnoreCase(NUM_TRIS)) {
					mesh.numTris = Integer.parseInt(parts.nextToken());
					mesh.tris = new int[mesh.numTris][];
				} else if(type.equalsIgnoreCase(TRI)) {
					int index = Integer.parseInt(parts.nextToken());
					mesh.tris[index] = new int[] { Integer.parseInt(parts.nextToken()), Integer.parseInt(parts.nextToken()), Integer.parseInt(parts.nextToken()) };
				} else if(type.equalsIgnoreCase(NUM_WEIGHTS)) {
					mesh.numWeights = Integer.parseInt(parts.nextToken());
					mesh.weights = new MD5Weight[mesh.numWeights];
				} else if(type.equalsIgnoreCase(WEIGHT)) {
					int index = Integer.parseInt(parts.nextToken());
					
					MD5Weight weight = new MD5Weight();
					weight.jointIndex = Integer.parseInt(parts.nextToken());
					weight.weightValue = Float.parseFloat(parts.nextToken());
					
					mesh.weights[index] = weight;
					
					// -- ignore '('
					parts.nextToken();
					weight.position.x = Float.parseFloat(parts.nextToken());
					weight.position.z = Float.parseFloat(parts.nextToken());
					weight.position.y = Float.parseFloat(parts.nextToken());
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void buildMeshes() {
		for(int i=0; i<mNumMeshes; ++i) {
			int boneIndex = 0;
			MD5Mesh mesh = mMeshes[i];
			mesh.vertices = new float[mesh.numVerts * 3];
			mesh.boneIndices = new int[mesh.numWeightElems];
			mesh.boneWeights = new float[mesh.numWeightElems]; 
			mesh.texCoords = new float[mesh.numVerts * 2];
			
			for(int j=0; j<mesh.numVerts; ++j) {
				MD5Vert vert = mesh.verts[j];
				Number3D position = new Number3D();
				
				for(int k=0; k<vert.weightElem; ++k) {
					MD5Weight weight = mesh.weights[vert.weightIndex + k];
					SkeletonJoint joint = mJoints[weight.jointIndex];
					
					Number3D rotPos = joint.getOrientation().multiply(weight.position);
					
					Number3D pos = Number3D.add(joint.getPosition(), rotPos);
					pos.multiply(weight.weightValue);
					position.add(pos);
					
					mesh.boneIndices[boneIndex] = weight.jointIndex;
					mesh.boneWeights[boneIndex++] = weight.weightValue;
				}
				
				int vertIndex = j * 3;
				mesh.vertices[vertIndex] = position.x;
				mesh.vertices[vertIndex+1] = position.y;
				mesh.vertices[vertIndex+2] = position.z;
				
				int uvIndex = j * 2;
				mesh.texCoords[uvIndex] = vert.texU;
				mesh.texCoords[uvIndex + 1] = vert.texV;
			}
		}
	}
	
	private void calculateNormals() {
		for(int i=0; i<mNumMeshes; ++i) {
			MD5Mesh mesh = mMeshes[i];
			mesh.indices = new int[mesh.numTris * 3];
			int index = 0;
			
			for(int j=0; j<mesh.numTris; ++j) {
				int index0 = mesh.tris[j][0];
				int index1 = mesh.tris[j][1];
				int index2 = mesh.tris[j][2];
				
				mesh.indices[index++] = index0;
				mesh.indices[index++] = index1;
				mesh.indices[index++] = index2;
				
				int index03 = index0 * 3;
				int index13 = index1 * 3;
				int index23 = index2 * 3;
				
				Number3D v0 = new Number3D(mesh.vertices[index03], mesh.vertices[index03 + 1], mesh.vertices[index03 + 2]);
				Number3D v1 = new Number3D(mesh.vertices[index13], mesh.vertices[index13 + 1], mesh.vertices[index13 + 2]);
				Number3D v2 = new Number3D(mesh.vertices[index23], mesh.vertices[index23 + 1], mesh.vertices[index23 + 2]);
				
				Number3D normal = Number3D.cross(Number3D.subtract(v2, v0), Number3D.subtract(v1, v0));
				
				mesh.verts[index0].normal.add(normal);
				mesh.verts[index1].normal.add(normal);
				mesh.verts[index2].normal.add(normal);
			}
			
			if(mesh.normals == null) mesh.normals = new float[mesh.numVerts * 3];
			
			for(int j=0; j<mesh.numVerts; ++j) {
				MD5Vert vert = mesh.verts[j];
				Number3D normal = vert.normal.clone();
				vert.normal.normalize();
				
				normal.normalize();
				
				int normIndex = j * 3;
				mesh.normals[normIndex] = normal.x;
				mesh.normals[normIndex+1] = normal.y;
				mesh.normals[normIndex+2] = normal.z;
				
				vert.normal.setAll(0, 0, 0);
				
				// -- bind-pose normal to joint-local
				//    so the animated normal can be computed faster
				for(int k=0; k<vert.weightElem; ++k) {
					MD5Weight weight = mesh.weights[vert.weightIndex + k];
					SkeletonJoint joint = mJoints[weight.jointIndex];
					vert.normal.add(Number3D.multiply(joint.getOrientation().multiply(normal), weight.weightValue));
				}
			}
		}
	}
	
	private void buildBindPose() {
		mBindPoseMatrix = new float[mNumJoints*16];
		mInverseBindPoseMatrix = new float[mNumJoints][];
		
		for(int i=0; i<mNumJoints; ++i) {
			SkeletonJoint joint = mJoints[i];
			
			float[] boneTranslation = new float[16];
			float[] boneRotation = new float[16];
			float[] boneMatrix = new float[16];
			float[] inverseBoneMatrix = new float[16];
			
			Matrix.setIdentityM(boneTranslation, 0);
			Matrix.setIdentityM(boneRotation, 0);
			
			Number3D jointPos = joint.getPosition();
			
			Matrix.translateM(boneTranslation, 0, jointPos.x, jointPos.y, jointPos.z);
			joint.getOrientation().toRotationMatrix(boneRotation);
			
			Matrix.multiplyMM(boneMatrix, 0, boneTranslation, 0, boneRotation, 0);
			Matrix.invertM(inverseBoneMatrix, 0, boneMatrix, 0);
			
			for(int j=0; j<16; j++){
				mBindPoseMatrix[i + j] = boneMatrix[j];
			}
			mInverseBindPoseMatrix[i] = inverseBoneMatrix;
		}
	}
	
	private void createObjects() {
		AnimationSkeleton root = new AnimationSkeleton();
		root.uBoneMatrix = mBindPoseMatrix;
		root.mInverseBindPoseMatrix = mInverseBindPoseMatrix;
		root.setJoints(mJoints);
		mRootObject = root;
		for(int i=0; i<mNumMeshes; ++i) {
			MD5Mesh mesh = mMeshes[i];
			BoneAnimationObject3D o = new BoneAnimationObject3D();
			o.setData(
					mesh.vertices, GLES20.GL_STREAM_DRAW,
					mesh.normals, GLES20.GL_STREAM_DRAW,
					mesh.texCoords, GLES20.GL_STATIC_DRAW,
					null, GLES20.GL_STATIC_DRAW,
					mesh.indices, GLES20.GL_STATIC_DRAW
					);			
			o.setMD5Mesh(mesh);
			o.setName("MD5Mesh_" + i);
			o.setSkeleton(mRootObject);
			
			boolean hasTexture = mesh.shader != null && mesh.shader.length() > 0;
			
			DiffuseMaterial mat = new DiffuseMaterial(AMaterial.SKELETAL_ANIMATION);
			mat.setNumJoints(mNumJoints);
			mat.setMaxWeights(mesh.maxNumWeights);
			o.setMaterial(mat);
			if(!hasTexture) {
				mat.setUseColor(!hasTexture);
				o.setColor(0xff000000 + (int)(Math.random() * 0xffffff));
			} else {
				int identifier = mResources.getIdentifier(mesh.shader, "drawable", mResources.getResourcePackageName(mResourceId));
				if(identifier == 0) {
					RajLog.e("Couldn't find texture " + mesh.shader);
					break;
				}
				o.addTexture(mTextureManager.addTexture(BitmapFactory.decodeResource(mResources, identifier)));
			}
			
			mRootObject.addChild(o);
		}
	}
	
	public class MD5Mesh {
		public String shader;
		public int numVerts;
		public int numTris;
		public int numWeights;
		public int numWeightElems = 0;
		public int maxNumWeights=0;
		public MD5Vert[] verts;
		public int[][] tris;
		public MD5Weight[] weights;
		public float[] vertices;
		public float[] normals;
		public int[] indices;
		public int[] boneIndices;
		public float[] boneWeights;
		public float[] texCoords;
	}
	
	public class MD5Vert {
		public float texU;
		public float texV;
		public int weightIndex;
		public int weightElem;	
		public Number3D normal = new Number3D();
		
		public String toString() {
			return texU + ", " + texV + ", " + weightIndex + ", " + weightElem;
		}
	}
	
	public class MD5Weight {
		public int jointIndex;
		public float weightValue;
		public Number3D position = new Number3D();
		
		public String toString() {
			return jointIndex + ", " + weightValue + ", " + position.toString();
		}
	}
}
