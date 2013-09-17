package rajawali.postprocessing;

import java.util.List;



public interface IPostProcessingEffect extends IPostProcessingComponent {
	void addPass(IPass pass);
	void insertPass(IPass pass, int index);
	void removePass(IPass pass);
	List<IPass> getPasses();
	void setManager(PostProcessingManager manager);
}
