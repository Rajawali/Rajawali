/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.loader;

import android.os.SystemClock;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture.TextureException;
import org.rajawali3d.materials.textures.Etc1Texture;
import org.rajawali3d.materials.textures.NormalMapTexture;
import org.rajawali3d.materials.textures.SpecularMapTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.RajLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

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
public class LoaderOBJ extends AMeshLoader {
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

    private boolean mNeedToRenameMtl = true;
    static AtomicInteger mUniqueInstanceId = new AtomicInteger(1000);

    public LoaderOBJ(Renderer renderer, String fileOnSDCard) {
    	super(renderer, fileOnSDCard);

        mNeedToRenameMtl = false;
    }

    public LoaderOBJ(Renderer renderer, int resourceId) {
    	this(renderer.getContext().getResources(), renderer.getTextureManager(), resourceId);
    }

	public LoaderOBJ(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, textureManager, resourceId);
	}

	public LoaderOBJ(Renderer renderer, File file) {
		super(renderer, file);
	}

	@Override
	public LoaderOBJ parse() throws ParsingException {
		return parse(false);
	}

	public LoaderOBJ parse(boolean offsetCentroids) throws ParsingException {
		super.parse();

		BufferedReader buffer = null;
		try {
			buffer = getBufferedReader();
		} catch (Exception e) {
			RajLog.e("["+getClass().getCanonicalName()+"] Could not find file.");
			e.printStackTrace();
			return this;
		}

		String line;
		ObjIndexData currObjIndexData = new ObjIndexData(new Object3D(generateObjectName()));
		ArrayList<ObjIndexData> objIndices = new ArrayList<ObjIndexData>();

		ArrayList<Float> vertices = new ArrayList<Float>();
		ArrayList<Float> texCoords = new ArrayList<Float>();
		ArrayList<Float> normals = new ArrayList<Float>();
		MaterialLib matLib = new MaterialLib();

		String currentMaterialName=null;
		boolean currentObjHasFaces=false;
		Object3D currentGroup = mRootObject;
		mRootObject.setName("default");
		Map<String, Object3D> groups = new HashMap<String, Object3D>();

		try {
			while((line = buffer.readLine()) != null) {
				// Skip comments and empty lines.
				if(line.length() == 0 || line.charAt(0) == '#')
					continue;
				if(line.endsWith("\\")) {
					line = line.substring(0, line.length()-1) + buffer.readLine();
				}
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
					currentObjHasFaces=true;
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
				} else if(type.equals(GROUP)) {
					int numGroups = parts.countTokens();
					Object3D previousGroup = null;
					for(int i=0; i<numGroups; i++) {
						String groupName = parts.nextToken();
						if(!groups.containsKey(groupName)) {
							groups.put(groupName, new Object3D(groupName));
						}
						Object3D group = groups.get(groupName);
						if(previousGroup!=null) {
							addChildSetParent(group, previousGroup);
						} else {
							currentGroup = group;
						}
						previousGroup = group;
					}
					RajLog.i("Parsing group: " + currentGroup.getName());
					if (currentObjHasFaces) {
						objIndices.add(currObjIndexData);
						currObjIndexData = new ObjIndexData(new Object3D(generateObjectName()));
						RajLog.i("Parsing object: " + currObjIndexData.targetObj.getName());
						currObjIndexData.materialName = currentMaterialName;
						currentObjHasFaces = false;
					}
					addChildSetParent(currentGroup, currObjIndexData.targetObj);
				} else if(type.equals(OBJECT)) {
					String objName = parts.hasMoreTokens() ? parts.nextToken() : generateObjectName();

					if (currentObjHasFaces) {
						objIndices.add(currObjIndexData);
						currObjIndexData = new ObjIndexData(new Object3D(currObjIndexData.targetObj.getName()));
						currObjIndexData.materialName = currentMaterialName;
						addChildSetParent(currentGroup, currObjIndexData.targetObj);
						RajLog.i("Parsing object: " + currObjIndexData.targetObj.getName());
						currentObjHasFaces = false;
					}
					currObjIndexData.targetObj.setName(objName);
				} else if(type.equals(MATERIAL_LIB)) {
					if(!parts.hasMoreTokens()) continue;
                    String materialLibPath = mNeedToRenameMtl ? parts.nextToken().replace(".", "_") : parts.nextToken();

					RajLog.d("Found Material Lib: " + materialLibPath);
					matLib.parse(materialLibPath);
				} else if(type.equals(USE_MATERIAL)) {
					currentMaterialName = parts.nextToken();
					if(currentObjHasFaces) {
						objIndices.add(currObjIndexData);
						currObjIndexData = new ObjIndexData(new Object3D(generateObjectName(currentMaterialName)));
						RajLog.i("Parsing object: " + currObjIndexData.targetObj.getName());
						addChildSetParent(currentGroup, currObjIndexData.targetObj);
						currentObjHasFaces = false;
					}
					currObjIndexData.materialName = currentMaterialName;
				}
			}
			buffer.close();

			if(currentObjHasFaces) {
				RajLog.i("Parsing object: " + currObjIndexData.targetObj.getName());
				objIndices.add(currObjIndexData);
			}


		} catch (Exception e) {
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

			float[] centroid = offsetCentroids ? getCentroid(oid.vertexIndices, vertices) : new float[] { 0, 0, 0 };
			oid.targetObj.setPosition(centroid[0], centroid[1], centroid[2]);
			for(i=0; i<oid.vertexIndices.size(); ++i) {
				int faceIndex = oid.vertexIndices.get(i) * 3;
				int vertexIndex = i * 3;
				try {
					aVertices[vertexIndex] = vertices.get(faceIndex) - centroid[0];
					aVertices[vertexIndex+1] = vertices.get(faceIndex + 1) - centroid[1];
					aVertices[vertexIndex+2] = vertices.get(faceIndex + 2) - centroid[2];
					aIndices[i] = i;
				} catch(ArrayIndexOutOfBoundsException e) {
					RajLog.d("Obj array index out of bounds: " + vertexIndex + ", " + faceIndex);
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

			oid.targetObj.setData(aVertices, aNormals, aTexCoords, aColors, aIndices, false);
			try {
				matLib.setMaterial(oid.targetObj, oid.materialName);
			} catch(Exception tme) {
				throw new ParsingException(tme);
			}
			if(oid.targetObj.getParent() == null)
				addChildSetParent(mRootObject, oid.targetObj);
		}
		for(Object3D group : groups.values()) {
			if(group.getParent()==null)
				addChildSetParent(mRootObject, group);
		}

		if(mRootObject.getNumChildren() == 1 && !mRootObject.getChildAt(0).isContainer())
			mRootObject = mRootObject.getChildAt(0);

		for(int i=0; i<mRootObject.getNumChildren(); i++)
			mergeGroupsAsObjects(mRootObject.getChildAt(i));

		return this;
	}

	float[] getCentroid(ArrayList<Integer> vertexIndices, ArrayList<Float> vertices) {
		float[] centroid = new float[] { 0,0,0 };
		for(int i=0; i<vertexIndices.size(); ++i) {
			int faceIndex = vertexIndices.get(i) * 3;
			int vertexIndex = i * 3;
			centroid[0] += vertices.get(faceIndex+0);
			centroid[1] += vertices.get(faceIndex+1);
			centroid[2] += vertices.get(faceIndex+2);
		}
		centroid[0] /= (vertexIndices.size());
		centroid[1] /= (vertexIndices.size());
		centroid[2] /= (vertexIndices.size());
		return centroid;
	}

	/**
	 * Collapse single-object groups. (Some obj exporters use g token for objects)
	 * @param object
	 */
	private void mergeGroupsAsObjects(Object3D object) {
		if(object.isContainer() && object.getNumChildren()==1 && object.getChildAt(0).getName().startsWith("Object")) {
			Object3D child = object.getChildAt(0);
			object.removeChild(child);
			child.setName(object.getName());
			addChildSetParent(object.getParent(), child);
			object.getParent().removeChild(object);
			object = child;
		}

		for(int i=0; i<object.getNumChildren(); i++) {
			mergeGroupsAsObjects(object.getChildAt(i));
		}
	}

	private static String generateObjectName() {
		return generateObjectName(null);
	}

	private static String generateObjectName(String prefix) {
		if(prefix==null) prefix = "Object";
		return prefix + mUniqueInstanceId.incrementAndGet();
	}

	/**
	 * Build string representation of object hierarchy
	 * @param parent
	 * @param sb
	 * @param prefix
	 */
	private void buildObjectGraph(Object3D parent, StringBuffer sb, String prefix) {
		sb.append(prefix).append("-->").append((parent.isContainer() ? "GROUP " : "") + parent.getName()).append('\n');
		for(int i=0; i<parent.getNumChildren(); i++) {
			buildObjectGraph(parent.getChildAt(i), sb, prefix+"\t");
		}
	}

	static private Field mParent;
	static {
		try {
			mParent = Object3D.class.getDeclaredField("mParent");
			mParent.setAccessible(true);
		} catch (NoSuchFieldException e) {
			RajLog.e("Reflection error Object3D.mParent");
		}
	}

	/**
	 * Add child and set parent reference.
	 * WHY DOES OBJECT3D NOT DO THIS?
	 * @param parent
	 * @param object
	 */
	private static void addChildSetParent(Object3D parent, Object3D object) {
		try {
			parent.addChild(object);
			mParent.set(object, parent);
		} catch(Exception e) {
			RajLog.e("Reflection error Object3D.mParent");
		}
	}

	public String toString() {
		if(mRootObject==null) {
			return "Object not parsed";
		} else {
			StringBuffer sb = new StringBuffer();
			buildObjectGraph(mRootObject, sb, "");
			return sb.toString();
		}
	}

	protected class ObjIndexData {
		public Object3D targetObj;

		public ArrayList<Integer> vertexIndices;
		public ArrayList<Integer> texCoordIndices;
		public ArrayList<Integer> colorIndices;
		public ArrayList<Integer> normalIndices;

		public String materialName;

		public ObjIndexData(Object3D targetObj) {
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

		public MaterialLib() {
			mMaterials = new Stack<LoaderOBJ.MaterialDef>();
		}

		public void parse(String materialLibPath) {
			BufferedReader buffer = null;
			try {
				buffer = getBufferedReader(materialLibPath);
			} catch (Exception e) {
				RajLog.e("["+getClass().getCanonicalName()+"] Could not find material library file (.mtl).");
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
						RajLog.d("Parsing material: " + matDef.name);
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
						matDef.specularHighlightTexture = parts.nextToken();
					} else if(type.equals(ALPHA_TEXTURE_1) || type.equals(ALPHA_TEXTURE_2)) {
						matDef.alphaTexture = parts.nextToken();
					} else if(type.equals(BUMP_TEXTURE)) {
						matDef.bumpTexture = parts.nextToken();
					}
				}
				if(matDef != null) mMaterials.add(matDef);
				buffer.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		public void setMaterial(Object3D object, String materialName) throws TextureException, FileNotFoundException {
			if(materialName == null) {
				RajLog.i(object.getName() + " has no material definition." );
				return;
			}

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
			boolean hasSpecularTexture = matDef != null && matDef.specularColorTexture != null;
			boolean hasSpecular = matDef != null && matDef.specularColor > 0xff000000 && matDef.specularCoefficient > 0;

			Material mat = new Material();
			mat.enableLighting(true);
			mat.setDiffuseMethod(new DiffuseMethod.Lambert());
			if(matDef!=null) {
				int alpha = (int)(matDef.alpha*255f);
				mat.setColor(((alpha<<24)&0xFF000000)|(matDef.diffuseColor&0x00FFFFFF));
			} else {
				mat.setColor((int)(Math.random() * 0xffffff));
			}

			if(hasSpecular || hasSpecularTexture) {
				SpecularMethod.Phong method = new SpecularMethod.Phong();
				method.setSpecularColor(matDef.specularColor);
				method.setShininess(matDef.specularCoefficient);
				mat.setSpecularMethod(method);
			}

			if(hasTexture) {
				String textureName = getFileNameWithoutExtension(matDef.diffuseTexture);
				if(isCompressedStream(matDef.diffuseTexture)) {
					Etc1Texture compressed = new Etc1Texture(textureName + "etc1", findCompressedStream(matDef.diffuseTexture), null);
					mat.addTexture(new Texture(textureName, compressed));
				} else {
					mat.addTexture(new Texture(textureName, findBitmap(matDef.diffuseTexture)));
				}
				mat.setColorInfluence(0);
			}
			if(hasBump) {
				String textureName = getFileNameWithoutExtension(matDef.bumpTexture);
				mat.addTexture(new NormalMapTexture(textureName, findBitmap(matDef.bumpTexture)));
			}
			if(hasSpecularTexture) {
				String textureName = getFileNameWithoutExtension(matDef.specularColorTexture);
				mat.addTexture(new SpecularMapTexture(textureName, findBitmap(matDef.specularColorTexture)));
			}
			object.setMaterial(mat);
			if(matDef!=null && matDef.alpha<1f)
				object.setTransparent(true);
		}

		private int getColorFromParts(StringTokenizer parts) {
			int r = (int)(Float.parseFloat(parts.nextToken()) * 255f);
			int g = (int)(Float.parseFloat(parts.nextToken()) * 255f);
			int b = (int)(Float.parseFloat(parts.nextToken()) * 255f);
			return Color.rgb(r, g, b);
		}
	}

	protected class MaterialDef {

		public String name;
		public int ambientColor;
		public int diffuseColor;
		public int specularColor;
		public float specularCoefficient;
		public float alpha = 1f;
		public String ambientTexture;
		public String diffuseTexture;
		public String specularColorTexture;
		public String specularHighlightTexture;
		public String alphaTexture;
		public String bumpTexture;
	}
}
