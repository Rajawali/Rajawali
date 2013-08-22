package rajawali.materials.plugins;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.AShaderBase.DataType;
import rajawali.materials.shaders.AShaderBase.IGlobalShaderVar;
import rajawali.materials.shaders.fragments.animation.VertexAnimationVertexShaderFragment;


public class VertexAnimationMaterialPlugin implements IMaterialPlugin {
	public static enum VertexAnimationShaderVar implements IGlobalShaderVar {
		A_NEXT_FRAME_POSITION("aNextFramePosition", DataType.VEC4),
		A_NEXT_FRAME_NORMAL("aNextFrameNormal", DataType.VEC3),
		U_INTERPOLATION("uInterpolation", DataType.FLOAT);
		
		private String mVarString;
		private DataType mDataType;

		VertexAnimationShaderVar(String varString, DataType dataType) {
			mVarString = varString;
			mDataType = dataType;
		}

		public String getVarString() {
			return mVarString;
		}

		public DataType getDataType() {
			return mDataType;
		}
	}	
	
	private VertexAnimationVertexShaderFragment mVertexShader; 
	
	public VertexAnimationMaterialPlugin()
	{
		mVertexShader = new VertexAnimationVertexShaderFragment();
	}
	
	public IShaderFragment getVertexShaderFragment() {
		return mVertexShader;
	}

	public IShaderFragment getFragmentShaderFragment() {
		return null;
	}
	
	public void setNextFrameVertices(final int vertexBufferHandle)
	{
		mVertexShader.setNextFrameVertices(vertexBufferHandle);
	}
	
	public void setNextFrameNormals(final int normalBufferHandle)
	{
		mVertexShader.setNextFrameNormals(normalBufferHandle);
	}
	
	public void setInterpolation(double interpolation) {
		mVertexShader.setInterpolation(interpolation);
	}

	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.PRE_LIGHTING;
	}
}
