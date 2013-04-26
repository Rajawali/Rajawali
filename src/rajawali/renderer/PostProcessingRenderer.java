package rajawali.renderer;

import rajawali.Camera2D;
import rajawali.filters.IPostProcessingFilter;
import rajawali.materials.AMaterial;
import rajawali.materials.TextureInfo;
import rajawali.materials.TextureManager.TextureType;
import rajawali.math.MathUtil;
import rajawali.primitives.Plane;
import rajawali.util.RajLog;
import android.opengl.GLES20;

public final class PostProcessingRenderer {
	private int mFrameBufferHandle;
	private int mDepthBufferHandle;
	private TextureInfo mFrameBufferTexInfo;
//	private TextureInfo mDepthBufferTexInfo;
	private Plane mPostProcessingQuad;
	private Camera2D mPostProcessingCam;
	private int mTextureSize;
	private RajawaliRenderer mRenderer;
	private IPostProcessingFilter mFilter;
	private boolean mEnabled;
	private boolean mInitialized;
	private int mQuadSegments = 1;
	private PostProcessingQuality mQuality;
	
	public enum PostProcessingQuality {
		HIGH,
		MEDIUM,
		LOW,
		VERY_LOW
	}
	
	public PostProcessingRenderer(RajawaliRenderer renderer) {
		this(renderer, -1);
	}
	
	public PostProcessingRenderer(RajawaliRenderer renderer, int frameBufferTextureSize) {
		this(renderer, frameBufferTextureSize, PostProcessingQuality.MEDIUM);
	}
	
	/**
	 * 
	 * @param renderer
	 * @param frameBufferTextureSize	MathUtil.getClosestPowerOfTwo(mTextureSize) or 1024 (default)
	 */
	public PostProcessingRenderer(RajawaliRenderer renderer, int frameBufferTextureSize, PostProcessingQuality quality) {
		mRenderer = renderer;
		mTextureSize = frameBufferTextureSize;
		mQuality = quality;
	}
	
	private void create() {
		int[] frameBuffers = new int[1];
		GLES20.glGenFramebuffers(1, frameBuffers, 0);
		mFrameBufferHandle = frameBuffers[0];
		
		if(mTextureSize == -1) {
			mTextureSize = MathUtil.getClosestPowerOfTwo(mRenderer.getViewportWidth() > mRenderer.getViewportHeight() ? mRenderer.getViewportWidth() : mRenderer.getViewportHeight());
		}
		
		if(mQuality == PostProcessingQuality.MEDIUM)
			mTextureSize >>= 1;
		else if(mQuality == PostProcessingQuality.LOW)
			mTextureSize >>= 2;
		else if(mQuality == PostProcessingQuality.VERY_LOW)
			mTextureSize >>= 3;

		int[] depthBuffers = new int[1];
		GLES20.glGenRenderbuffers(1, depthBuffers, 0);
		mDepthBufferHandle = depthBuffers[0];
		
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBufferHandle);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mTextureSize, mTextureSize);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
		
		mFrameBufferTexInfo = mRenderer.getTextureManager().addTexture(null, mTextureSize, mTextureSize, TextureType.FRAME_BUFFER);

		mPostProcessingQuad = new Plane(1, 1, mQuadSegments, mQuadSegments);
		mPostProcessingQuad.setMaterial((AMaterial)mFilter);
		mPostProcessingQuad.setDoubleSided(true);
		mPostProcessingQuad.setRotZ(-90);
		mPostProcessingQuad.setRotY(180);
		mPostProcessingCam = new Camera2D();
		mPostProcessingCam.setProjectionMatrix(0, 0);

		mPostProcessingQuad.addTexture(mFrameBufferTexInfo);
		mInitialized = true;
	}
	
	public void setFilter(IPostProcessingFilter filter) {
		mFilter = filter;
	}
	
	public void bind() {
		if(!mInitialized)
			create();
		GLES20.glViewport(0, 0, mTextureSize, mTextureSize);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferHandle);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mFrameBufferTexInfo.getTextureId(), 0);
		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
			RajLog.d("Could not bind post processing frame buffer." + status);
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		}
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mDepthBufferHandle);
	}
	
	public void unbind() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
		GLES20.glViewport(0, 0, mRenderer.getViewportWidth(), mRenderer.getViewportHeight());
	}
	
	public void destroy() {
		GLES20.glDeleteFramebuffers(1, new int[] { mFrameBufferHandle }, 0);
		GLES20.glDeleteRenderbuffers(1, new int[] { mDepthBufferHandle }, 0);
		
		if (mFrameBufferTexInfo!=null) mRenderer.getTextureManager().removeTexture(mFrameBufferTexInfo);
	}
	
	public void reload() {
		if(mPostProcessingQuad != null)
			mPostProcessingQuad.reload();
		
		int[] frameBuffers = new int[1];
		GLES20.glGenFramebuffers(1, frameBuffers, 0);
		mFrameBufferHandle = frameBuffers[0];

		int[] depthBuffers = new int[1];
		GLES20.glGenRenderbuffers(1, depthBuffers, 0);
		mDepthBufferHandle = depthBuffers[0];
		
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBufferHandle);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mTextureSize, mTextureSize);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
	}
	
	public void render() {
		unbind();
		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glDepthMask(true);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		mPostProcessingQuad.render(mPostProcessingCam, mPostProcessingCam.getProjectionMatrix(), mPostProcessingCam.getViewMatrix(), null);
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public void setEnabled(boolean enabled) {
		this.mEnabled = enabled;
	}

	public boolean isInitialized() {
		return mInitialized;
	}

	public void setInitialized(boolean initialized) {
		this.mInitialized = initialized;
	}
	
	public void checkError(String message, int status) {
		StringBuffer sb = new StringBuffer();
		sb.append(message).append(" - ");
		
		switch(status)
		{

			case GLES20.GL_FRAMEBUFFER_UNSUPPORTED:
				sb.append( "OpenGL framebuffer format not supported. ");
				break;
			case GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
				sb.append( "OpenGL framebuffer missing attachment.");
				break;
			case GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
				sb.append( "OpenGL framebuffer attached images must have same dimensions.");
				break;
			case GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
				sb.append( "OpenGL framebuffer attached images must have same format.");
				break;
			case GLES20.GL_INVALID_FRAMEBUFFER_OPERATION:
				sb.append("OpenGL invalid framebuffer operation.");
				break;
			case GLES20.GL_NO_ERROR:
				sb.append("No errors.");
				break;
			case GLES20.GL_INVALID_VALUE:
				sb.append("Invalid value");
				break;
			case GLES20.GL_INVALID_OPERATION:
				sb.append("Invalid operation");
				break;
			case GLES20.GL_INVALID_ENUM:
				sb.append("Invalid enum");
				break;
			case GLES20.GL_FRAMEBUFFER_COMPLETE:
				sb.append("Framebuffer complete.");
				break;
			default:
				sb.append("OpenGL error: " + status);
				break;
		}
		
		RajLog.d(sb.toString());
	}

	public int getQuadSegments() {
		return mQuadSegments;
	}

	public void setQuadSegments(int quadSegments) {
		this.mQuadSegments = quadSegments;
	}

	public PostProcessingQuality getQuality() {
		return mQuality;
	}

	public void setQuality(PostProcessingQuality quality) {
		this.mQuality = quality;
	}
}
