package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;

/**
 * This only works for API Level 15 and higher.
 * Thanks to Lubomir Panak (@drakh)
 * <p>
 * How to use:
 * <pre><code>
 * protected void initScene() {
 * 		super.initScene();
 * 		mLight = new DirectionalLight(0, 0, 1);
 * 		mCamera.setPosition(0, 0, -17);
 * 		
 * 		VideoMaterial material = new VideoMaterial();
 * 		TextureInfo tInfo = mTextureManager.addVideoTexture();
 * 		
 * 		mTexture = new SurfaceTexture(tInfo.getTextureId());
 * 		
 * 		mMediaPlayer = MediaPlayer.create(getContext(), R.raw.nemo);
 * 		mMediaPlayer.setSurface(new Surface(mTexture));
 * 		mMediaPlayer.start();
 * 		
 * 		BaseObject3D cube = new Plane(2, 2, 1, 1);
 * 		cube.setMaterial(material);
 * 		cube.addTexture(tInfo);
 * 		cube.addLight(mLight);
 * 		addChild(cube);
 * 	}
 * 
 * 	public void onDrawFrame(GL10 glUnused) {
 * 		mTexture.updateTexImage();
 * 		super.onDrawFrame(glUnused);
 * 	}
 * </code></pre>
 *  
 * @author dennis.ippel
 * @author Lubomir Panak (@drakh)
 *
 */
public class VideoMaterial extends AMaterial {
	
	public VideoMaterial() {
		super(R.raw.video_material_vertex, R.raw.video_material_fragment, false);
		setShaders();
	}
	
	public VideoMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader, false);
		setShaders();
	}
}
