package rajawali.materials;

import rajawali.lights.ALight;
import android.opengl.GLES20;

public class SphereMapMaterial extends AAdvancedMaterial {
	protected static final String mVShader = 
		"precision mediump float;\n" +

		"uniform mat4 uMVPMatrix;\n" +
		"uniform mat4 uMMatrix;\n" +
		"uniform mat3 uNMatrix;\n" +
		"uniform vec3 uLightPos;\n" +
		"uniform vec3 uCameraPosition;\n" +
		"attribute vec4 aPosition;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec3 aNormal;\n" +
		"attribute vec4 aColor;\n" +
		"varying vec2 vTextureCoord;\n" +
		"varying vec2 vReflectTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_VERTEX_VARS +
		"%LIGHT_VARS%" +
		
		"void main() {\n" +
		"	float dist = 0.0;\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	V = uMMatrix * aPosition;\n" +
		"	vec3 eyeDir = normalize(V.xyz - uCameraPosition.xyz);\n" +
		"	N = normalize(uNMatrix * aNormal);\n" +
		"	vReflectDir = reflect(eyeDir, N);\n" +
		"	float m = 2.0 * sqrt(vReflectDir.x*vReflectDir.x + vReflectDir.y*vReflectDir.y + (vReflectDir.z+1.0)*(vReflectDir.z+1.0));\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"	vReflectTextureCoord.s = vReflectDir.x/m + 0.5;\n" +
		"	vReflectTextureCoord.t = vReflectDir.y/m + 0.5;\n" +
		"	vNormal = aNormal;\n" +
		"#ifndef TEXTURED\n" +
		"	vColor = aColor;\n" +
		"#endif\n" +
		"%LIGHT_CODE%" +
		M_FOG_VERTEX_DENSITY +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"uniform sampler2D uDiffuseTexture;\n" +
		"uniform sampler2D uSphereMapTexture;\n" +
		"uniform vec4 uAmbientColor;\n" +
		"uniform vec4 uAmbientIntensity;\n" +
		"uniform float uSphereMapStrength;\n" +

		"varying vec2 vReflectTextureCoord;\n" +
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_FRAGMENT_VARS +
		"%LIGHT_VARS%" +

		"void main() {\n" +
		"	float intensity = 0.0;\n" +
		"%LIGHT_CODE%" +
		"	vec4 reflColor = texture2D(uSphereMapTexture, vReflectTextureCoord);\n" +
		"#ifdef TEXTURED\n" +		
		"	vec4 diffColor = texture2D(uDiffuseTexture, vTextureCoord);\n" +
		"#else\n" +
	    "	vec4 diffColor = vColor;\n" +
	    "#endif\n" +
		"	gl_FragColor = diffColor + reflColor * uSphereMapStrength;\n" +
		"	gl_FragColor += uAmbientColor * uAmbientIntensity;" +
		"	gl_FragColor.rgb *= intensity;\n" +
		M_FOG_FRAGMENT_COLOR +	
		"}\n";
	
	private int muSphereMapStrengthHandle;
	
	private float mSphereMapStrength = .4f;
	
	public SphereMapMaterial() {
		super(mVShader, mFShader);
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform1f(muSphereMapStrengthHandle, mSphereMapStrength);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		StringBuffer sb = new StringBuffer();
		StringBuffer vc = new StringBuffer();
		
		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);
			
			sb.append("vec3 L = vec3(0.0);\n");
			
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
