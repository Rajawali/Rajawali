package rajawali.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;

import rajawali.BaseObject3D;
import rajawali.materials.AMaterial;
import rajawali.materials.BumpmapMaterial;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.PhongMaterial;
import rajawali.materials.TextureManager;
import rajawali.materials.TextureManager.TextureType;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import rajawali.wallpaper.Wallpaper;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

/**
 * The most important thing is that the model should be triangulated. Rajawali doesn�t accept quads, only tris. In Blender, this is an option you can select in the exporter. In a program like MeshLab, this is done automatically.
 * At the moment, Rajawali also doesn�t support per-face textures. This is on the todo list.
 * <p>
 * The options that should be checked when exporting from blender are:
 * <ul>
 * <li>Apply Modifiers
 * <li>Include Normals
 * <li>Include UVs
 * <li>Write Materials (if applicable)
 * <li>Triangulate Faces
 * <li>Objects as OBJ Objects
 * </ul>
 * <p>
 * The files should be written to your �res/raw� folder in your ADT project. Usually you�ll get errors in the console when you do this. The Android SDK ignores file extensions so it�ll regard the .obj and .mtl files as duplicates. The way to fix this is to rename the files. For instance:
 * - myobject.obj > myobject_obj
 * - myobject.mtl > myobject_mtl
 * The parser replaces any dots in file names, so this should be picked up automatically by the parser. Path fragments in front of file names (also texture paths) are discarded so you can leave them as is.
 * <p>
 * The texture file paths in the .mtl files are stripped off periods and path fragments as well. The textures need to be placed in the res/drawable-nodpi folder.
 * <p>
 * If it still throws errors check if there are any funny characters or unsupported texture formats (like bmp).
 * <p>
 * Just as a reminder, here�s the code that takes care of the parsing:
 * <pre>
 * {@code
 * ObjParser objParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.myobject_obj);
 * objParser.parse();
 * BaseObject3D mObject = objParser.getParsedObject();
 * mObject.setLight(mLight);
 * addChild(mObject);
 * }
 * </pre>
 * 
 * @author dennis.ippel
 *
 */
public class ObjParser extends AMeshParser {
    protected final String VERTEX = "v";
    protected final String FACE = "f";
    protected final String TEXCOORD = "vt";
    protected final String NORMAL = "vn";
    protected final String OBJECT = "o";
    protected final String GROUP = "g";
    protected final String MATERIAL_LIB = "mtllib";
    protected final String USE_MATERIAL = "usemtl";
    protected final String NEW_MATERIAL = "newmtl";
    protected final String DIFFUSE_COLOR = "Kd";
    protected final String DIFFUSE_TEX_MAP = "map_Kd";
	
    public ObjParser(RajawaliRenderer renderer, String fileOnSDCard) {
    	super(renderer, fileOnSDCard);
    }
    
