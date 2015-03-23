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

import java.util.Stack;

import org.rajawali3d.math.vector.Vector2;
import org.rajawali3d.math.vector.Vector3;
import android.graphics.Color;

public class FBXValues {
	public static final String MODELTYPE_CAMERA_SWITCHER = "CameraSwitcher";
	public static final String MODELTYPE_CAMERA = "Camera";
	public static final String MODELTYPE_LIGHT = "Light";
	public static final String MODELTYPE_MESH = "Mesh";
	
	public FBXHeaderExtension fbxHeaderExtension = new FBXHeaderExtension();
	public String creationTime;
	public String creator;
	public Definitions definitions = new Definitions();
	public Objects objects = new Objects();
	public Relations relations = new Relations();
	public Connections connections = new Connections();
	public Takes takes = new Takes();
	public Version5 version5 = new Version5();
	
	protected class FBXHeaderExtension {
		public Integer fbxHeaderVersion;
		public Integer fbxVersion;
		public String creator;
		public CreationTimeStamp creationTimeStamp = new CreationTimeStamp();
		public Object otherFlags = new Object(); 
		
		protected class CreationTimeStamp {
			public Integer version;
			public Integer year;
			public Integer month;
			public Integer day;
			public Integer hour;
			public Integer minute;
			public Integer second;
			public Integer millisecond;
		}
	}
	
	protected class Version5 {
		public AmbientRenderSettings ambientRenderSettings = new AmbientRenderSettings();
		public FogOptions fogOptions = new FogOptions();
		public Settings settings = new Settings();
		public RendererSetting rendererSetting = new RendererSetting();
		
		protected class AmbientRenderSettings {
			public Integer version;
			public FBXColor4 ambientLightColor;
		}
		
		protected class FogOptions {
			public Integer fogEnable;
			public Integer fogMode;
			public Float fogDensity;
			public Float fogStart;
			public Float fogEnd;
			public FBXColor4 fogColor;
		}
		
		protected class Settings {
			public Integer frameRate;
			public Integer timeFormat;
			public Integer snapOnFrames;
			public Integer referenceTimeIndex;
			public Long timeLineStartTime;
			public Long timeLineStopTime;
		}
		
		protected class RendererSetting {
			public String defaultCamera;
			public Integer defaultViewingMode;
		}
	}
	
	protected class Takes {
		public String current;
	}
	
	protected class Definitions {
		public Integer version;
		public Integer count;
		public Stack<ObjectType> objectTypes = new Stack<ObjectType>();
		
		protected ObjectType addObjectType(String type) {
			ObjectType ot = new ObjectType(type);
			objectTypes.add(ot);
			return ot;
		}
		
		protected class ObjectType {
			public Integer count;
			public String type;
			
			public ObjectType(String type) {
				this.type = type;
			}
		}
	}
	
	protected class Connections {
		public Stack<Connect> connections = new Stack<Connect>();
		
		public void addConnection(String type, String object1, String object2) {
			connections.add(new Connect(type, object1, object2));
		}

		protected class Connect {
			public String type;
			public String object1;
			public String object2;
			
			public Connect(String type, String object1, String object2) {
				this.type = type;
				this.object1 = object1;
				this.object2 = object2;
			}
		}
	}
	
	protected class Relations {
		public Stack<Model> models = new Stack<Model>();
		public Stack<Material> materials = new Stack<Material>();
		public Stack<Texture> textures = new Stack<Texture>();
		
		public Texture addTexture(String name, String type) {
			Texture texture = new Texture(name, type);
			textures.add(texture);
			return texture;
		}
		
		protected class Texture {
			public String type;
			public Integer version;
			public String textureName;
			
			public Texture(String name, String type) {
				this.textureName = name;
				this.type = type;
			}
		}
		
		public Model addModel(String name, String type) {
			Model model = new Model(name, type);
			models.add(model);
			return model;
		}
		
