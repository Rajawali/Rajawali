package rajawali.materials;

import android.graphics.Color;
import android.opengl.GLES20;

public class ToonMaterial extends DiffuseMaterial {
	protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N;\n" +
			"varying vec4 V;\n" +
			"varying vec4 vColor;\n" +
	 
			"uniform sampler2D uDiffuseTexture;\n" +
			"uniform bool uUseTexture;\n" +
			"uniform vec4 uAmbientColor;\n" +
			"uniform vec4 uAmbientIntensity;\n" + 
			"uniform vec4 uToonColor0, uToonColor1, uToonColor2, uToonColor3;\n" +
			
			M_FOG_FRAGMENT_VARS +
			M_LIGHTS_VARS +			

			"void main() {\n" +
			"	float intensity = 0.0;\n" +
			"	for(int i=0; i<" +MAX_LIGHTS+ "; i++) {" +
			"		vec3 L = vec3(0.0);" +
			"		float attenuation = 1.0;" +
			"		if(uLightType[i] == POINT_LIGHT) {" +
			"			L = normalize(uLightPosition[i] - V.xyz);\n" +
			"			float dist = distance(V.xyz, uLightPosition[i]);\n" +
			"			attenuation = 1.0 / (uLightAttenuation[i][1] + uLightAttenuation[i][2] * dist + uLightAttenuation[i][3] * dist * dist);\n" +
			"		} else {" +
			"			L = -normalize(uLightDirection[i]);" +
			"		}" +
			"		intensity += uLightPower[i] * max(dot(N, L), 0.1) * attenuation;\n" +
			"	}\n" +
			"	vec4 color = vColor;" +
			"   if(intensity > .95) color = uToonColor0;" +
			"   else if(intensity > .5) color = uToonColor1;" +
			"   else if(intensity > .25) color = uToonColor2;" +
			"   else color = uToonColor3;" +
			"	color.rgb *= intensity;" +	
			"	color += uAmbientColor * uAmbientIntensity;" +
			M_FOG_FRAGMENT_CALC +
			"	gl_FragColor = color;" +
			M_FOG_FRAGMENT_COLOR +	
			"}";
	
	protected int muToonColor0Handle, muToonColor1Handle, muToonColor2Handle, muToonColor3Handle;
	protected float[] mToonColor0, mToonColor1, mToonColor2, mToonColor3;
	
	public ToonMaterial() {
		this(false);
	}
	
	public ToonMaterial(boolean isAnimated) {
		super(DiffuseMaterial.mVShader, mFShader, isAnimated);
		mToonColor0 = new float[] { 1, .5f, .5f, 1 };
		mToonColor1 = new float[] { .6f, .3f, .3f, 1 };
		mToonColor2 = new float[] { .4f, .2f, .2f, 1 };
		mToonColor3 = new float[] { .2f, .1f, .1f, 1 };
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform4fv(muToonColor0Handle, 1, mToonColor0, 0);
		GLES20.glUniform4fv(muToonColor1Handle, 1, mToonColor1, 0);
		GLES20.glUniform4fv(muToonColor2Handle, 1, mToonColor2, 0);
		GLES20.glUniform4fv(muToonColor3Handle, 1, mToonColor3, 0);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muToonColor0Handle = getUniformLocation("uToonColor0");
		muToonColor1Handle = getUniformLocation("uToonColor1");
		muToonColor2Handle = getUniformLocation("uToonColor2");
		muToonColor3Handle = getUniformLocation("uToonColor3");
	}
	
	public void setToonColors(int color0, int color1, int color2, int color3) {
		mToonColor0[0] = Color.red(color0); 
		mToonColor0[1] = Color.green(color0);
		mToonColor0[2] = Color.blue(color0); 
		mToonColor0[3] = Color.alpha(color0); 

		mToonColor1[0] = Color.red(color1); 
		mToonColor1[1] = Color.green(color1);
		mToonColor1[2] = Color.blue(color1); 
		mToonColor1[3] = Color.alpha(color1); 

		mToonColor2[0] = Color.red(color2); 
		mToonColor2[1] = Color.green(color2);
		mToonColor2[2] = Color.blue(color2); 
		mToonColor2[3] = Color.alpha(color2); 
		
		mToonColor3[0] = Color.red(color3); 
		mToonColor3[1] = Color.green(color3);
		mToonColor3[2] = Color.blue(color3); 
		mToonColor3[3] = Color.alpha(color3); 
	}
}
