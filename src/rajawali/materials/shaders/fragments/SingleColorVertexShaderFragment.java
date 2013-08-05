package rajawali.materials.shaders.fragments;

import android.graphics.Color;
import android.opengl.GLES20;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;


public class SingleColorVertexShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "SINGLE_COLOR_VERTEX";	
	private final static String U_SINGLE_COLOR = "uSingleColor";
	
	private RVec4 muSingleColor;
	private int muSingleColorHandle;
	private float mColor[];
	
	public SingleColorVertexShaderFragment()
	{
		super(ShaderType.VERTEX_SHADER_FRAGMENT);
		mColor = new float[] { 1.f, 0, 0, 1.f };
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		muSingleColor = (RVec4)addUniform(U_SINGLE_COLOR, DataType.VEC4);
	}
	
	@Override
	public void main() {
		RVec4 color = (RVec4)getGlobal(DefaultVar.G_COLOR);
		color.assign(muSingleColor);
	}
	
	public String getShaderId()
	{
		return SHADER_ID;
	}
	
	@Override
	public void setLocations(int programHandle) {
		muSingleColorHandle = getUniformLocation(programHandle, U_SINGLE_COLOR);
	}
	
	public void setColor(float[] color) {
		mColor = color;
		
	}
	
	public void setColor(int color) {
		mColor[0] = Color.red(color) / 255.f;
		mColor[1] = Color.green(color) / 255.f;
		mColor[2] = Color.blue(color) / 255.f;
		mColor[3] = Color.alpha(color) / 255.f;
	}

	@Override
	public void applyParams() {
		GLES20.glUniform4fv(muSingleColorHandle, 1, mColor, 0);
	}
}
