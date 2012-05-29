package rajawali.animation.mesh;

import rajawali.Geometry3D;
import rajawali.math.Number3D;

public class VertexAnimationFrame implements IAnimationFrame {
	protected Geometry3D mGeometry;
	protected String mName;
	protected float[] mVertices;
	
	public VertexAnimationFrame() {
		mGeometry = new Geometry3D();
	}
	
	public Geometry3D getGeometry() {
		return mGeometry;
	}

	public void setGeometry(Geometry3D geometry) {
		mGeometry = geometry;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}
	
	public float[] calculateNormals(int[] indices) {
		float[] vertices = new float[mGeometry.getVertices().capacity()];
		mGeometry.getVertices().get(vertices).position(0);
		float[] faceNormals = new float[indices.length];
		float[] vertNormals = new float[vertices.length];

		int numIndices = indices.length;
		int numVertices = vertices.length;
		int id1, id2, id3, vid1, vid2, vid3;
		Number3D v1 = new Number3D();
		Number3D v2 = new Number3D();
		Number3D v3 = new Number3D();
		Number3D normal = new Number3D();
		
		// -- calculate face normals
		for(int i=0; i<numIndices; i+=3) {
			id1 = indices[i];
			id2 = indices[i+1];
			id3 = indices[i+2];
			
			vid1 = id1 * 3;
			vid2 = id2 * 3;
			vid3 = id3 * 3;
			
			v1.setAll(vertices[vid1], vertices[vid1+1], vertices[vid1+2]);
			v2.setAll(vertices[vid2], vertices[vid2+1], vertices[vid2+2]);
			v3.setAll(vertices[vid3], vertices[vid3+1], vertices[vid3+2]);
			
			Number3D vector1 = Number3D.subtract(v2, v1);
            Number3D vector2 = Number3D.subtract(v3, v1);
            
            normal = Number3D.cross(vector1, vector2);
            normal.normalize();
            
            faceNormals[i] = normal.x;
            faceNormals[i+1] = normal.y;
            faceNormals[i+2] = normal.z;

		}
		// -- calculate vertex normals
		
		Number3D vertexNormal = new Number3D();
		
		for(int i=0; i<numVertices; i+=3) {
			int vIndex = i / 3;
			
			vertexNormal.setAll(0, 0, 0);			
			
			for(int j=0; j<numIndices; j+=3)
			{
				id1 = indices[j];
				id2 = indices[j+1];
				id3 = indices[j+2];
				
				if(id1 == vIndex || id2 == vIndex || id3 == vIndex) {
					vertexNormal.add(faceNormals[j], faceNormals[j+1], faceNormals[j+2]);
				}
			}
			vertexNormal.normalize();
			vertNormals[i] = -vertexNormal.x;
			vertNormals[i+1] = vertexNormal.y;
			vertNormals[i+2] = -vertexNormal.z;
		}
		//mGeometry.setNormals(vertNormals);
		faceNormals = null;
		v1 = null;
		v2 = null;
		v3 = null;
		return vertNormals;
	}
}
