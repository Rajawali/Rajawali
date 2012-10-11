package rajawali.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import rajawali.animation.mesh.AAnimationObject3D;
import rajawali.animation.mesh.BoneAnimationFrame;
import rajawali.animation.mesh.BoneAnimationObject3D;
import rajawali.animation.mesh.Skeleton;
import rajawali.animation.mesh.SkeletonJoint;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.TextureManager;
import rajawali.math.Number3D;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;

public class MD5Parser extends AParser implements IParser {
	private static final String MD5_VERSION = "MD5Version"; 
	private static final String COMMAND_LINE = "commandline";
	private static final String MD5MESH = "md5mesh";
	private static final String MD5ANIM = "md5anim";
	
	private static final String NUM_JOINTS = "numJoints";
	private static final String NUM_MESHES = "numMeshes";
	private static final String NUM_VERTS = "numverts";
	private static final String NUM_TRIS = "numtris";
	private static final String NUM_WEIGHTS = "numweights";
	private static final String NUM_FRAMES = "numFrames";
	
	private static final String JOINTS = "joints";
	private static final String MESH = "mesh";
	private static final String SHADER = "shader";
	private static final String VERT = "vert";
	private static final String TRI = "tri";
	private static final String WEIGHT = "weight";
	
	private static final String FRAME_RATE = "frameRate";
	private static final String NUM_ANIMATED_COMPONENTS = "numAnimatedComponents";
	private static final String HIERARCHY = "hierarchy";
	private static final String BOUNDS = "bounds";
	private static final String BASEFRAME = "baseframe";
	private static final String FRAME = "frame";
	
	private int mNumJoints;
	private int mNumMeshes;
	private int mNumFrames;
	private int mMeshIndex = 0;
	private int mFrameRate;
	private int mNumAnimatedComponents;
	
	private SkeletonJoint[] mJoints;
	private MD5Mesh[] mMeshes;
	private BoneAnimationFrame[] mFrames;
	private SkeletonJoint[] mBaseFrame;
	private float[] mFrameData;
	
	public float[][] mBindPoseMatrix;
	public float[][] mInverseBindPoseMatrix;
	
	public MD5Parser(RajawaliRenderer renderer, String fileOnSDCard) {
		super(renderer, fileOnSDCard);
	}
	
	public MD5Parser(RajawaliRenderer renderer, int resourceId) {
		this(renderer.getContext().getResources(), renderer.getTextureManager(), resourceId);
	}
	
