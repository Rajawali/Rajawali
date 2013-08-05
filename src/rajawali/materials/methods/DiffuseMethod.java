package rajawali.materials.methods;


public class DiffuseMethod {
	
	public static class Lambert implements IDiffuseMethod
	{
		private float mIntensity;
		
		public Lambert()
		{
			this(0.8f);
		}
		
		public Lambert(float intensity)
		{
			mIntensity = intensity;
		}
		
		public float getIntensity()
		{
			return mIntensity;
		}
		
		public void setIntensity(float intensity)
		{
			mIntensity = intensity;
		}
	}
}
