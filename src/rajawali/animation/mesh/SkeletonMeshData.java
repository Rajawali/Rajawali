package rajawali.animation.mesh;

import rajawali.math.Vector2;
import rajawali.math.Vector3;

public class SkeletonMeshData {

	private String mTextureName;
	private int mNumVertices;
	private int mNumTriangles;
	private int mNumWeights;
	private int mMaxBoneWeightsPerVertex;
	private BoneVertex[] mBoneVertices;
	private BoneWeight[] mBoneWeights;
	private int[][] mTriangles;
	private float[] mVertices;
	private float[] mNormals;
	private int[] mIndices;
	private int[] mBoneIndices;
	private float[] mTextureCoordinates;
	private float[] mWeights;

	public static class BoneVertex {
		private Vector2 mTextureCoordinate;
		private int mWeightIndex;
		private int mNumWeights;
		private Vector3 mNormal = new Vector3();

		public BoneVertex()
		{
			mTextureCoordinate = new Vector2();
			mNormal = new Vector3();
		}

		public void setWeightIndex(int weightIndex)
		{
			mWeightIndex = weightIndex;
		}

		public int getWeightIndex()
		{
			return mWeightIndex;
		}

		public void setNumWeights(int numWeights)
		{
			mNumWeights = numWeights;
		}

		public int getNumWeights()
		{
			return mNumWeights;
		}

		public void setTextureCoordinate(float u, float v)
		{
			mTextureCoordinate.setAll(u, v);
		}

		public Vector2 getTextureCoordinate()
		{
			return mTextureCoordinate;
		}

		public void setNormal(float x, float y, float z)
		{
			mNormal.setAll(x, y, z);
		}

		public Vector3 getNormal()
		{
			return mNormal;
		}
	}

	public static class BoneWeight
	{

		private int mJointIndex;
		private float mWeightValue;
		private Vector3 mPosition;

		public BoneWeight()
		{
			mPosition = new Vector3();
		}

		public BoneWeight(int jointIndex, float weightValue)
		{
			this();
			mJointIndex = jointIndex;
			mWeightValue = weightValue;
		}

		public void setJointIndex(int jointIndex)
		{
			mJointIndex = jointIndex;
		}

		public int getJointIndex()
		{
			return mJointIndex;
		}

		public void setWeightValue(float weightValue)
		{
			mWeightValue = weightValue;
		}

		public float getWeightValue()
		{
			return mWeightValue;
		}

		public void setPosition(float x, float y, float z)
		{
			mPosition.setAll(x, y, z);
		}

		public Vector3 getPosition()
		{
			return mPosition;
		}
	}
	
	public BoneVertex getBoneVertexAt(int index)
	{
		return mBoneVertices[index];
	}
	
	public BoneWeight getWeightAt(int index)
	{
		return mBoneWeights[index];
	}
	
	public void setBoneIndex(int index, int jointIndex)
	{
		mBoneIndices[index] = jointIndex;
	}
	
	public void setWeightValue(int index, float value)
	{
		mWeights[index] = value;
	}
	
	public void setVertex(int index, float x, float y, float z)
	{
		mVertices[index] = x;
		mVertices[index + 1] = y;
		mVertices[index + 2] = z;
	}
	
	public void setTriangle(int index, int a, int b, int c)
	{
		mTriangles[index][0] = a;
		mTriangles[index][1] = b;
		mTriangles[index][2] = c;
	}
	
	public float getVertexComponent(int index)
	{
		return mVertices[index];
	}
	
	public void setIndex(int index, int indexValue)
	{
		mIndices[index] = indexValue;
	}
	
	public int[] getTriangle(int index)
	{
		return mTriangles[index];
	}
	
	public void setTextureCoordinate(int index, float u, float v)
	{
		mTextureCoordinates[index] = u;
		mTextureCoordinates[index + 1] = v;
	}
	
	public void addBoneVertex(int index, BoneVertex vertex)
	{
		mBoneVertices[index] = vertex;
	}
	
	public void addBoneWeight(int index, BoneWeight weight)
	{
		mBoneWeights[index] = weight;
	}

	public void addNumWeightsToTotal(int numWeights)
	{
		mNumWeights += numWeights;
	}
	
	public void addTriangle(int index, int a, int b, int c)
	{
		mTriangles[index] = new int[] { a, b, c };
	}
	
	public BoneVertex getBoneVertex(int index)
	{
		return mBoneVertices[index];
	}
	