	public MD5Parser(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, textureManager, resourceId);
	}
	
	@Override
	public AAnimationObject3D getParsedAnimationObject() {	
		return (AAnimationObject3D)mRootObject;
	}
	
	@Override
	public void parse() {
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
				e.printStackTrace();
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
			
			parseAnimFile();
			
			for(int i=0; i<mNumMeshes; i++) {
				((BoneAnimationObject3D)mRootObject.getChildAt(i)).setFrames(mFrames);
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				buffer.close();
			} catch(Exception ex) {}
		}
	}
	
	private void parseAnimFile() {
		String meshFile = mResources.getString(mResourceId);
		meshFile = meshFile.replace(MD5MESH, MD5ANIM);
		meshFile = meshFile.substring(meshFile.lastIndexOf("/") + 1, meshFile.length());
		int identifier = mResources.getIdentifier(meshFile, "raw", mResources.getResourcePackageName(mResourceId));
		
		if(identifier == 0) {
			RajLog.d("No md5anim file found (looking for 'raw/" + meshFile + "').");
			return;
		}		

		BufferedReader buffer = null;
		if(mFile == null) {
			InputStream fileIn = mResources.openRawResource(identifier);
			buffer = new BufferedReader(new InputStreamReader(fileIn));
		} else {
			try {
				buffer = new BufferedReader(new FileReader(mFile));
			} catch (FileNotFoundException e) {
				RajLog.e("["+getClass().getCanonicalName()+"] Could not find file.");
				e.printStackTrace();
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
				} else if(type.equalsIgnoreCase(COMMAND_LINE)) { 
				} else if(type.equalsIgnoreCase(NUM_JOINTS)) {
				} else if(type.equalsIgnoreCase(NUM_FRAMES)) {
					mNumFrames = Integer.parseInt(parts.nextToken());
					mFrames = new BoneAnimationFrame[mNumFrames];
				} else if(type.equalsIgnoreCase(FRAME_RATE)) {
					mFrameRate = Integer.parseInt(parts.nextToken());
					for(int i=0; i<mRootObject.getNumChildren(); ++i)
						((BoneAnimationObject3D)mRootObject.getChildAt(i)).setFps(mFrameRate);
				} else if(type.equalsIgnoreCase(NUM_ANIMATED_COMPONENTS)) {
					mNumAnimatedComponents = Integer.parseInt(parts.nextToken());
					mFrameData = new float[mNumAnimatedComponents];
				} else if(type.equalsIgnoreCase(HIERARCHY)) {
					parseHierarchy(buffer);
				} else if(type.equalsIgnoreCase(BOUNDS)) {
					parseBounds(buffer);
				} else if(type.equalsIgnoreCase(FRAME)) {
					parseFrame(Integer.parseInt(parts.nextToken()), buffer);
				} else if(type.equalsIgnoreCase(BASEFRAME)) {
					mBaseFrame = new SkeletonJoint[mNumJoints];
					parseBaseFrame(buffer);
				}
			}
			buffer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
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
	
	private void parseHierarchy(BufferedReader buffer) {
		try {
			String line;
			int index = 0;
			
			while((line = buffer.readLine()) != null) {
				line = line.replace("\t", " ");
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();

				if(line.indexOf('}') > -1) return;
				if(numTokens == 0) continue;
				
				SkeletonJoint joint = mJoints[index++];
				// discard name
				parts.nextToken();
				// discard parent index
				parts.nextToken();
				joint.setNumComp(Integer.parseInt(parts.nextToken()));
				joint.setFrameIndex(Integer.parseInt(parts.nextToken()));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseBounds(BufferedReader buffer) {
		try {
			String line;
			int index = 0;

			while((line = buffer.readLine()) != null) {
				line = line.replace("\t", " ");
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();
				if(line.indexOf('}') > -1) return;
				if(numTokens == 0) continue;
				
				BoneAnimationFrame frame = new BoneAnimationFrame();
				mFrames[index++] = frame;
				// discard (
				parts.nextToken();

				Number3D min = new Number3D(Float.parseFloat(parts.nextToken()), Float.parseFloat(parts.nextToken()), Float.parseFloat(parts.nextToken()));
				// discard )
				parts.nextToken();
				// discard (
				parts.nextToken();
			
				Number3D max = new Number3D(Float.parseFloat(parts.nextToken()), Float.parseFloat(parts.nextToken()), Float.parseFloat(parts.nextToken()));
				
				frame.setBounds(min, max);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseFrame(int frameIndex, BufferedReader buffer) {
		try {
			String line;
			int index = 0;
			BoneAnimationFrame frame = mFrames[frameIndex];
			frame.setFrameIndex(frameIndex);
			Skeleton skeleton = frame.getSkeleton();
			SkeletonJoint[] joints = new SkeletonJoint[mNumJoints];
			
			while((line = buffer.readLine()) != null) {
				line = line.replace("\t", " ");
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();
				if(line.indexOf('}') > -1) {
					skeleton.setJoints(joints);
					buildFrameSkeleton(skeleton);
					return;
				}
				if(numTokens == 0) continue;

				// -- position (x, y, z)
				mFrameData[index++] = Float.parseFloat(parts.nextToken());
				mFrameData[index++] = Float.parseFloat(parts.nextToken());
				mFrameData[index++] = Float.parseFloat(parts.nextToken());

				// -- orientation (x, y, z)
				mFrameData[index++] = Float.parseFloat(parts.nextToken());
				mFrameData[index++] = Float.parseFloat(parts.nextToken());
				mFrameData[index++] = Float.parseFloat(parts.nextToken());
				/*
				SkeletonJoint joint = new SkeletonJoint(mBaseFrame[index]);
				SkeletonJoint jointInfo = mJoints[index];
				
				joint.setParentIndex(jointInfo.getParentIndex());
				
				if((jointInfo.getNumComp() & 1) == 1) joint.getPosition().x = position.x;
				if((jointInfo.getNumComp() & 2) == 1) joint.getPosition().y = position.y;
				if((jointInfo.getNumComp() & 4) == 1) joint.getPosition().z = position.z;
				if((jointInfo.getNumComp() & 8) == 1) joint.getOrientation().x = orientation.x;
				if((jointInfo.getNumComp() & 16) == 1) joint.getOrientation().y = orientation.y;
				if((jointInfo.getNumComp() & 32) == 1) joint.getOrientation().z = orientation.z;
				joint.getOrientation().computeW();
				
				joints[index++] = joint;
				*/
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void buildFrameSkeleton(Skeleton skeleton) {
		for(int i=0; i<mNumJoints; ++i) {
			SkeletonJoint joint = new SkeletonJoint(mBaseFrame[i]);
			SkeletonJoint jointInfo = mJoints[i];
		
			joint.setParentIndex(jointInfo.getParentIndex());
			
			int j = 0;
			int frameIndex = jointInfo.getFrameIndex();
			
			if((jointInfo.getNumComp() & 1) == 1) joint.getPosition().x = mFrameData[frameIndex + j++];
			if((jointInfo.getNumComp() & 2) == 1) joint.getPosition().z = mFrameData[frameIndex + j++];
			if((jointInfo.getNumComp() & 4) == 1) joint.getPosition().y = mFrameData[frameIndex + j++];
			if((jointInfo.getNumComp() & 8) == 1) joint.getOrientation().x = mFrameData[frameIndex + j++];
			if((jointInfo.getNumComp() & 16) == 1) joint.getOrientation().z = mFrameData[frameIndex + j++];
			if((jointInfo.getNumComp() & 32) == 1) joint.getOrientation().y = mFrameData[frameIndex + j++];
			joint.getOrientation().computeW();
			/*
			joints[index++] = joint;
			
			SkeletonJoint joint = skeleton.getJoint(i);
			SkeletonJoint jointInfo = mJoints[i];
			if (joint.getParentIndex() >= 0 ) // Has a parent joint
	        {
	            SkeletonJoint parentJoint = skeleton.getJoint(jointInfo.getParentIndex());
	            Number3D rotPos = parentJoint.getOrientation().multiply(joint.getPosition());
	 
	            joint.getPosition().setAllFrom(Number3D.add(parentJoint.getPosition(), rotPos));
	            joint.getOrientation().multiply(parentJoint.getOrientation());
	            joint.getOrientation().normalize();
	        }*/
		}
	}
	
	private void parseBaseFrame(BufferedReader buffer) {
		try {
			String line;
			int index = 0;
			
			while((line = buffer.readLine()) != null) {
				line = line.replace("\t", " ");
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();
				if(line.indexOf('}') > -1) return;
				if(numTokens == 0) continue;
				
				SkeletonJoint joint = new SkeletonJoint();
				mBaseFrame[index++] = joint;
				
				// ignore "("
				parts.nextToken();
				float x = Float.parseFloat(parts.nextToken());
				float y = Float.parseFloat(parts.nextToken());
				float z = Float.parseFloat(parts.nextToken());
				joint.setPosition(x, z, y);
				
				// ignore ")"
				parts.nextToken();
				// ignore "("
				parts.nextToken();
				
				x = Float.parseFloat(parts.nextToken());
				y = Float.parseFloat(parts.nextToken());
				z = Float.parseFloat(parts.nextToken());
				joint.setOrientation(x, z, y);
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
		mBindPoseMatrix = new float[mNumJoints][];
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
			
			mBindPoseMatrix[i] = boneMatrix;
			mInverseBindPoseMatrix[i] = inverseBoneMatrix;
		}
	}
	
	private void createObjects() {
		mRootObject = new BoneAnimationObject3D();
		
		for(int i=0; i<mNumMeshes; ++i) {
			MD5Mesh mesh = mMeshes[i];
			BoneAnimationObject3D o = new BoneAnimationObject3D();
			o.setNumJoints(mNumJoints);
			o.setData(mesh.vertices, mesh.normals, mesh.texCoords, null, mesh.indices);
			o.setMD5Mesh(mesh);
			
			boolean hasTexture = mesh.shader != null && mesh.shader.length() > 0;
			
			DiffuseMaterial mat = new DiffuseMaterial();
			o.setMaterial(mat);
			if(!hasTexture) {
				mat.setUseColor(!hasTexture);
				o.setColor(0xff000000 + (int)(Math.random() * 0xffffff));
			} else {
				int identifier = mResources.getIdentifier(mesh.shader, "drawable", mResources.getResourcePackageName(mResourceId));
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
