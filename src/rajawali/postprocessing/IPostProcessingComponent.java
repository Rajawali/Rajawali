package rajawali.postprocessing;


public interface IPostProcessingComponent {
	public static enum PostProcessingComponentType {
		PASS, EFFECT
	};
	
	boolean isEnabled();
	PostProcessingComponentType getType();
}
