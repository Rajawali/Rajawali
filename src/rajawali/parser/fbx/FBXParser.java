package rajawali.parser.fbx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import rajawali.BaseObject3D;
import rajawali.materials.AMaterial;
import rajawali.math.Number3D;
import rajawali.parser.AParser;
import rajawali.parser.fbx.FBXValues.Connections.Connect;
import rajawali.parser.fbx.FBXValues.FBXColor4;
import rajawali.parser.fbx.FBXValues.FBXFloatBuffer;
import rajawali.parser.fbx.FBXValues.FBXIntBuffer;
import rajawali.parser.fbx.FBXValues.FBXMatrix;
import rajawali.parser.fbx.FBXValues.Objects.Model;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;

public class FBXParser extends AParser {
	private static final char COMMENT = ';';
	private static final String OBJECT_TYPE = "ObjectType:";
	private static final String MODEL = "Model:";
	private static final String PROPERTIES = "Properties";
	private static final String LAYER_ELEMENT = "LayerElement";
	private static final String LAYER = "Layer:";
	private static final String PROPERTY = "Property:";
	private static final String TYPE_VECTOR3D = "Vector3D";
	private static final String TYPE_COLOR = "Color";
	private static final String TYPE_COLOR_RGB = "ColorRGB";
	private static final String TYPE_LCL_TRANSLATION = "LclTranslation";
	private static final String TYPE_LCL_ROTATION = "LclRotation";
	private static final String TYPE_LCL_SCALING = "LclScaling";
	private static final String MATERIAL = "Material:";
	private static final String POSE = "Pose:";
	private static final String POSE_NODE = "PoseNode:";
	private static final String CONNECT = "Connect:";
	
	private FBXValues mFbx;
	private Stack<Object> mObjStack;
	private RajawaliRenderer mRenderer;
	
	public FBXParser(RajawaliRenderer renderer, int resourceId) {
		super(renderer.getContext().getResources(), renderer.getTextureManager(), resourceId);
		mRenderer = renderer;
		mObjStack = new Stack<Object>();
		mFbx = new FBXValues();
		mObjStack.add(mFbx);
	}
	
	@Override
	public void parse() {
		InputStream fileIn = mResources.openRawResource(mResourceId);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(fileIn));
		String line;
		
		try {
			while((line = buffer.readLine()) != null) {
				if(line.length() == 0 || line.charAt(0) == COMMENT)
					continue;
				
				readLine(buffer, line);
			}
			buffer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Stack<Model> models = mFbx.objects.getModelsByType(FBXValues.MODELTYPE_MESH);
		
		for(int i=0; i<models.size(); ++i) {
			buildMesh(models.get(i));
		}
	}
	
