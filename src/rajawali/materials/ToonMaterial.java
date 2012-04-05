package rajawali.materials;

import rajawali.wallpaper.Wallpaper;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

public class ToonMaterial extends DiffuseMaterial {
	protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N, L;\n" +
			"varying vec4 vColor;\n" +
	 
			"uniform sampler2D uTexture0;\n" +
			"uniform bool uUseTexture;\n" +
			"uniform float uLightPower;\n" +
			"uniform vec4 uAmbientColor;\n" +
			"uniform vec4 uAmbientIntensity;\n" + 
			"uniform vec4 uToonColor0, uToonColor1, uToonColor2, uToonColor3;\n" +

			"void main() {\n" +
			"	float intensity = max(dot(L, N), 0.0);\n" +
			"	vec4 color = vColor;" +
			"   if(intensity > .95) color = uToonColor0;" +
			"   else if(intensity > .5) color = uToonColor1;" +
			"   else if(intensity > .25) color = uToonColor2;" +
			"   else color = uToonColor3;" +
			"	color.rgb *= intensity * uLightPower;" +	
			"	color += uAmbientColor * uAmbientIntensity;" +
			"	gl_FragColor = color;" +
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
		muToonColor0Handle = GLES20.glGetUniformLocation(mProgram, "uToonColor0");
		if(muToonColor0Handle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uToonColor0");
		}
		muToonColor1Handle = GLES20.glGetUniformLocation(mProgram, "uToonColor1");
		if(muToonColor1Handle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uToonColor1");
		}
		muToonColor2Handle = GLES20.glGetUniformLocation(mProgram, "uToonColor2");
		if(muToonColor2Handle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uToonColor2");
		}
		muToonColor3Handle = GLES20.glGetUniformLocation(mProgram, "uToonColor3");
		if(muToonColor3Handle == -1) {
			Log.d(Wallpaper.TAG, "Could not get uniform location for uToonColor3");
		}
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
