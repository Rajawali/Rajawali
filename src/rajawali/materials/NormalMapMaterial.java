package rajawali.materials;

import rajawali.lights.ALight;

import com.monyetmabuk.livewallpapers.photosdof.R;

public class NormalMapMaterial extends AAdvancedMaterial {

	public NormalMapMaterial() {
		this(false);
	}
	
	public NormalMapMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
	}
	
	public NormalMapMaterial(boolean isAnimated) {
		super(R.raw.diffuse_material_vertex, R.raw.bumpmap_material_fragment);
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
