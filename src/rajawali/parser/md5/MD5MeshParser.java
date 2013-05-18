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
import rajawali.animation.mesh.SkeletonMeshData;
import rajawali.animation.mesh.SkeletonMeshData.BoneVertex;
import rajawali.animation.mesh.SkeletonMeshData.BoneWeight;
import rajawali.materials.AMaterial;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.Texture;
import rajawali.materials.textures.TextureManager;
import rajawali.math.Vector3;
import rajawali.parser.AMeshParser;
import rajawali.parser.IAnimatedMeshParser;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.content.res.Resources;
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
	
	private SkeletonMeshData[] mMeshes;
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
					mMeshes = new SkeletonMeshData[mNumMeshes];
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
		} catch(TextureException tme) {
			try {
				buffer.close();
			} catch(Exception ex) {}
			throw new ParsingException(tme);
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
			SkeletonMeshData mesh = new SkeletonMeshData();
			
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
					String shader = parts.nextToken();
					shader = shader.replace("\"", "");
					mesh.setTextureName(shader);
					if(shader.length() == 0) continue;
					
					int lastDelim = shader.lastIndexOf("/");
					if(lastDelim == -1)
						lastDelim = shader.lastIndexOf("\\");
					if(lastDelim > -1)
						mesh.setTextureName(shader.substring(lastDelim + 1, shader.length()));
					
					int dot = shader.lastIndexOf(".");
					if(dot > -1)
						mesh.setTextureName(shader.substring(0, dot));
				} else if(type.equalsIgnoreCase(NUM_VERTS)) {
					mesh.setNumVertices(Integer.parseInt(parts.nextToken()));
					mesh.setBoneVertices(new SkeletonMeshData.BoneVertex[mesh.getNumVertices()]);
				} else if(type.equalsIgnoreCase(VERT)) {
					int index = Integer.parseInt(parts.nextToken());
					BoneVertex vert = new BoneVertex();

					// -- ignore '('
					parts.nextToken();
					vert.setTextureCoordinate(Float.parseFloat(parts.nextToken()), Float.parseFloat(parts.nextToken()));

					// -- ignore ')'
					parts.nextToken();
					vert.setWeightIndex(Integer.parseInt(parts.nextToken()));
					vert.setNumWeights(Integer.parseInt(parts.nextToken()));
					mesh.addNumWeightsToTotal(vert.getNumWeights());
					
					mesh.setMaxBoneWeightsPerVertex(Math.max(mesh.getMaxBoneWeightsPerVertex(), vert.getNumWeights()));// MAXIMUM 

					mesh.addBoneVertex(index, vert);
				} else if(type.equalsIgnoreCase(NUM_TRIS)) {
					mesh.setNumTriangles(Integer.parseInt(parts.nextToken()));
					mesh.setTriangles(new int[mesh.getNumTriangles()][]);
				} else if(type.equalsIgnoreCase(TRI)) {
					int index = Integer.parseInt(parts.nextToken());
					mesh.addTriangle(index, Integer.parseInt(parts.nextToken()), Integer.parseInt(parts.nextToken()), Integer.parseInt(parts.nextToken()));
				} else if(type.equalsIgnoreCase(NUM_WEIGHTS)) {
					mesh.setNumWeights(Integer.parseInt(parts.nextToken()));
					mesh.setBoneWeights(new SkeletonMeshData.BoneWeight[mesh.getNumWeights()]);
				} else if(type.equalsIgnoreCase(WEIGHT)) {
					int index = Integer.parseInt(parts.nextToken());
					
					SkeletonMeshData.BoneWeight weight = new SkeletonMeshData.BoneWeight();
					weight.setJointIndex(Integer.parseInt(parts.nextToken()));
					weight.setWeightValue(Float.parseFloat(parts.nextToken()));
					
					mesh.addBoneWeight(index, weight);
					
					// -- ignore '('
					parts.nextToken();
					float x = Float.parseFloat(parts.nextToken());
					float z = Float.parseFloat(parts.nextToken());
					float y = Float.parseFloat(parts.nextToken());
					weight.setPosition(x, y, z);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void buildMeshes() {
		for(int i=0; i<mNumMeshes; ++i) {
			int boneIndex = 0;
			SkeletonMeshData mesh = mMeshes[i];
			mesh.setVertices(new float[mesh.getNumVertices() * 3]);
			mesh.setBoneIndices(new int[mesh.getNumWeights()]);
			mesh.setWeights(new float[mesh.getNumWeights()]);
			mesh.setTextureCoordinates(new float[mesh.getNumVertices() * 2]);
			
			int numVerts = mesh.getNumVertices();
			
			for(int j=0; j<numVerts; ++j) {
				BoneVertex vert = mesh.getBoneVertexAt(j);
				Vector3 position = new Vector3();
				
				int numWeights = vert.getNumWeights();
				
				for(int k=0; k<numWeights; ++k) {
					BoneWeight weight = mesh.getWeightAt(vert.getWeightIndex() + k);
					SkeletonJoint joint = mJoints[weight.getJointIndex()];
					
					Vector3 rotPos = joint.getOrientation().multiply(weight.getPosition());
					
					Vector3 pos = Vector3.add(joint.getPosition(), rotPos);
					pos.multiply(weight.getWeightValue());
					position.add(pos);
					
					mesh.setBoneIndex(boneIndex, weight.getJointIndex());
					mesh.setWeightValue(boneIndex++, weight.getWeightValue());
				}
				
				int vertIndex = j * 3;
				mesh.setVertex(vertIndex, position.x, position.y, position.z);
				
				int uvIndex = j * 2;
				mesh.setTextureCoordinate(uvIndex, vert.getTextureCoordinate().getX(), vert.getTextureCoordinate().getY());
			}
		}
	}
	
	private void calculateNormals() {
		for(int i=0; i<mNumMeshes; ++i) {
			SkeletonMeshData mesh = mMeshes[i];
			int numTriangles = mesh.getNumTriangles();
			
			mesh.setIndices(new int[numTriangles * 3]);
			int index = 0;
						
			for(int j=0; j<numTriangles; ++j) {
				int[] triangle = mesh.getTriangle(j);
				int index0 = triangle[0];
				int index1 = triangle[1];
				int index2 = triangle[2];
				
				mesh.setIndex(index++, index0);
				mesh.setIndex(index++, index1);
				mesh.setIndex(index++, index2);
				
				int index03 = index0 * 3;
				int index13 = index1 * 3;
				int index23 = index2 * 3;
				
				Vector3 v0 = new Vector3(mesh.getVertexComponent(index03), mesh.getVertexComponent(index03 + 1), mesh.getVertexComponent(index03 + 2));
				Vector3 v1 = new Vector3(mesh.getVertexComponent(index13), mesh.getVertexComponent(index13 + 1), mesh.getVertexComponent(index13 + 2));
				Vector3 v2 = new Vector3(mesh.getVertexComponent(index23), mesh.getVertexComponent(index23 + 1), mesh.getVertexComponent(index23 + 2));
				
				Vector3 normal = Vector3.cross(Vector3.subtract(v2, v0), Vector3.subtract(v1, v0));
				
				mesh.getBoneVertex(index0).getNormal().add(normal);
				mesh.getBoneVertex(index1).getNormal().add(normal);
				mesh.getBoneVertex(index2).getNormal().add(normal);
			}
			
			int numVertices = mesh.getNumVertices();
			if(mesh.getNormals() == null) mesh.setNormals(new float[numVertices * 3]);
		
			for(int j=0; j<numVertices; ++j) {
				BoneVertex vert = mesh.getBoneVertex(j);
				Vector3 normal = vert.getNormal().clone();
				vert.getNormal().normalize();
				
				normal.normalize();
				
				int normIndex = j * 3;
				mesh.setNormalComponent(normIndex, normal.x);
				mesh.setNormalComponent(normIndex+1, normal.y);
				mesh.setNormalComponent(normIndex+2, normal.z);
				
				vert.getNormal().setAll(0, 0, 0);
				
				// -- bind-pose normal to joint-local
				//    so the animated normal can be computed faster
				int numWeights = vert.getNumWeights();
				for(int k=0; k<numWeights; ++k) {
					BoneWeight weight = mesh.getBoneWeight(vert.getWeightIndex() + k);
					SkeletonJoint joint = mJoints[weight.getJointIndex()];
					vert.getNormal().add(Vector3.multiply(joint.getOrientation().multiply(normal), weight.getWeightValue()));
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
			
			Vector3 jointPos = joint.getPosition();
			
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
	
	private void createObjects() throws TextureException, ParsingException {
		AnimationSkeleton root = new AnimationSkeleton();
		root.uBoneMatrix = mBindPoseMatrix;
		root.mInverseBindPoseMatrix = mInverseBindPoseMatrix;
		root.setJoints(mJoints);
		mRootObject = root;
		for(int i=0; i<mNumMeshes; ++i) {
			SkeletonMeshData mesh = mMeshes[i];
			BoneAnimationObject3D o = new BoneAnimationObject3D();
			o.setData(
					mesh.getVertices(), GLES20.GL_STREAM_DRAW,
					mesh.getNormals(), GLES20.GL_STREAM_DRAW,
					mesh.getTextureCoordinates(), GLES20.GL_STATIC_DRAW,
					null, GLES20.GL_STATIC_DRAW,
					mesh.getIndices(), GLES20.GL_STATIC_DRAW
					);			
			o.setSkeletonMeshData(mesh);
			o.setName("MD5Mesh_" + i);
			o.setSkeleton(mRootObject);
			
			boolean hasTexture = mesh.getTextureName() != null && mesh.getTextureName().length() > 0;
			
			DiffuseMaterial mat = new DiffuseMaterial(AMaterial.SKELETAL_ANIMATION);
			mat.setNumJoints(mNumJoints);
			mat.setMaxWeights(mesh.getMaxBoneWeightsPerVertex());
			o.setMaterial(mat);
			if(!hasTexture) {
				mat.setUseColor(!hasTexture);
				o.setColor(0xff000000 + (int)(Math.random() * 0xffffff));
			} else {
				int identifier = mResources.getIdentifier(mesh.getTextureName(), "drawable", mResources.getResourcePackageName(mResourceId));
				if(identifier == 0) {
					throw new ParsingException("Couldn't find texture " + mesh.getTextureName());
				}
				mat.addTexture(new Texture(identifier));
			}
			
			mRootObject.addChild(o);
		}
	}
}