	public void setNormalComponent(int index, float value)
	{
		mNormals[index] = value;
	}
	
	public BoneWeight getBoneWeight(int index)
	{
		return mBoneWeights[index];
	}
	
	/**
	 * @return the textureName
	 */
	public String getTextureName() {
		return mTextureName;
	}

	/**
	 * @param textureName
	 *            the textureName to set
	 */
	public void setTextureName(String textureName) {
		this.mTextureName = textureName;
	}

	/**
	 * @return the numVertices
	 */
	public int getNumVertices() {
		return mNumVertices;
	}

	/**
	 * @param numVertices
	 *            the numVertices to set
	 */
	public void setNumVertices(int numVertices) {
		this.mNumVertices = numVertices;
	}

	/**
	 * @return the numTriangles
	 */
	public int getNumTriangles() {
		return mNumTriangles;
	}

	/**
	 * @param numTriangles
	 *            the numTriangles to set
	 */
	public void setNumTriangles(int numTriangles) {
		this.mNumTriangles = numTriangles;
	}

	/**
	 * @return the numWeights
	 */
	public int getNumWeights() {
		return mNumWeights;
	}

	/**
	 * @param numWeights
	 *            the numWeights to set
	 */
	public void setNumWeights(int numWeights) {
		this.mNumWeights = numWeights;
	}

	/**
	 * @return the maxBoneWeightsPerVertex
	 */
	public int getMaxBoneWeightsPerVertex() {
		return mMaxBoneWeightsPerVertex;
	}

	/**
	 * @param maxBoneWeightsPerVertex
	 *            the maxBoneWeightsPerVertex to set
	 */
	public void setMaxBoneWeightsPerVertex(int maxBoneWeightsPerVertex) {
		this.mMaxBoneWeightsPerVertex = maxBoneWeightsPerVertex;
	}

	/**
	 * @return the boneVertices
	 */
	public BoneVertex[] getBoneVertices() {
		return mBoneVertices;
	}

	/**
	 * @param boneVertices
	 *            the boneVertices to set
	 */
	public void setBoneVertices(BoneVertex[] boneVertices) {
		this.mBoneVertices = boneVertices;
	}

	/**
	 * @return the boneWeights
	 */
	public BoneWeight[] getBoneWeights() {
		return mBoneWeights;
	}

	/**
	 * @param boneWeights
	 *            the boneWeights to set
	 */
	public void setBoneWeights(BoneWeight[] boneWeights) {
		this.mBoneWeights = boneWeights;
	}

	/**
	 * @return the triangles
	 */
	public int[][] getTriangles() {
		return mTriangles;
	}

	/**
	 * @param triangles
	 *            the triangles to set
	 */
	public void setTriangles(int[][] triangles) {
		this.mTriangles = triangles;
	}

	/**
	 * @return the vertices
	 */
	public float[] getVertices() {
		return mVertices;
	}

	/**
	 * @param vertices
	 *            the vertices to set
	 */
	public void setVertices(float[] vertices) {
		this.mVertices = vertices;
	}

	/**
	 * @return the normals
	 */
	public float[] getNormals() {
		return mNormals;
	}

	/**
	 * @param normals
	 *            the normals to set
	 */
	public void setNormals(float[] normals) {
		this.mNormals = normals;
	}

	/**
	 * @return the indices
	 */
	public int[] getIndices() {
		return mIndices;
	}

	/**
	 * @param indices
	 *            the indices to set
	 */
	public void setIndices(int[] indices) {
		this.mIndices = indices;
	}

	/**
	 * @return the boneIndices
	 */
	public int[] getBoneIndices() {
		return mBoneIndices;
	}

	/**
	 * @param boneIndices
	 *            the boneIndices to set
	 */
	public void setBoneIndices(int[] boneIndices) {
		this.mBoneIndices = boneIndices;
	}

	/**
	 * @return the textureCoordinates
	 */
	public float[] getTextureCoordinates() {
		return mTextureCoordinates;
	}

	/**
	 * @param textureCoordinates
	 *            the textureCoordinates to set
	 */
	public void setTextureCoordinates(float[] textureCoordinates) {
		this.mTextureCoordinates = textureCoordinates;
	}

	/**
	 * @return the weights
	 */
	public float[] getWeights() {
		return mWeights;
	}

	/**
	 * @param weights
	 *            the weights to set
	 */
	public void setWeights(float[] weights) {
		this.mWeights = weights;
	}
}
