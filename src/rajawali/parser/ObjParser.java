package rajawali.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import rajawali.BaseObject3D;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.TextureManager;
import rajawali.wallpaper.Wallpaper;

import android.content.res.Resources;
import android.util.Log;


public class ObjParser extends AParser {
    protected final String VERTEX = "v";
    protected final String FACE = "f";
    protected final String TEXCOORD = "vt";
    protected final String NORMAL = "vn";
    protected final String OBJECT = "o";
    protected final String MATERIAL_LIB = "mtllib";
    protected final String USE_MATERIAL = "usemtl";
    protected final String NEW_MATERIAL = "newmtl";
    protected final String DIFFUSE_COLOR = "Kd";
    protected final String DIFFUSE_TEX_MAP = "map_Kd";
	
	public ObjParser(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, textureManager, resourceId);
	}
	
	@Override
	public void parse() {
		InputStream fileIn = mResources.openRawResource(mResourceId);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(fileIn));
		String line;
		ObjIndexData currObjIndexData = null;
		ArrayList<ObjIndexData> objIndices = new ArrayList<ObjIndexData>();
				
		ArrayList<Float> vertices = new ArrayList<Float>();
		ArrayList<Float> texCoords = new ArrayList<Float>();
		ArrayList<Float> normals = new ArrayList<Float>();
		
		try {
			while((line = buffer.readLine()) != null) {
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();
				
				if(numTokens == 0)
					continue;
				String type = parts.nextToken();
				
				if(type.equals(VERTEX)) {
					vertices.add(Float.parseFloat(parts.nextToken()));
					vertices.add(Float.parseFloat(parts.nextToken()));
					vertices.add(Float.parseFloat(parts.nextToken()));
				} else if(type.equals(FACE)) {
					if(numTokens != 4) {
						Log.d(Wallpaper.TAG, line);
						throw new RuntimeException("Quads are not allowed. Make sure the model contains only triangles.");
					} else {
                        boolean emptyVt = line.indexOf("//") > -1;
                        if(emptyVt) line = line.replace("//", "/");
                        
                        parts = new StringTokenizer(line);
                        
                        parts.nextToken();
                        StringTokenizer subParts = new StringTokenizer(parts.nextToken(), "/");
                        int partLength = subParts.countTokens();
                        
                        boolean hasuv = partLength >= 2 && !emptyVt;
                        boolean hasn = partLength == 3 || (partLength == 2 && emptyVt);
                        
                        for (int i = 1; i < 4; i++) {
                        	if(i > 1)
                        		subParts = new StringTokenizer(parts.nextToken(), "/");

                            currObjIndexData.vertexIndices.add((short) (Short.parseShort(subParts.nextToken()) - 1));
                            if (hasuv)
                                currObjIndexData.texCoordIndices.add((short) (Short.parseShort(subParts.nextToken()) - 1));
                            if (hasn)
                                currObjIndexData.normalIndices.add((short) (Short.parseShort(subParts.nextToken()) - 1));
                        }
					}
				} else if(type.equals(TEXCOORD)) {
					texCoords.add(Float.parseFloat(parts.nextToken()));
                    texCoords.add(Float.parseFloat(parts.nextToken()) * -1f);
				} else if(type.equals(NORMAL)) {
					normals.add(Float.parseFloat(parts.nextToken()));
                    normals.add(Float.parseFloat(parts.nextToken()));
                    normals.add(Float.parseFloat(parts.nextToken()));
				} else if(type.equals(OBJECT)) {
					String objName = parts.hasMoreTokens() ? parts.nextToken() : "";

					Log.d(Wallpaper.TAG, "Parsing object: " + objName);
					currObjIndexData = new ObjIndexData(new BaseObject3D(objName));
					objIndices.add(currObjIndexData);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int numObjects = objIndices.size();
		
		for(int j=0; j<numObjects; ++j) {
			ObjIndexData oid = objIndices.get(j);
			
			int i;
			float[] aVertices 	= new float[oid.vertexIndices.size() * 3];
			float[] aTexCoords 	= new float[oid.texCoordIndices.size() * 2];
			float[] aNormals 	= new float[oid.normalIndices.size() * 3];
			short[] aIndices 	= new short[oid.vertexIndices.size()];
			
			for(i=0; i<oid.vertexIndices.size(); ++i) {
				short faceIndex = (short)(oid.vertexIndices.get(i) * 3);
				int vertexIndex = i * 3;
				aVertices[vertexIndex] = vertices.get(faceIndex);
				aVertices[vertexIndex+1] = vertices.get(faceIndex + 1);
				aVertices[vertexIndex+2] = vertices.get(faceIndex + 2);
				aIndices[i] = (short) i; 
			}
			for(i=0; i<oid.texCoordIndices.size(); ++i) {
				short texCoordIndex = (short)(oid.texCoordIndices.get(i) * 2);
				int ti = i * 2;
				aTexCoords[ti] = texCoords.get(texCoordIndex);
				aTexCoords[ti + 1] = texCoords.get(texCoordIndex + 1);
			}
			for(i=0; i<oid.normalIndices.size(); ++i){
				short normalIndex = (short)(oid.normalIndices.get(i) * 3);
				int ni = i * 3;
				aNormals[ni] = normals.get(normalIndex);
				aNormals[ni+1] = normals.get(normalIndex + 1);
				aNormals[ni+2] = normals.get(normalIndex + 2);
			}
			
			oid.targetObj.setData(aVertices, aNormals, aTexCoords, aIndices);
			oid.targetObj.setShader(new SimpleMaterial());
			
			mRootObject.addChild(oid.targetObj);
		}
	}
	
	protected class ObjIndexData {
		public BaseObject3D targetObj;
		
		public ArrayList<Short> vertexIndices;
		public ArrayList<Short> texCoordIndices;
		public ArrayList<Short> normalIndices;
		
		public ObjIndexData(BaseObject3D targetObj) {
			this.targetObj = targetObj;
			vertexIndices = new ArrayList<Short>();
			texCoordIndices = new ArrayList<Short>();
			normalIndices = new ArrayList<Short>();
		}
	}
}
