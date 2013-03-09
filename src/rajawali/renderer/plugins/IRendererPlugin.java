/**
 * 
 */
package rajawali.renderer.plugins;


/**
 * Plugin interface for the RajawaliRenderer for applying scenewide rendering
 * postprocessing effects. 
 * @author Andrew Jo
 */
public interface IRendererPlugin {
	public void destroy();
	
	public void reload();
	
	/**
	 * Called by the RajawaliRenderer. You are responsible for settings up all
	 * the necessary GL calls here to achieve your custom effect.
	 */
	public void render();
}