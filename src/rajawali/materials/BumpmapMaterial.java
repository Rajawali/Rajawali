package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;

import rajawali.lights.ALight;

public class BumpmapMaterial extends AAdvancedMaterial {

	public BumpmapMaterial() {
		this(false);
	}
	
	public BumpmapMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		super(vertexShader, fragmentShader, isAnimated);
	}
	
	public BumpmapMaterial(boolean isAnimated) {
		super(R.raw.diffuse_material_vertex, R.raw.bumpmap_material_fragment, isAnimated);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		StringBuffer fc = new StringBuffer();
		StringBuffer vc = new StringBuffer();
		fc.append("float normPower = 0.0;\n");
		
		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);
			
			if(light.getLightType() == ALight.POINT_LIGHT) {
				fc.append("L = normalize(uLightPosition").append(i).append(" - V.xyz);\n");
				vc.append("dist = distance(V.xyz, uLightPosition").append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (uLightAttenuation").append(i).append("[1] + uLightAttenuation").append(i).append("[2] * dist + uLightAttenuation").append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				fc.append("L = -normalize(uLightDirection").append(i).append(");");				
				vc.append("vAttenuation").append(i).append(" = 1.0;\n");
			}
			fc.append("normPower = uLightPower").append(i).append(" * max(dot(bumpnormal, L), 0.1) * vAttenuation").append(i).append(";\n");
			fc.append("intensity += normPower;\n");
			fc.append("Kd += uLightColor").append(i).append(" * normPower;\n");
		}
		
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", vc.toString()), fragmentShader.replace("%LIGHT_CODE%", fc.toString()));
	}

}
