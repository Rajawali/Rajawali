package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;

import rajawali.lights.ALight;
import android.opengl.GLES20;

public class SphereMapMaterial extends AAdvancedMaterial {
	
	private int muSphereMapStrengthHandle;
	
	private float mSphereMapStrength = .4f;
	
	public SphereMapMaterial() {
		super(R.raw.sphere_material_vertex, R.raw.sphere_material_fragment);
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform1f(muSphereMapStrengthHandle, mSphereMapStrength);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		StringBuffer sb = new StringBuffer();
		StringBuffer vc = new StringBuffer();

		sb.append("vec3 L = vec3(0.0);\n");

		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);
			
			if(light.getLightType() == ALight.POINT_LIGHT) {
				sb.append("L = normalize(uLightPosition").append(i).append(" - V.xyz);\n");
				vc.append("dist = distance(V.xyz, uLightPosition").append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (uLightAttenuation").append(i).append("[1] + uLightAttenuation").append(i).append("[2] * dist + uLightAttenuation").append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				vc.append("vAttenuation").append(i).append(" = 1.0;\n");
				sb.append("L = -normalize(uLightDirection").append(i).append(");");				
			}
			sb.append("intensity += uLightPower").append(i).append(" * max(dot(N, L), 0.1) * vAttenuation").append(i).append(";\n");
		}
		
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", vc.toString()), fragmentShader.replace("%LIGHT_CODE%", sb.toString()));
		
		muSphereMapStrengthHandle = getUniformLocation("uSphereMapStrength");
	}

	public float getSphereMapStrength() {
		return mSphereMapStrength;
	}

	public void setSphereMapStrength(float sphereMapStrength) {
		this.mSphereMapStrength = sphereMapStrength;
	}
}