		public Material addMaterial(String name) {
			Material material = new Material(name);
			materials.add(material);
			return material;
		}
		
		protected class Model {
			public String name;
			public String type;
			
			public Model(String name, String type) {
				this.name = name;
				this.type = type;
			}
		}
		
		protected class Material {
			public String name;
			
			public Material(String name) {
				this.name = name;
			}
		}
	}
	
	protected class Objects {
		public Stack<Model> models = new Stack<Model>();
		public Stack<FBXMaterial> materials = new Stack<FBXMaterial>();
		public Stack<Texture> textures = new Stack<Texture>();
		public Pose pose = new Pose();
		public GlobalSettings globalSettings = new GlobalSettings();
		
		public Texture addTexture(String name, String type) {
			Texture texture = new Texture(name, type);
			textures.add(texture);
			return texture;
		}
		
		protected class Texture {
			public String type;
			public Integer version;
			public String textureName;
			public String media;
			public String fileName;
			public String relativeFilename;
			public Vector2 modelUVTranslation;
			public Vector2 modelUVScaling;
			public String texture_Alpha_Source;
			public Properties properties = new Properties();
			
			public Texture(String name, String type) {
				this.textureName = name;
				this.type = type;
			}
			
			protected class Properties {
				public Vector3 translation;
				public Vector3 rotation;
				public Vector3 scaling;
				public Float textureAlpha;
				public Integer textureTypeUse;
				public Integer currentTextureBlendMode;
				public Boolean useMaterial;
				public Boolean useMipMap;
				public Integer currentMappingType;
				public Boolean uVSwap;
				public Integer wrapModeU;
				public Integer wrapModeV;
				public Vector3 textureRotationPivot;
				public Vector3 textureScalingPivot;
			}
		}
		
		protected class GlobalSettings {
			public Integer version;
			public Properties properties = new Properties();
			
			protected class Properties {
				public Integer upAxis;
				public Integer upAxisSign;
				public Integer frontAxis;
				public Integer frontAxisSign;
				public Integer coordAxis;
				public Integer coordAxisSign;
				public Float unitScaleFactor;
			}
		}
		
		public void setPoseName(String name) {
			pose.name = name;
		}
		
		protected class Pose {
			public String name;
			public String type;
			public Integer version;
			public Integer nbPoseNodes;
			public Stack<PoseNode> poseNodes = new Stack<PoseNode>();
			public Object properties = new Object();
			
			public PoseNode addPoseNode() {
				PoseNode pn = new PoseNode();
				poseNodes.add(pn);
				return pn;
			}
			
			protected class PoseNode {
				public String node;
				public FBXMatrix matrix;
			}
		}
		
		public Stack<Model> getModelsByType(String type) {
			Stack<Model> mdls = new Stack<Model>();
			
			for(int i=0; i<models.size(); ++i)
				if(models.get(i).type.equals(type))
					mdls.add(models.get(i));
			
			return mdls;
		}
		
		public Model addModel(String name, String type) {
			Model model = new Model(name, type);
			models.add(model);
			return model;
		}
		
		public FBXMaterial addMaterial(String name) {
			FBXMaterial material = new FBXMaterial(name);
			materials.add(material);
			return material;
		}
		
		protected class FBXMaterial {
			public Integer version;
			public String shadingModel;
			public Integer MultiLayer;
			public Properties properties = new Properties();
			public String name;
			
			public FBXMaterial(String name) {
				this.name = name;
			}
			
			protected class Properties {
				public String shadingModel;
				public Boolean multiLayer;
				public Vector3 emissiveColor;
				public Float emissiveFactor;
				public Vector3 ambientColor;
				public Float ambientFactor;
				public Vector3 diffuseColor;
				public Float diffuseFactor;
				public Vector3 bump;
				public Vector3 transparentColor;
				public Float transparencyFactor;
				public Vector3 specularColor;
				public Float specularFactor;
				public Float shininessExponent;
				public Vector3 reflectionColor;
				public Float reflectionFactor;
				public Vector3 emissive;
				public Vector3 ambient;
				public Vector3 diffuse;
				public Vector3 specular;
				public Float shininess;
				public Float opacity;
				public Float reflectivity;
			}
		}
		
