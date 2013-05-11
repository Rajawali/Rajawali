package rajawali.materials.textures;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rajawali.materials.textures.ATexture.TextureException;
import rajawali.renderer.AFrameTask;
import rajawali.renderer.RajawaliRenderer;
import android.content.Context;
import android.opengl.GLES20;

/**
 * @author dennis.ippel
 * 
 */
public final class TextureManager extends AFrameTask {
	private static TextureManager instance = null;

	private Context mContext;
	private RajawaliRenderer mRenderer;

	private List<ATexture> mTextureList;

	//private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

	private TextureManager()
	{
		mTextureList = Collections.synchronizedList(new CopyOnWriteArrayList<ATexture>());
	}

	public static TextureManager getInstance()
	{
		if(instance == null)
		{
			instance = new TextureManager();
		}
		return instance;
	}
	
	public ATexture addTexture(ATexture texture) {
		mRenderer.queueAddTask(texture);
		return texture;
	}

	public void taskAdd(ATexture texture)
	{
		try {
			texture.add();
		} catch (TextureException e) {
			throw new RuntimeException(e);
		}
		// TODO: Check texture name
		mTextureList.add(texture);
	}

	public void replaceTexture(ATexture texture)
	{
		mRenderer.queueReplaceTask(texture, null);
	}

	public void taskReplace(ATexture texture)
	{
		try {
			texture.replace();
		} catch(TextureException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void removeTexture(ATexture texture) {
		mRenderer.queueRemoveTask(texture);
	}

	public void removeTextures(List<ATexture> textures)
	{
		int numTextures = textures.size();

		for (int i = 0; i < numTextures; i++)
		{
			removeTexture(textures.get(i));
		}
	}

	public void taskRemove(ATexture texture)
	{
		try {
			texture.remove();
		} catch(TextureException e) {
			throw new RuntimeException(e);
		}
		mTextureList.remove(texture);
	}

	public void reload()
	{
		mRenderer.queueReloadTask(this);
	}
	
	public void taskReload()
	{
		int len = mTextureList.size();
		for(int i=0; i<len; i++)
		{
			ATexture texture = mTextureList.get(i);
			taskAdd(texture);
		}
	}

	public void reset()
	{
		mRenderer.queueResetTask(this);
	}

	public void taskReset()
	{
		try {
			int count = mTextureList.size();
			int[] textures = new int[count];
			for(int i=0; i<count; i++)
			{
				ATexture texture = mTextureList.get(i);
				texture.reset();
				textures[i] = texture.getTextureId();
			}
			
			GLES20.glDeleteTextures(count, textures, 0);
			mTextureList.clear();
		} catch(TextureException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void validateTextures()
	{
		// TODO: validate
	}

	/**
	 * Returns the number of textures currently managed.
	 * 
	 * @return
	 */
	public int getNumTextures() {
		return mTextureList.size();
	}
	
	public void setContext(Context context)
	{
		mContext = context;
	}
	
	public Context getContext()
	{
		return mContext;
	}

	public void setRenderer(RajawaliRenderer renderer)
	{
		mRenderer = renderer;
	}
	
	public RajawaliRenderer getRenderer()
	{
		return mRenderer;
	}
	
	public TYPE getFrameTaskType() {
		return TYPE.TEXTURE_MANAGER;
	}		
	
	public static class TextureManagerException extends Exception
	{
		private static final long serialVersionUID = 2046770147250128945L;

		public TextureManagerException() {
			super();
		}

		public TextureManagerException(final String msg) {
			super(msg);
		}

		public TextureManagerException(final Throwable throwable) {
			super(throwable);
		}

		public TextureManagerException(final String msg, final Throwable throwable) {
			super(msg, throwable);
		}
	}
}
