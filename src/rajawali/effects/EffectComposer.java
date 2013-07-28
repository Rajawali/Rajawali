package rajawali.effects;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rajawali.Camera2D;
import rajawali.materials.textures.ATexture.FilterType;
import rajawali.materials.textures.ATexture.WrapType;
import rajawali.primitives.Plane;
import rajawali.primitives.ScreenQuad;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.scene.RajawaliScene;
import rajawali.scenegraph.IGraphNode.GRAPH_TYPE;
import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.opengl.GLES20;
import android.view.Display;
import android.view.WindowManager;

public class EffectComposer {
	protected RajawaliRenderer mRenderer;
	protected RenderTarget mRenderTarget1;
	protected RenderTarget mRenderTarget2;
	protected RenderTarget mReadBuffer;
	protected RenderTarget mWriteBuffer;
	
	protected List<APass> mPasses;
	
	protected ShaderPass mCopyPass;
	
	protected Camera2D mCamera = new Camera2D();
	protected ScreenQuad mPostProcessingQuad = new ScreenQuad();
	protected RajawaliScene mScene = new RajawaliScene(mRenderer, GRAPH_TYPE.NONE);
	
	public EffectComposer(RajawaliRenderer renderer, RenderTarget renderTarget) {
		mRenderer = renderer;
		if (renderTarget == null) {
			int width, height;
			if (renderer.getSceneInitialized()) {
				width = mRenderer.getViewportWidth();
				height = mRenderer.getViewportHeight();
			} else {
				WindowManager wm = (WindowManager)renderer.getContext()
						.getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				width = size.x;
				height = size.y;
			}
			mRenderTarget1 = new RenderTarget(width, height, 0, 0, true, 
					false, true, GLES20.GL_UNSIGNED_BYTE, Config.RGB_565,
					FilterType.LINEAR, WrapType.CLAMP);
		} else {
			mRenderTarget1 = renderTarget;
		}
		
		mRenderTarget2 = mRenderTarget1.clone();
		
		mWriteBuffer = mRenderTarget1;
		mReadBuffer = mRenderTarget2;
		
		mPasses = Collections.synchronizedList(new CopyOnWriteArrayList<APass>());
		
		mCamera.setProjectionMatrix(0, 0);
		
		// Set up a scene with just a 2D camera and a fullscreen quad.
		mPostProcessingQuad.setRotZ(90);
		mScene.addChild(mPostProcessingQuad);
		mScene.replaceAndSwitchCamera(mScene.getCamera(), mCamera);
	}
	
	public EffectComposer(RajawaliRenderer renderer) {
		this(renderer, null);
	}
	
	/**
	 * Swaps read and write buffers.
	 */
	public void swapBuffers() {
		RenderTarget tmp = mReadBuffer;
		mReadBuffer = mWriteBuffer;
		mWriteBuffer = tmp;
	}
	
	public void addPass(APass pass) {
		mPasses.add(pass);
	}
	
	public void insertPass(APass pass, int index) {
		mPasses.add(index, pass);
	}
	
	public void removePass(APass pass) {
		mPasses.remove(pass);
	}
	
	public void setSize(int width, int height) {
		RenderTarget renderTarget = mRenderTarget1.clone();
		renderTarget.setWidth(width);
		renderTarget.setHeight(height);
		reset(renderTarget);
	}
	
	public void reset(RenderTarget renderTarget) {
		if (renderTarget == null) {
			int width, height;
			if (mRenderer.getSceneInitialized()) {
				width = mRenderer.getViewportWidth();
				height = mRenderer.getViewportHeight();
			} else {
				WindowManager wm = (WindowManager)mRenderer.getContext()
						.getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				width = size.x;
				height = size.y;
			}
			mRenderTarget1 = new RenderTarget(width, height, 0, 0, true, 
					false, true, GLES20.GL_UNSIGNED_BYTE, Config.RGB_565,
					FilterType.LINEAR, WrapType.CLAMP);
		} else  {
			mRenderTarget1 = renderTarget;
		}
		
		mRenderTarget2 = mRenderTarget1.clone();
		mWriteBuffer = mRenderTarget1;
		mReadBuffer = mRenderTarget2;
	}
	
	public void render(double deltaTime) {
		mWriteBuffer = mRenderTarget1;
		mReadBuffer = mRenderTarget2;
		
		boolean maskActive = false;
		
		APass pass;
		
		for (int i = 0; i < mPasses.size(); i++) {
			pass = mPasses.get(i);
			if (!pass.isEnabled()) continue;
			
			pass.render(mRenderer, mWriteBuffer, mReadBuffer, deltaTime);
			
			if (pass.needsSwap()) {
				if (maskActive) {
					GLES20.glStencilFunc(GLES20.GL_NOTEQUAL, 1, 0xffffffff);
					
					// TODO: Add ShaderPass stuff here
					// mCopyPass.render(mRenderer, mWriteBuffer, mReadBuffer, deltaTime);
					
					GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, 0xffffffff);
				}
				
				swapBuffers();
			}
			
			// If the current pass is a mask pass, notify the next pass that mask is active.
			if (pass instanceof MaskPass)
				maskActive = true;
			else if (pass instanceof ClearMaskPass)
				maskActive = false;
		}
	}
	
	public boolean isEmpty() {
		return mPasses.isEmpty();
	}
	
	public RajawaliScene getScene() {
		return mScene;
	}
}