	private void buildMesh(Model model) {
		BaseObject3D o = new BaseObject3D(model.name);
		boolean hasUVs = model.layerElementUV.uVIndex != null;
		
		ArrayList<Integer> indices = new ArrayList<Integer>();
		ArrayList<Float> uvs = new ArrayList<Float>();
		int[] vidx = model.polygonVertexIndex.data;
		int[] uvidx = null;
		float[] uvdata = null;
		if(hasUVs) {
			uvidx = model.layerElementUV.uVIndex.data;
			uvdata = model.layerElementUV.uV.data;
		}
		
		int count = 0;
		
		for(int i=0; i<vidx.length; ++i) {
			count++;
			
			if(vidx[i] < 0) {
				if(count==3) {
					indices.add(vidx[i-2]);
					indices.add(vidx[i-1]);
					indices.add((vidx[i] * -1)-1);
					
					if(hasUVs) {
						int uvIndex1 = uvidx[vidx[i-2]] * 2;
						int uvIndex2 = uvidx[vidx[i-1]] * 2;
						int uvIndex3 = uvidx[(vidx[i] * -1)-1] * 2;
						
						uvs.add(uvdata[uvIndex1]);	uvs.add(uvdata[uvIndex1+1]);
						uvs.add(uvdata[uvIndex2]);	uvs.add(uvdata[uvIndex2+1]);
						uvs.add(uvdata[uvIndex3]);	uvs.add(uvdata[uvIndex3+1]);
					}
				} else {
					int index1 = vidx[i-3];
					int index2 = vidx[i-2];
					int index3 = vidx[i-1];
					int index4 = (vidx[i] * -1)-1;
					
					indices.add(index3);
					indices.add(index4);
					indices.add(index1);
					
					indices.add(index2);
					indices.add(index3);
					indices.add(index1);
					
					if(hasUVs) {
						int uvIndex1 = uvidx[index1] * 2;
						int uvIndex2 = uvidx[index2] * 2;
						int uvIndex3 = uvidx[index3] * 2;
						int uvIndex4 = uvidx[index4] * 2;
						
						uvs.add(uvdata[uvIndex3]);	uvs.add(uvdata[uvIndex3+1]);
						uvs.add(uvdata[uvIndex4]);	uvs.add(uvdata[uvIndex4+1]);
						uvs.add(uvdata[uvIndex1]);	uvs.add(uvdata[uvIndex1+1]);	
	
						uvs.add(uvdata[uvIndex2]);	uvs.add(uvdata[uvIndex2+1]);
						uvs.add(uvdata[uvIndex3]);	uvs.add(uvdata[uvIndex3+1]);
						uvs.add(uvdata[uvIndex1]);	uvs.add(uvdata[uvIndex1+1]);
					}
				}
				count = 0;
			}
		}
		o.setData(model.vertices.data, model.layerElementNormal.normals.data, hasUVs ? convertFloats(uvs) : null, null, convertIntegers(indices));
		o.setMaterial(getMaterialForMesh(model.name));
		//o.getMaterial().setUseColor(true);
		o.setPosition(model.properties.lclTranslation);
		o.setScale(model.properties.lclScaling);
		o.setRotation(model.properties.lclRotation);
//		o.setColor(Color.rgb((int)(model.properties.color.x * 255f), (int)(model.properties.color.y * 255f), (int)(model.properties.color.z * 255f)));		
		
		
		//mRootObject.addChild(o);
	}
	
	public static int[] convertIntegers(List<Integer> integers)
	{
	    int[] ret = new int[integers.size()];
	    for (int i=0; i < ret.length; i++)
	    {
	        ret[i] = integers.get(i).intValue();
	    }
	    return ret;
	}
	
	public static float[] convertFloats(List<Float> floats)
	{
	    float[] ret = new float[floats.size()];
	    for (int i=0; i < ret.length; i++)
	    {
	        ret[i] = floats.get(i).floatValue();
	    }
	    return ret;
	}
	
	private AMaterial getMaterialForMesh(String name) {
		AMaterial mat = null;
		Stack<Connect> conns = mFbx.connections.connections;
		int num = conns.size();
		String materialName = null;
		
		for(int i=0; i<num; ++i) {
			RajLog.i("Checking [" + name + "] against [" +conns.get(i).object2+ "]");
			if(conns.get(i).object2.equals(name)) {
				materialName = conns.get(i).object2;
				RajLog.i("Found material " + conns.get(i).object1 + " for model " + conns.get(i).object2);
				break;
			}
		}
		
		if(materialName != null) {
		
		}
		
		return mat;
	}
	
