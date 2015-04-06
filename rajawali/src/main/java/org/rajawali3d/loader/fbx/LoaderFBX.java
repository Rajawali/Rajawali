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
package org.rajawali3d.loader.fbx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.Object3D;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.lights.SpotLight;
import org.rajawali3d.loader.AMeshLoader;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.fbx.FBXValues.Connections.Connect;
import org.rajawali3d.loader.fbx.FBXValues.FBXColor4;
import org.rajawali3d.loader.fbx.FBXValues.FBXFloatBuffer;
import org.rajawali3d.loader.fbx.FBXValues.FBXIntBuffer;
import org.rajawali3d.loader.fbx.FBXValues.FBXMatrix;
import org.rajawali3d.loader.fbx.FBXValues.Objects.FBXMaterial;
import org.rajawali3d.loader.fbx.FBXValues.Objects.Model;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture.TextureException;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector2;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.util.RajLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
public class LoaderFBX extends AMeshLoader {
	private static final char COMMENT = ';';
	private static final String OBJECT_TYPE = "ObjectType:";
	private static final String MODEL = "Model:";
	private static final String PROPERTIES = "Properties";
	private static final String LAYER_ELEMENT = "LayerElement";
	private static final String LAYER = "Layer:";
	private static final String PROPERTY = "Property:";
	private static final String TYPE_VECTOR3D = "Vector3D";
	private static final String TYPE_VECTOR = "Vector";
	private static final String TYPE_COLOR = "Color";
	private static final String TYPE_COLOR_RGB = "ColorRGB";
	private static final String TYPE_LCL_TRANSLATION = "LclTranslation";
	private static final String TYPE_LCL_ROTATION = "LclRotation";
	private static final String TYPE_LCL_SCALING = "LclScaling";
	private static final String MATERIAL = "Material:";
	private static final String POSE = "Pose:";
	private static final String POSE_NODE = "PoseNode:";
	private static final String CONNECT = "Connect:";
	private static final String TEXTURE = "Texture:";
	private static final String FBX_U = "FBX";
	private static final String FBX_L = FBX_U.toLowerCase(Locale.US);
	
	private static final String REGEX_CLEAN = "\\s|\\t|\\n";
	private static final String REGEX_NO_SPACE_NO_QUOTE = "\\\"|\\s";
	private static final String REGEX_NO_QUOTE = "\\\"";
	private static final String REGEX_NO_FUNNY_CHARS = "\\W";
	private static final String REPLACE_EMPTY = "";
	
	private FBXValues mFbx;
	private Stack<Object> mObjStack;
	private RajawaliRenderer mRenderer;
	
	public LoaderFBX(RajawaliRenderer renderer, String fileOnSDCard) {
		super(renderer, fileOnSDCard);
		mRenderer = renderer;
		mObjStack = new Stack<Object>();
		mFbx = new FBXValues();
		mObjStack.add(mFbx);
	}
	
	public LoaderFBX(RajawaliRenderer renderer, File file) {
		super(renderer, file);
		mRenderer = renderer;
		mObjStack = new Stack<Object>();
		mFbx = new FBXValues();
		mObjStack.add(mFbx);
	}
	
	public LoaderFBX(RajawaliRenderer renderer, int resourceId) {
		super(renderer.getContext().getResources(), renderer.getTextureManager(), resourceId);
		mRenderer = renderer;
		mObjStack = new Stack<Object>();
		mFbx = new FBXValues();
		mObjStack.add(mFbx);
	}
	