	public ObjParser(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, textureManager, resourceId);
	}
	
	@Override
	public ObjParser parse() throws ParsingException {
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
		ObjIndexData currObjIndexData = new ObjIndexData(new BaseObject3D());
		ArrayList<ObjIndexData> objIndices = new ArrayList<ObjIndexData>();
				
		ArrayList<Float> vertices = new ArrayList<Float>();
		ArrayList<Float> texCoords = new ArrayList<Float>();
		ArrayList<Float> normals = new ArrayList<Float>();
		MaterialLib matLib = new MaterialLib();
		
		try {
			while((line = buffer.readLine()) != null) {
				// Skip comments and empty lines.
				if(line.length() == 0 || line.charAt(0) == '#')
					continue;
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
					boolean isQuad = numTokens == 5;
					int[] quadvids = new int[4];
					int[] quadtids = new int[4];
					int[] quadnids = new int[4];
					
                    boolean emptyVt = line.indexOf("//") > -1;
                    if(emptyVt) line = line.replace("//", "/");
                    
                    parts = new StringTokenizer(line);
                    
                    parts.nextToken();
                    StringTokenizer subParts = new StringTokenizer(parts.nextToken(), "/");
                    int partLength = subParts.countTokens();
                    
                    boolean hasuv = partLength >= 2 && !emptyVt;
                    boolean hasn = partLength == 3 || (partLength == 2 && emptyVt);
                    int idx;
                    
                    for (int i = 1; i < numTokens; i++) {
                    	if(i > 1)
                    		subParts = new StringTokenizer(parts.nextToken(), "/");
                    	idx = Integer.parseInt(subParts.nextToken());

                    	if(idx < 0) idx = (vertices.size() / 3) + idx;
                    	else idx -= 1;
                        if(!isQuad)
                        	currObjIndexData.vertexIndices.add(idx);
                        else 
                        	quadvids[i-1] = idx;
                        if (hasuv)
                        {
                        	idx = Integer.parseInt(subParts.nextToken());
                        	if(idx < 0) idx = (texCoords.size() / 2) + idx;
                        	else idx -= 1;
                        	if(!isQuad)
                        		currObjIndexData.texCoordIndices.add(idx);
                        	else 
                            	quadtids[i-1] = idx;
                        }
                        if (hasn)
                        {
                        	idx = Integer.parseInt(subParts.nextToken());
                        	if(idx < 0) idx = (normals.size() / 3) + idx;
                        	else idx -= 1;
                        	if(!isQuad)
                        		currObjIndexData.normalIndices.add(idx);
                        	else 
                            	quadnids[i-1] = idx;
                        }
                    }
                    
                    if(isQuad) {
                    	int[] indices = new int[] { 0, 1, 2, 0, 2, 3 };
                    	
                    	for(int i=0; i<6; ++i) {
                    		int index = indices[i];
                        	currObjIndexData.vertexIndices.add(quadvids[index]);
                        	currObjIndexData.texCoordIndices.add(quadtids[index]);
                        	currObjIndexData.normalIndices.add(quadnids[index]);
                    	}
                    }
				} else if(type.equals(TEXCOORD)) {
					texCoords.add(Float.parseFloat(parts.nextToken()));
                    texCoords.add(1f - Float.parseFloat(parts.nextToken()));
				} else if(type.equals(NORMAL)) {
					normals.add(Float.parseFloat(parts.nextToken()));
                    normals.add(Float.parseFloat(parts.nextToken()));
                    normals.add(Float.parseFloat(parts.nextToken()));
				} else if(type.equals(OBJECT) || type.equals(GROUP)) {
					String objName = parts.hasMoreTokens() ? parts.nextToken() : "Object" + (int)(Math.random() * 10000);
					Log.d(Wallpaper.TAG, "Parsing object: " + objName);
					if(currObjIndexData.targetObj.getName() != null)
						currObjIndexData = new ObjIndexData(new BaseObject3D(objName));
					else
						currObjIndexData.targetObj.setName(objName);
					objIndices.add(currObjIndexData);
				} else if(type.equals(MATERIAL_LIB)) {
					if(!parts.hasMoreTokens()) continue;
					String materialLibPath = parts.nextToken().replace(".", "_");
					Log.d(Wallpaper.TAG, "Found Material Lib: " + materialLibPath);
					if(mFile != null)
						matLib.parse(materialLibPath, null, null);
					else
						matLib.parse(materialLibPath, mResources.getResourceTypeName(mResourceId), mResources.getResourcePackageName(mResourceId));
				} else if(type.equals(USE_MATERIAL)) {
					currObjIndexData.materialName = parts.nextToken();
				}
			}
			buffer.close();
			
			if(objIndices.size() == 0) {
				objIndices.add(currObjIndexData);
			}
		} catch (IOException e) {
			throw new ParsingException(e);
		}
		
		
		int numObjects = objIndices.size();
		
		for(int j=0; j<numObjects; ++j) {
			ObjIndexData oid = objIndices.get(j);
			
			int i;
			float[] aVertices 	= new float[oid.vertexIndices.size() * 3];
			float[] aTexCoords 	= new float[oid.texCoordIndices.size() * 2];
			float[] aNormals 	= new float[oid.normalIndices.size() * 3];
			float[] aColors		= new float[oid.colorIndices.size() * 4];
			int[] aIndices 		= new int[oid.vertexIndices.size()];
			
			for(i=0; i<oid.vertexIndices.size(); ++i) {
				int faceIndex = oid.vertexIndices.get(i) * 3;
				int vertexIndex = i * 3;
				try {
					aVertices[vertexIndex] = vertices.get(faceIndex);
					aVertices[vertexIndex+1] = vertices.get(faceIndex + 1);
					aVertices[vertexIndex+2] = vertices.get(faceIndex + 2);
					aIndices[i] = i;
				} catch(ArrayIndexOutOfBoundsException e) {
					Log.d("Rajawali", "ERREUR!! " + vertexIndex + ", " + faceIndex);
				}
			}
			if(texCoords != null && texCoords.size() > 0) {
				for(i=0; i<oid.texCoordIndices.size(); ++i) {
					int texCoordIndex = oid.texCoordIndices.get(i) * 2;
					int ti = i * 2;
					aTexCoords[ti] = texCoords.get(texCoordIndex);
					aTexCoords[ti + 1] = texCoords.get(texCoordIndex + 1);
				}
			}
			for(i=0; i<oid.colorIndices.size(); ++i) {
				int colorIndex = oid.colorIndices.get(i) * 4;
				int ti = i * 4;
				aTexCoords[ti] = texCoords.get(colorIndex);
				aTexCoords[ti + 1] = texCoords.get(colorIndex + 1);
				aTexCoords[ti + 2] = texCoords.get(colorIndex + 2);
				aTexCoords[ti + 3] = texCoords.get(colorIndex + 3);
			}
			for(i=0; i<oid.normalIndices.size(); ++i){
				int normalIndex = oid.normalIndices.get(i) * 3;
				int ni = i * 3;
				if(normals.size() == 0) {
					RajLog.e("["+getClass().getName()+"] There are no normals specified for this model. Please re-export with normals.");
					throw new ParsingException("["+getClass().getName()+"] There are no normals specified for this model. Please re-export with normals.");
				}
				aNormals[ni] = normals.get(normalIndex);
				aNormals[ni+1] = normals.get(normalIndex + 1);
				aNormals[ni+2] = normals.get(normalIndex + 2);
			}
			
			oid.targetObj.setData(aVertices, aNormals, aTexCoords, aColors, aIndices);
			matLib.setMaterial(oid.targetObj, oid.materialName);
			mRootObject.addChild(oid.targetObj);
		}
		
		if(mRootObject.getNumChildren() == 1)
			mRootObject = mRootObject.getChildAt(0);
		
		return this;
	}
	
	protected class ObjIndexData {
		public BaseObject3D targetObj;
		
		public ArrayList<Integer> vertexIndices;
		public ArrayList<Integer> texCoordIndices;
		public ArrayList<Integer> colorIndices;
		public ArrayList<Integer> normalIndices;
		
		public String materialName;
		
		public ObjIndexData(BaseObject3D targetObj) {
			this.targetObj = targetObj;
			vertexIndices = new ArrayList<Integer>();
			texCoordIndices = new ArrayList<Integer>();
			colorIndices = new ArrayList<Integer>();
			normalIndices = new ArrayList<Integer>();
		}
	}
	
	protected class MaterialLib {
		private final String MATERIAL_NAME = "newmtl";
		private final String AMBIENT_COLOR = "Ka";
		private final String DIFFUSE_COLOR = "Kd";
		private final String SPECULAR_COLOR = "Ks";
		private final String SPECULAR_COEFFICIENT = "Ns";
		private final String ALPHA_1 = "d";
		private final String ALPHA_2 = "Tr";
		private final String AMBIENT_TEXTURE = "map_Ka";
		private final String DIFFUSE_TEXTURE = "map_Kd";
		private final String SPECULAR_COLOR_TEXTURE = "map_Ks";
		private final String SPECULAR_HIGHLIGHT_TEXTURE = "map_Ns";
		private final String ALPHA_TEXTURE_1 = "map_d";
		private final String ALPHA_TEXTURE_2 = "map_Tr";
		private final String BUMP_TEXTURE = "map_Bump";
		
		private Stack<MaterialDef> mMaterials;
		private String mResourcePackage;
		
		public MaterialLib() {
			mMaterials = new Stack<ObjParser.MaterialDef>();
		}
		
		public void parse(String materialLibPath, String resourceType, String resourcePackage) {
			BufferedReader buffer = null;
			if(mFile == null) {
				mResourcePackage = resourcePackage;
				int identifier = mResources.getIdentifier(materialLibPath, resourceType, resourcePackage);
				try {
					InputStream fileIn = mResources.openRawResource(identifier);
					buffer = new BufferedReader(new InputStreamReader(fileIn));
				} catch(Exception e) {
					RajLog.e("["+getClass().getCanonicalName()+"] Could not find material library file (.mtl).");
					return;
				}
			} else {
				try {
					File materialFile = new File(mFile.getParent() + File.separatorChar + materialLibPath);
					buffer = new BufferedReader(new FileReader(materialFile));
				} catch (Exception e) {
					RajLog.e("["+getClass().getCanonicalName()+"] Could not find file.");
					e.printStackTrace();
					return;
				}
			}
			
			String line;
			MaterialDef matDef = null;
			
			try {
				while((line = buffer.readLine()) != null) {
					// Skip comments and empty lines.
					if(line.length() == 0 || line.charAt(0) == '#')
						continue;
					StringTokenizer parts = new StringTokenizer(line, " ");
					int numTokens = parts.countTokens();
					
					if(numTokens == 0)
						continue;
					String type = parts.nextToken();
					type = type.replaceAll("\\t", "");
					type = type.replaceAll(" ", "");
					
					if(type.equals(MATERIAL_NAME)) {
						if(matDef != null) mMaterials.add(matDef);
						matDef = new MaterialDef();
						matDef.name = parts.hasMoreTokens() ? parts.nextToken() : "";
						Log.d(Wallpaper.TAG, "Parsing material: " + matDef.name);
					} else if(type.equals(DIFFUSE_COLOR)) {
						matDef.diffuseColor = getColorFromParts(parts);
					} else if(type.equals(AMBIENT_COLOR)) {
						matDef.ambientColor = getColorFromParts(parts);
					} else if(type.equals(SPECULAR_COLOR)) {
						matDef.specularColor = getColorFromParts(parts);
					} else if(type.equals(SPECULAR_COEFFICIENT)) {
						matDef.specularCoefficient = Float.parseFloat(parts.nextToken());
					} else if(type.equals(ALPHA_1) || type.equals(ALPHA_2)) {
						matDef.alpha = Float.parseFloat(parts.nextToken());
					} else if(type.equals(AMBIENT_TEXTURE)) {
						matDef.ambientTexture = parts.nextToken();
					} else if(type.equals(DIFFUSE_TEXTURE)) {
						matDef.diffuseTexture = parts.nextToken();
					} else if(type.equals(SPECULAR_COLOR_TEXTURE)) {
						matDef.specularColorTexture = parts.nextToken();
					} else if(type.equals(SPECULAR_HIGHLIGHT_TEXTURE)) {
						matDef.specularHightlightTexture = parts.nextToken();
					} else if(type.equals(ALPHA_TEXTURE_1) || type.equals(ALPHA_TEXTURE_2)) {
						matDef.alphaTexture = parts.nextToken();
					} else if(type.equals(BUMP_TEXTURE)) {
						matDef.bumpTexture = parts.nextToken();
					}
				}
				if(matDef != null) mMaterials.add(matDef);
				buffer.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		public void setMaterial(BaseObject3D object, String materialName) {
			MaterialDef matDef = null;
			
			for(int i=0; i<mMaterials.size(); ++i) {
				if(mMaterials.get(i).name.equals(materialName))
				{
					matDef = mMaterials.get(i);
					break;
				}
			}

			boolean hasTexture = matDef != null && matDef.diffuseTexture != null;
			boolean hasBump = matDef != null && matDef.bumpTexture != null;
			boolean hasSpecular = matDef != null && matDef.specularColor > 0xff000000 && matDef.specularCoefficient > 0;
			
			AMaterial mat = null;
			
			if(hasSpecular && !hasBump)
				mat = new PhongMaterial();
			else if(hasBump)
				mat = new BumpmapMaterial();
			else
				mat = new DiffuseMaterial();

			mat.setUseColor(!hasTexture);
			object.setColor(matDef != null ? matDef.diffuseColor : (0xff000000 + ((int)(Math.random() * 0xffffff))));
			object.setMaterial(mat);
			if(hasSpecular && !hasBump) {
				PhongMaterial phong = (PhongMaterial)mat;
				phong.setSpecularColor(matDef.specularColor);
				phong.setShininess(matDef.specularCoefficient);
			}
			
			if(hasTexture) {
				if(mFile == null) {
					int identifier = mResources.getIdentifier(getFileNameWithoutExtension(matDef.diffuseTexture), "drawable", mResourcePackage);
					object.addTexture(mTextureManager.addTexture(BitmapFactory.decodeResource(mResources, identifier)));
				} else {
					try {
						String filePath = mFile.getParent() + File.separatorChar + getOnlyFileName(matDef.diffuseTexture);
						object.addTexture(mTextureManager.addTexture(BitmapFactory.decodeFile(filePath), TextureType.BUMP));
					} catch (Exception e) {
						RajLog.e("["+getClass().getCanonicalName()+"] Could not find file " + matDef.diffuseTexture);
						e.printStackTrace();
						return;
					}
				}
			}
			if(hasBump) {
				if(mFile == null) {
					int identifier = mResources.getIdentifier(getFileNameWithoutExtension(matDef.bumpTexture), "drawable", mResourcePackage);
					object.addTexture(mTextureManager.addTexture(BitmapFactory.decodeResource(mResources, identifier), TextureType.BUMP));
				} else {
					try {
						String filePath = mFile.getParent() + File.separatorChar + getOnlyFileName(matDef.bumpTexture);
						object.addTexture(mTextureManager.addTexture(BitmapFactory.decodeFile(filePath), TextureType.BUMP));
					} catch (Exception e) {
						RajLog.e("["+getClass().getCanonicalName()+"] Could not find file " + matDef.bumpTexture);
						e.printStackTrace();
						return;
					}
				}
			}
		}
		
		private int getColorFromParts(StringTokenizer parts) {
			int r = (int)(Float.parseFloat(parts.nextToken()) * 255f);
			int g = (int)(Float.parseFloat(parts.nextToken()) * 255f);
			int b = (int)(Float.parseFloat(parts.nextToken()) * 255f);
			return Color.rgb(r, g, b);
		}
	}
}