	private void readLine(BufferedReader buffer, String line) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
		if(line.contains("{")) {
			
			// -- found new object
			
			Object last = mObjStack.peek();

			if(line.contains(":")) {
				if(line.contains(OBJECT_TYPE)) {
					String val = line.split(":")[1].replaceAll("\\W", "");
					Object ot = last.getClass().getDeclaredMethod("addObjectType", String.class).invoke(last, val);
					mObjStack.push(ot);
					return;
				} else if(line.contains(MODEL)) {
					String[] vals = line.split(",");
					vals[0] = vals[0].split(" ")[1].replaceAll("\\\"", "");
					vals[1] = vals[1].replaceAll("\\W", "");
					
					Object mo = last.getClass().getDeclaredMethod("addModel", String.class, String.class).invoke(last, vals[0], vals[1]);
					mObjStack.push(mo);
					return;
				} else if(line.contains(MATERIAL) && !line.contains(LAYER_ELEMENT)) {
					String[] vals = line.split(",");
					vals[0] = vals[0].replaceAll("\\W", "");
					
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
					line = line.replaceAll("\\W|\\d", "");
				} else if(line.contains(LAYER)) {
					line = LAYER;
				} else if(line.contains(POSE)) {
					String val = line.split(":")[1];
					String[] vals = val.split(",");
					last.getClass().getDeclaredMethod("setPoseName", String.class).invoke(last, vals[0].replaceAll("\\W", ""));
					line = POSE;
				}
			}
			
			line = line.replaceAll("\\W", "");
			line = line.replaceAll("FBX", "fbx");
			line = line.substring(0,1).toLowerCase() + line.substring(1);
			
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
			String[] spl = line.split(":");
			if(spl.length == 0) return;
			String prop = spl[0].replaceAll("\\W", "");
			prop = prop.replaceAll("FBX", "fbx");
			prop = prop.substring(0,1).toLowerCase() + prop.substring(1);
			boolean processNextLine = false;
			
			Object obj = mObjStack.peek();
			try {
				if(spl.length < 2) return;
				String val = spl[1];
				
				if(line.contains(PROPERTY)) {
					String[] vals = val.split(",");
					prop = vals[0].replaceAll("\\W", "");
					prop = prop.substring(0,1).toLowerCase() + prop.substring(1);
					String type = vals[1].replaceAll("\\W", "");
				
					if(type.equals(TYPE_VECTOR3D) || type.equals(TYPE_COLOR) || type.equals(TYPE_COLOR_RGB) || type.equals(TYPE_LCL_ROTATION) 
							|| type.equals(TYPE_LCL_SCALING) || type.equals(TYPE_LCL_TRANSLATION)) {
						val = vals[3] +","+vals[4]+","+vals[5];
					} else {
						if(vals.length < 4)
							return;
						val = vals[3];
					}
				} else if(line.contains(CONNECT)) {
					String[] vals = line.substring(line.indexOf(':')).split(",");
					
					last.getClass().getDeclaredMethod("addConnection", String.class, String.class, String.class)
					.invoke(last, vals[0].replaceAll("\\\"|\\s", ""), vals[1].replaceAll("\\\"|\\s", ""), vals[2].replaceAll("\\\"|\\s", ""));
					return;
				}

				Field field = obj.getClass().getField(prop);
				Class<?> clazz = field.getType();
				
				if(clazz.equals(Integer.class))
					field.set(obj, Integer.valueOf("0" + val.replaceAll("\\W", "")));
				else if(clazz.equals(String.class))
					field.set(obj, val);
				else if(clazz.equals(Long.class))
					field.set(obj, Long.valueOf("0" + val.replaceAll("\\W", "")));		
				else if(clazz.equals(Number3D.class))
					field.set(obj, new Number3D(val.split(",")));
				else if(clazz.equals(FBXFloatBuffer.class))
				{
					StringBuffer sb = new StringBuffer(val);
					String noSpace;
					while((line = buffer.readLine()) != null) {
						noSpace = line.replaceAll("\\s", "");
						if(noSpace.charAt(0) == ',')
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
						noSpace = line.replaceAll("\\s", "");
						if(noSpace.charAt(0) == ',')
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
						noSpace = line.replaceAll("\\s", "");
						if(noSpace.charAt(0) == ',')
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
				
				if(processNextLine)
					readLine(buffer, line);
			} catch(NoSuchFieldException e) {
				return;
			}
		}
	}
}