	@Override
	public LoaderFBX parse() throws ParsingException {
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
				String repl = line.replaceAll(REGEX_CLEAN, REPLACE_EMPTY);
				if(repl.length() == 0 || repl.charAt(0) == COMMENT)
					continue;
				
				readLine(buffer, line);
			}
			buffer.close();
		} catch(Exception e) {
			throw new ParsingException(e);
		}
		
		// -- get lights
		
		Stack<Model> lights = mFbx.objects.getModelsByType(FBXValues.MODELTYPE_LIGHT);
		int numLights = lights.size();
		
		RajawaliRenderer.setMaxLights(numLights == 0 ? 1 : numLights);
		Stack<ALight> sceneLights = new Stack<ALight>();
		
		for(int i=0; i<numLights; ++i) {
			Model l = lights.get(i);
			// -- really need to add more light types

			sceneLights.add(buildLight(l));
		}
		
		if(numLights == 0)
		{
			ALight light = new DirectionalLight();
			light.setPosition(2, 0, -5);
			light.setPower(1);
			sceneLights.add(light);
		}
		
		// -- check fog
		//TODO: add fog support
		/*if(mFbx.version5.fogOptions.fogEnable != null && mFbx.version5.fogOptions.fogEnable == 1) {
			FogOptions fogOptions = mFbx.version5.fogOptions;
			mRenderer.setFogEnabled(true);
			Camera cam = mRenderer.getCamera();
			cam.setFogEnabled(true);
			cam.setFogNear(fogOptions.fogStart);
			cam.setFogColor(fogOptions.fogColor.color);
			mRenderer.setBackgroundColor(fogOptions.fogColor.color);
		}*/
	
		// -- get meshes
		
		Stack<Model> models = mFbx.objects.getModelsByType(FBXValues.MODELTYPE_MESH);
		
		try {
			for(int i=0; i<models.size(); ++i) {
				buildMesh(models.get(i), sceneLights);
			}
		} catch(TextureException tme) {
			throw new ParsingException(tme);
		}
		
		// -- get cameras
		
		Stack<Model> cameras = mFbx.objects.getModelsByType(FBXValues.MODELTYPE_CAMERA);
		Model camera = null;

		for(int i=0; i<cameras.size(); ++i) {
			if(cameras.get(i).hidden == null || !cameras.get(i).hidden.equals("True"))
			{
				camera = cameras.get(i);
				break;
			}
		}

		if(camera != null) { //TODO: FIX
			Camera cam = mRenderer.getCurrentCamera();
			cam.setPosition(camera.position);
			cam.setX(mRenderer.getCurrentCamera().getX() * -1);
			cam.setRotation(camera.properties.lclRotation);
			Vector3 lookAt = camera.lookAt;
//			lookAt.x = -lookAt.x;
			cam.setLookAt(lookAt);
			cam.setNearPlane(camera.properties.nearPlane);
			cam.setFarPlane(camera.properties.farPlane);
			cam.setFieldOfView(camera.properties.fieldOfView);
		}
		
		return this;
	}
	
	private ALight buildLight(Model l){
		int m = l.properties.lightType != null ? l.properties.lightType:ALight.POINT_LIGHT;
		switch (m){
		
		case ALight.POINT_LIGHT:		//Point
			PointLight light = new PointLight();
			light.setPosition(l.properties.lclTranslation);
			light.setX(light.getX() * -1f);
			light.setRotation(l.properties.lclRotation);
			light.setPower(l.properties.intensity / 100f);
			light.setColor(l.properties.color);
			// TODO add to scene
			//mRootObject.addLight(light);
			return light;			
			
		case ALight.DIRECTIONAL_LIGHT:		//Area
			DirectionalLight lD = new DirectionalLight();  //TODO calculate direction based on position and rotation
			lD.setPosition(l.properties.lclTranslation);
			lD.setX(lD.getX() * -1f);
			lD.setRotation(l.properties.lclRotation);
			lD.setPower(l.properties.intensity / 100f);
			lD.setColor(l.properties.color);
			// TODO add to scene
			//mRootObject.addLight(lD);
			return lD;
			
		default:
		case ALight.SPOT_LIGHT:		//Spot
			SpotLight lS = new SpotLight();		//TODO calculate direction based on position and rotation
			lS.setPosition(l.properties.lclTranslation);
			lS.setX(lS.getX() * -1f);
			lS.setRotation(l.properties.lclRotation);
			lS.setPower(l.properties.intensity / 100f);
			lS.setCutoffAngle(l.properties.coneangle);
			lS.setColor(l.properties.color);
			lS.setLookAt(0, 0, 0);
			// TODO add to scene
			//mRootObject.addLight(lS);			
			return lS;
		}
	
	}
	
	private void buildMesh(Model model, Stack<ALight> lights) throws TextureException, ParsingException {
		Object3D o = new Object3D(model.name);
		boolean hasUVs = model.layerElementUV.uVIndex != null;
		
		int[] vidx 					= model.polygonVertexIndex.data;
		int[] uvidx 				= null;
		float[] modelVerts 			= model.vertices.data;
		float[] modelNorm			= model.layerElementNormal.normals.data;
		float[] modelUv		 		= null;

		ArrayList<Integer> indices 	= new ArrayList<Integer>();
		ArrayList<Float> vertices 	= new ArrayList<Float>();
		ArrayList<Float> normals	= new ArrayList<Float>();
		ArrayList<Float> uvs 		= null;
		
		if(hasUVs) {
			uvs = new ArrayList<Float>();
			uvidx = model.layerElementUV.uVIndex.data;
			modelUv = model.layerElementUV.uV.data;
		}
		
		int count = 0;
		int indexCount = 0;
		int[] triIds = new int[3];
		int[] quadIds = new int[6];
		int i = 0, j = 0, k = 0;
		int vidxLen = vidx.length;
		
		for(i=0; i<vidxLen; ++i) {
			count++;
			
			if(vidx[i] < 0) {
				if(count==3) {
					int index1 = vidx[i-2],
						index2 = vidx[i-1],
						index3 = (vidx[i] * -1) - 1;

					indices.add(indexCount++);
					indices.add(indexCount++);
					indices.add(indexCount++);

					triIds[0] = index1 * 3;
					triIds[1] = index2 * 3;
					triIds[2] = index3 * 3;
					
					for(j=0; j<3; ++j)
					{
						int cid = triIds[j];
						for(k=0; k<3; ++k) {
							vertices.add(modelVerts[cid+k]);
							int dir = i==0 ? -1 : 1; 
							normals.add(modelNorm[cid+k] * dir);
						}
					}
					
					if(hasUVs) {
						int uvIndex3 = uvidx[i] * 2;
						int uvIndex2 = uvidx[i-1] * 2;
						int uvIndex1 = uvidx[i-2] * 2;

						uvs.add(modelUv[uvIndex1+0]);
						uvs.add(1f-modelUv[uvIndex1+1]);
						
						uvs.add(modelUv[uvIndex2+0]);
						uvs.add(1f-modelUv[uvIndex2+1]);
						
						uvs.add(modelUv[uvIndex3+0]);
						uvs.add(1f-modelUv[uvIndex3+1]);
					}
				} else {
					int index1 = vidx[i-3];
					int index2 = vidx[i-2];
					int index3 = vidx[i-1];
					int index4 = (vidx[i] * -1)-1;
					
					indices.add(indexCount++);
					indices.add(indexCount++);
					indices.add(indexCount++);
					indices.add(indexCount++);
					indices.add(indexCount++);
					indices.add(indexCount++);
					
					quadIds[0] = index1 * 3;
					quadIds[1] = index2 * 3;
					quadIds[2] = index3 * 3;
					quadIds[3] = index4 * 3;
					quadIds[4] = index1 * 3;
					quadIds[5] = index3 * 3;
					
					for(j=0; j<6; ++j)
					{
						int cid = quadIds[j];
						for(k=0; k<3; ++k) {
							vertices.add(modelVerts[cid+k]);
							normals.add(modelNorm[cid+k]);
						}
					}					
					
					if(hasUVs) {
						int uvIndex1 = uvidx[i-3] * 2;
						int uvIndex2 = uvidx[i-2] * 2;
						int uvIndex3 = uvidx[i-1] * 2;
						int uvIndex4 = uvidx[i] * 2;
						
						quadIds[0] = uvIndex1;
						quadIds[1] = uvIndex2;
						quadIds[2] = uvIndex3;
						quadIds[3] = uvIndex4;
						quadIds[4] = uvIndex1;
						quadIds[5] = uvIndex3;
						
						for(j=0; j<6; ++j) {
							int cid = quadIds[j];
							for(k=0; k<2; ++k) {
								if(k==0)
									uvs.add(modelUv[cid + k]);
								else
									uvs.add(1f-modelUv[cid + k]);
							}
						}
						
					}
				}
				count = 0;
			}
		}
		
		o.setData(convertFloats(vertices), convertFloats(normals), hasUVs ? convertFloats(uvs) : null, null,
            convertIntegers(indices), false);
		
		vertices.clear();
		vertices = null;
		normals.clear();
		normals = null;
		if(hasUVs) {
			uvs.clear();
			uvs = null;
		}
		indices.clear();
		indices = null;
		o.setMaterial(getMaterialForMesh(o, model.name));
		setMeshTextures(o, model.name);
		
		o.setPosition(model.properties.lclTranslation);
		o.setX(o.getX() * -1);
		o.setScale(model.properties.lclScaling);
		o.setRotation(model.properties.lclRotation);
		o.setRotZ(-o.getRotZ());
		
		mRootObject.addChild(o);
	}
	
	public static int[] convertIntegers(List<Integer> integers)
	{
	    int[] ret = new int[integers.size()];
	    int len = ret.length;
	    for (int i=0; i < len; ++i)
	    {
	        ret[i] = integers.get(i).intValue();
	    }
	    return ret;
	}
	
	public static float[] convertFloats(List<Float> floats)
	{
	    float[] ret = new float[floats.size()];
	    int len = ret.length;
	    for (int i=0; i < len; ++i)
	    {
	        ret[i] = floats.get(i).floatValue();
	    }
	    return ret;
	}
	
	private void setMeshTextures(Object3D o, String name) throws TextureException, ParsingException {
		Stack<FBXValues.Objects.Texture> textures = mFbx.objects.textures;
		Stack<Connect> connections = mFbx.connections.connections;
		int numTex = textures.size();
		int numCon = connections.size();
		
		for(int i=0; i<numTex; ++i) {
			FBXValues.Objects.Texture tex = textures.get(i);
			for(int j=0; j<numCon; ++j) {
				Connect conn = connections.get(j);

				if(conn.object2.equals(name) && conn.object1.equals(tex.textureName))
				{
					// -- one texture for now
					String textureName = tex.fileName;

					Bitmap bitmap = null;
					if(mFile == null) {
						int identifier = mResources.getIdentifier(getFileNameWithoutExtension(textureName).toLowerCase(Locale.US), "drawable", mResources.getResourcePackageName(mResourceId));
						bitmap = BitmapFactory.decodeResource(mResources, identifier);
					} else {
						try {
							String filePath = mFile.getParent() + File.separatorChar + getOnlyFileName(textureName);
							bitmap = BitmapFactory.decodeFile(filePath);
						} catch (Exception e) {
							throw new ParsingException("["+getClass().getCanonicalName()+"] Could not find file " + getOnlyFileName(textureName));
						}
					}
					o.getMaterial().setColorInfluence(0);
					o.getMaterial().addTexture(new Texture(textureName.replaceAll("[\\W]|_", ""), bitmap));
					return;
				}
			}
		}
	}

	private Material getMaterialForMesh(Object3D o, String name) {
		Material mat = new Material();
		FBXMaterial material = null;
		Stack<Connect> conns = mFbx.connections.connections;
		int num = conns.size();
		String materialName = null;
		
		for(int i=0; i<num; ++i) {
			if(conns.get(i).object2.equals(name)) {
				materialName = conns.get(i).object1;
				break;
			}
		}
		
		if(materialName != null) {
			Stack<FBXMaterial> materials = mFbx.objects.materials;
			num = materials.size();
			for(int i=0; i<num; ++i) {
				if(materials.get(i).name.equals(materialName)) {
					material = materials.get(i);
					break;
				}
			}
		}
		
		if(material != null) {
			mat.setDiffuseMethod(new DiffuseMethod.Lambert());
			mat.enableLighting(true);
			Vector3 color = material.properties.diffuseColor;
			mat.setColor(Color.rgb((int)(color.x * 255.f), (int)(color.y * 255.f), (int)(color.z * 255.f)));
			color = material.properties.ambientColor;
			mat.setAmbientColor(Color.rgb((int)(color.x * 255.f), (int)(color.y * 255.f), (int)(color.z * 255.f)));
			float intensity = material.properties.ambientFactor.floatValue();
			mat.setAmbientIntensity(intensity, intensity, intensity);

			if(material.shadingModel.equals("phong"))
			{
				SpecularMethod.Phong method = new SpecularMethod.Phong();
				if(material.properties.specularColor != null)
				{
					color = material.properties.specularColor;
					method.setSpecularColor(Color.rgb((int)(color.x * 255.f), (int)(color.y * 255.f), (int)(color.z * 255.f)));
				}
				if(material.properties.shininess != null)
					method.setShininess(material.properties.shininess);
			}
		}
		
		return mat;
	}
	
	private void readLine(BufferedReader buffer, String line) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
		if(line.replaceAll(REGEX_CLEAN, REPLACE_EMPTY).length() == 0) return;
		if(line.contains("{")) {
			
			// -- found new object
			
			Object last = mObjStack.peek();

			if(line.contains(":")) {
				if(line.contains(OBJECT_TYPE)) {
					String val = line.split(":")[1].replaceAll(REGEX_NO_FUNNY_CHARS, REPLACE_EMPTY);
					Object ot = last.getClass().getDeclaredMethod("addObjectType", String.class).invoke(last, val);
					mObjStack.push(ot);
					return;
				} else if(line.contains(MODEL)) {
					String[] vals = line.split(",");
					if(vals.length < 2) {
						// TODO add model object for Take
						mObjStack.push(new Object());
						return;
					}
					vals[0] = vals[0].split(": ")[1].replaceAll(REGEX_NO_QUOTE, REPLACE_EMPTY);
					vals[1] = vals[1].replaceAll(REGEX_NO_FUNNY_CHARS, REPLACE_EMPTY);
					
					Object mo = last.getClass().getDeclaredMethod("addModel", String.class, String.class).invoke(last, vals[0], vals[1]);
					mObjStack.push(mo);
					return;
				} else if(line.contains(MATERIAL) && !line.contains(LAYER_ELEMENT)) {
					String[] vals = line.split(": ")[1].split(",");
					vals[0] = vals[0].replaceAll(REGEX_NO_SPACE_NO_QUOTE, REPLACE_EMPTY);
					
					Object ma = last.getClass().getDeclaredMethod("addMaterial", String.class).invoke(last, vals[0]);
					mObjStack.push(ma);
					return;
				} else if(line.contains(POSE_NODE)) {
					Object pn = last.getClass().getDeclaredMethod("addPoseNode").invoke(last);
					mObjStack.push(pn);
					return;
				} else if(line.contains(PROPERTIES)) {
					line = "Properties";
				} else if(line.contains(LAYER_ELEMENT)) {
					line = line.replaceAll("\\W|\\d", REPLACE_EMPTY);
				} else if(line.contains(LAYER)) {
					line = LAYER;
				} else if(line.contains(POSE)) {
					String val = line.split(":")[1];
					String[] vals = val.split(",");
					last.getClass().getDeclaredMethod("setPoseName", String.class).invoke(last, vals[0].replaceAll(REGEX_NO_FUNNY_CHARS, REPLACE_EMPTY));
					line = POSE;
				} else if(line.contains(TEXTURE)) {
					String val = line.split(": ")[1];
					String[] vals = val.split(",");
					Object te = last.getClass().getDeclaredMethod("addTexture", String.class, String.class).invoke(last, vals[0].replaceAll(REGEX_NO_QUOTE, REPLACE_EMPTY), vals[1].replace(REGEX_NO_QUOTE, REPLACE_EMPTY));
					mObjStack.push(te);					
					return;
				}
			}
			
			line = line.replaceAll(REGEX_NO_FUNNY_CHARS, REPLACE_EMPTY);
			line = line.replaceAll(FBX_U, FBX_L);
			line = line.substring(0,1).toLowerCase(Locale.US) + line.substring(1);
			
			try {
				Field field = last.getClass().getField(line);
				mObjStack.push(field.get(last));
			} catch(NoSuchFieldException e) {
				// -- create a generic object
				mObjStack.push(new Object());
				return;
			}
		} else if(line.contains("}")) {
			
			// -- end of object
			
			mObjStack.pop();
		} else {

			// -- found property
			
			Object last = mObjStack.peek();			
			String[] spl = line.split(": ");
			if(spl.length == 0) return;
			String prop = spl[0].replaceAll(REGEX_NO_FUNNY_CHARS, REPLACE_EMPTY);
			prop = prop.replaceAll(FBX_U, FBX_L);
			prop = prop.substring(0,1).toLowerCase(Locale.US) + prop.substring(1);
			boolean processNextLine = false;
			
			Object obj = mObjStack.peek();
			try {
				if(spl.length < 2) return;
				String val = spl[1];
				
				if(line.contains(PROPERTY)) {
					String[] vals = val.split(",");
					prop = vals[0].replaceAll(REGEX_NO_FUNNY_CHARS, REPLACE_EMPTY);
					prop = prop.substring(0,1).toLowerCase(Locale.US) + prop.substring(1);
					String type = vals[1].replaceAll(REGEX_NO_FUNNY_CHARS, REPLACE_EMPTY);
				
					if(type.equals(TYPE_VECTOR3D) || type.equals(TYPE_COLOR) || type.equals(TYPE_COLOR_RGB) || type.equals(TYPE_LCL_ROTATION) 
							|| type.equals(TYPE_LCL_SCALING) || type.equals(TYPE_LCL_TRANSLATION) || type.equals(TYPE_VECTOR)) {
						val = vals[3] +","+vals[4]+","+vals[5];
					} else {
						if(vals.length < 4)
							return;
						val = vals[3].replaceAll(REGEX_NO_QUOTE, REPLACE_EMPTY);
					}
				} else if(line.contains(CONNECT)) {
					String[] vals = line.substring(line.indexOf(':')).split(",");
					
					last.getClass().getDeclaredMethod("addConnection", String.class, String.class, String.class)
					.invoke(last, vals[0].replaceAll(REGEX_NO_SPACE_NO_QUOTE, REPLACE_EMPTY), vals[1].replaceAll(REGEX_NO_SPACE_NO_QUOTE, REPLACE_EMPTY), vals[2].replaceAll(REGEX_NO_SPACE_NO_QUOTE, REPLACE_EMPTY));
					return;
				}

				Field field = obj.getClass().getField(prop);
				Class<?> clazz = field.getType();
				
				if(clazz.equals(Integer.class))
				{
					// TODO investigate why there are multiple values in TextureId sometimes
					if(val.split(",").length > 0) val = val.split(",")[0];
					field.set(obj, Integer.valueOf(val.replaceAll(REGEX_NO_SPACE_NO_QUOTE, REPLACE_EMPTY)));
				}
				else if(clazz.equals(String.class))
					field.set(obj, val.replaceAll(REGEX_NO_QUOTE, REPLACE_EMPTY));
				else if(clazz.equals(Long.class))
					field.set(obj, Long.valueOf(val.replaceAll(REGEX_NO_SPACE_NO_QUOTE, REPLACE_EMPTY)));		
				else if(clazz.equals(Float.class))
					field.set(obj, Float.valueOf(val.replaceAll(REGEX_NO_SPACE_NO_QUOTE, REPLACE_EMPTY)));		
				else if(clazz.equals(Vector3.class)) {
					field.set(obj, new Vector3(val.split(",")));
				}
				else if(clazz.equals(FBXFloatBuffer.class))
				{
					StringBuffer sb = new StringBuffer(val);
					String noSpace;
					while((line = buffer.readLine()) != null) {
						noSpace = line.replaceAll("\\s", REPLACE_EMPTY);
						if(noSpace.length() > 0 && noSpace.charAt(0) == ',')
							sb.append(noSpace);
						else
						{
							processNextLine = true;
							break;
						}							
					}
					field.set(obj, new FBXFloatBuffer(sb.toString()));
				}
				else if(clazz.equals(FBXIntBuffer.class))
				{
					StringBuffer sb = new StringBuffer(val);
					String noSpace;
					while((line = buffer.readLine()) != null) {
						noSpace = line.replaceAll("\\s", REPLACE_EMPTY);
						if(noSpace.length() > 0 && noSpace.charAt(0) == ',')
							sb.append(noSpace);
						else
						{
							processNextLine = true;
							break;
						}							
					}
					field.set(obj, new FBXIntBuffer(sb.toString()));
				}
				else if(clazz.equals(FBXMatrix.class))
				{
					StringBuffer sb = new StringBuffer(val);
					String noSpace;
					while((line = buffer.readLine()) != null) {
						noSpace = line.replaceAll(REGEX_CLEAN, REPLACE_EMPTY);
						if(noSpace.length() > 0 && noSpace.charAt(0) == ',')
							sb.append(noSpace);
						else
						{
							processNextLine = true;
							break;
						}							
					}
					field.set(obj, new FBXMatrix(sb.toString()));
				}
				else if(clazz.equals(FBXColor4.class))
				{
					field.set(obj, new FBXColor4(val));
				}
				else if(clazz.equals(Vector2.class))
				{
					field.set(obj, new Vector2(val.replaceAll("\\s", REPLACE_EMPTY).split(",")));
				}
				
				if(processNextLine && line.replaceAll(REGEX_CLEAN, REPLACE_EMPTY).length() > 0)
					readLine(buffer, line);
			} catch(NoSuchFieldException e) {
				return;
			}
		}
	}
}