		protected class Model {
			public String name;
			public String type;
			public Integer version;
			public String hidden;
			public String culling;
			public String typeFlags;
			public Properties properties = new Properties();
			public Vector3 position;
			public Vector3 up;
			public Vector3 lookAt;
			public FBXFloatBuffer vertices;
			public FBXIntBuffer polygonVertexIndex;
			public LayerElementNormal layerElementNormal = new LayerElementNormal();
			public Object layerElementSmoothing = new Object();
			public LayerElementUV layerElementUV = new LayerElementUV();
			public LayerElementTexture layerElementTexture = new LayerElementTexture();
			public LayerElementMaterial layerElementMaterial = new LayerElementMaterial();
			public Layer layer = new Layer();
			
			public Model(String name, String type) {
				this.name = name;
				this.type = type;
			}
			
			protected class Properties {
				public Boolean quaternionInterpolate;
				public Integer visibility;
				public Vector3 lclTranslation;
				public Vector3 lclRotation;
				public Vector3 lclScaling;
				public Vector3 rotationOffset;
				public Vector3 rotationPivot;
				public Vector3 scalingOffset;
				public Vector3 scalingPivot;
				public Vector3 color;
				public Float intensity;
				public Float fieldOfView;
				public Float focalLength;
				public Integer aspectW;
				public Integer aspectH;
				public Integer pixelAspectRatio;
				public Float nearPlane;
				public Float farPlane;
				public Integer lightType;
				public Float coneangle;
			}
			
			protected class Layer {
				// -- ignore for now
				public LayerElement layerElement = new LayerElement();
			}
			
			protected class LayerElement {
				public Integer version;
				public String name;
				public String mappingInformationType;
				public String referenceInformationType;
				public String type;
				public String typedIndex;
			}
			
			protected class LayerElementNormal extends LayerElement {
				public FBXFloatBuffer normals;
			}
			
			protected class LayerElementUV extends LayerElement {
				public FBXFloatBuffer uV;
				public FBXIntBuffer uVIndex;
			}
			
			protected class LayerElementTexture extends LayerElement {
				public String blendMode;
				public Float textureAlpha;
				public Integer textureId;
			}
			
			protected class LayerElementMaterial extends LayerElement {
				public int materials;
			}
		}
	}
	
	public static class FBXMatrix {
		public float[] data;
		
		public FBXMatrix(String vals) {
			String[] values = vals.split(",");
			int num = values.length;
			data = new float[num];
			
			for(int i=0; i<num; ++i)
				data[i] = Float.parseFloat(values[i].replaceAll("\\s", ""));
		}
	}

	public static class FBXFloatBuffer {
		public float[] data;
		
		public FBXFloatBuffer(String floats) {
			String[] values = floats.split(",");
			int num = values.length;
			data = new float[num];
			
			for(int i=0; i<num; ++i)
				data[i] = Float.parseFloat(values[i].replaceAll("\\s", ""));
		}
	}

	public static class FBXIntBuffer {
		public int[] data;
		
		public FBXIntBuffer(String ints) {
			String[] values = ints.split(",");
			int num = values.length;
			data = new int[num];
			
			for(int i=0; i<num; ++i)
				data[i] = Integer.parseInt(values[i].replaceAll("\\s", ""));
		}
	}
	
	public static class FBXColor4 {
		public int color;
		
		public FBXColor4(String vals) {
			String[] values = vals.split(",");
			color = Color.argb((int)(Float.parseFloat(values[3]) * 255f), (int)(Float.parseFloat(values[0]) * 255f), (int)(Float.parseFloat(values[1]) * 255f), (int)(Float.parseFloat(values[2]) * 255f));
		}
	}
}
