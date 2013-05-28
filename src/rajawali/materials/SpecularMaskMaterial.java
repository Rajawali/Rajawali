package rajawali.materials;

import rajawali.lights.ALight;

import com.monyetmabuk.livewallpapers.photosdof.R;

public class SpecularMaskMaterial extends PhongMaterial {
	
	public SpecularMaskMaterial() {
		super(R.raw.phong_material_vertex, R.raw.specular_mask_material_fragment);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		StringBuffer fc = new StringBuffer();
		StringBuffer vc = new StringBuffer();
		
		for (int i = 0; i < mLights.size(); ++i) {
			ALight light = mLights.get(i);
			
			if (light.getLightType() == ALight.POINT_LIGHT) {
				fc.append("L = normalize(uLightPosition").append(i).append(" + vEyeVec);\n");
				
				vc.append("dist = distance(-vEyeVec, uLightPosition").append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (uLightAttenuation").append(i).append("[1] + uLightAttenuation").append(i).append("[2] * dist + uLightAttenuation").append(i).append("[3] * dist * dist);\n");				
			} else if (light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				fc.append("L = normalize(-uLightDirection").append(i).append(");\n");
			}
			
			fc.append("NdotL = max(dot(bumpnormal, L), 0.1);\n");
			fc.append("Kd += NdotL * vAttenuation").append(i).append(" * uLightPower").append(i).append(";\n");
			fc.append("Ks += pow(NdotL, uShininess) * vAttenuation").append(i).append(" * uLightPower").append(i).append(";\n");
		}
		
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", vc.toString()), fragmentShader.replace("%LIGHT_CODE%", fc.toString()));
	}
}