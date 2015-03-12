/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali.materials.textures;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rajawali.materials.AResourceManager;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.renderer.RajawaliRenderer;
import android.opengl.GLES20;

/**
 * A singleton class that keeps track of all textures used by the application. All textures will be restored when the
 * OpenGL context is being recreated. This however needs to be indicated by setting
 * {@link ATexture#shouldRecycle(boolean)} to true (which is the default). It will then keep a reference to the Bitmap
 * which means the application will take up more memory.
 * <p>
 * The advantage of storing the Bitmap in memory is that it the texture can quickly be recovered when the context is
 * restored.
 * 
 * @author dennis.ippel
 * 
 */
public final class TextureManager extends AResourceManager
{
	/**
	 * Stores the singleton instance
	 */
	private static TextureManager instance = null;
	/**
	 * A list of managed textures
	 */
	private List<ATexture> mTextureList;

	/**
	 * The constructor can only be instantiated by the TextureManager class itself.
	 */
	private TextureManager()
	{
		mTextureList = Collections.synchronizedList(new CopyOnWriteArrayList<ATexture>());
		mRenderers = Collections.synchronizedList(new CopyOnWriteArrayList<RajawaliRenderer>());
	}

	/**
	 * 
	 * @return The TextureManager instance
	 */
	public static TextureManager getInstance()
	{
		if (instance == null)
		{
			instance = new TextureManager();
		}
		return instance;
	}

	/**
	 * Adds a new {@link ATexture} to the TextureManager.
	 * 
	 * @param texture
	 * @return
	 */
	public ATexture addTexture(ATexture texture) {
		mRenderer.queueAddTask(texture);
		return texture;
	}

	/**
	 * Adds a {@link ATexture} to the TextureManager. This should only be called by {@link RajawaliRender}.
	 * 
	 * @param texture
	 */
	public void taskAdd(ATexture texture)
	{
		taskAdd(texture, false);
	}

	/**
	 * Adds a {@link ATexture} to the TextureManager. This should only be called by {@link RajawaliRender}.
	 * 
	 * @param texture
	 * @param isUpdatingAfterContextWasLost
	 */
	private void taskAdd(ATexture texture, boolean isUpdatingAfterContextWasLost)
	{
		if (!isUpdatingAfterContextWasLost)
		{
			// -- check if texture exists already
			int count = mTextureList.size();
			for (int i = 0; i < count; i++)
			{
				if (mTextureList.get(i).getTextureName().equals(texture.getTextureName()))
				{
					if (mTextureList.get(i) != texture)
						texture.setFrom(mTextureList.get(i));
					else
						return;
				}
			}
			texture.setOwnerIdentity(mRenderer.getClass().toString());
		}

		try {
			texture.add();
		} catch (TextureException e) {
			throw new RuntimeException(e);
		}

		if (!isUpdatingAfterContextWasLost)
			mTextureList.add(texture);
	}

	/**
	 * Replaces an existing {@link ATexture}.
	 * 
	 * @param texture
	 * @return
	 */
	public void replaceTexture(ATexture texture)
	{
		mRenderer.queueReplaceTask(texture, null);
	}

	/**
	 * Replaces an existing {@link ATexture}. This should only be called by {@link RajawaliRender}.
	 * 
	 * @param texture
	 * @return
	 */
	public void taskReplace(ATexture texture)
	{
		try {
			texture.replace();
		} catch (TextureException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Removes a {@link ATexture} from the TextureManager.
	 * 
	 * @param texture
	 * @return
	 */
	public void removeTexture(ATexture texture) {
		mRenderer.queueRemoveTask(texture);
	}

	/**
	 * Removes a list of {@link ATexture}s from the TextureManager.
	 * 
	 * @param texture
	 * @return
	 */
	public void removeTextures(List<ATexture> textures)
	{
		int numTextures = textures.size();

		for (int i = 0; i < numTextures; i++)
		{
			removeTexture(textures.get(i));
		}
	}

	/**
	 * Removes a {@link ATexture} from the TextureManager. This should only be called by {@link RajawaliRender}.
	 * 
	 * @param texture
	 * @return
	 */
	public void taskRemove(ATexture texture)
	{
		try {
			texture.remove();
		} catch (TextureException e) {
			throw new RuntimeException(e);
		}
		mTextureList.remove(texture);
	}

	/**
	 * Restores all textures that are managed by the TextureManager. All textures will be restored when the OpenGL
	 * context is being recreated. This however needs to be indicated by setting {@link ATexture#shouldRecycle(boolean)}
	 * to true (which is the default). It will then keep a reference to the Bitmap which means the application will take
	 * up more memory.
	 * <p>
	 * The advantage of storing the Bitmap in memory is that it the texture can quickly be recovered when the context is
	 * restored.
	 */
	public void reload()
	{
		mRenderer.queueReloadTask(this);
	}

	/**
	 * Restores all textures that are managed by the TextureManager. This should only be called by
	 * {@link RajawaliRender}.
	 */
	public void taskReload()
	{
		int len = mTextureList.size();
		for (int i = 0; i < len; i++)
		{
			ATexture texture = mTextureList.get(i);
			if (texture.willRecycle())
			{
				mTextureList.remove(i);
				i -= 1;
				len -= 1;
			}
			else
			{
				taskAdd(texture, true);
			}
		}
	}

	/**
	 * Completely resets the TextureManager. Disposes the Bitmaps and removes all references.
	 */
	public void reset()
	{
		mRenderer.queueResetTask(this);
	}

	/**
	 * Completely resets the TextureManager. This should only be called by {@link RajawaliRender}.
	 */
	public void taskReset()
	{
		try {
			int count = mTextureList.size();

			int[] textures = new int[count];
			for (int i = 0; i < count; i++)
			{
				ATexture texture = mTextureList.get(i);

				if (texture.getOwnerIdentity().equals(mRenderer.getClass().toString()) || texture.willRecycle())
				{
					texture.reset();
					textures[i] = texture.getTextureId();
					mTextureList.remove(i);
					i -= 1;
					count -= 1;
				}
			}
			
			if(RajawaliRenderer.hasGLContext())
				GLES20.glDeleteTextures(count, textures, 0);

			if (mRenderers.size() > 0)
			{
				mRenderer = mRenderers.get(mRenderers.size() - 1);
				reload();
			} else {
				mTextureList.clear();
			}
		} catch (TextureException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Completely resets the TextureManager. This should only be called by {@link RajawaliRender}.
	 * 
	 * @param renderer
	 */
	public void taskReset(RajawaliRenderer renderer)
	{
		if (mRenderers.size() == 0)
		{
			taskReset();
		}
	}

	/**
	 * Returns the number of textures currently managed.
	 * 
	 * @return
	 */
	public int getNumTextures() {
		return mTextureList.size();
	}


	public TYPE getFrameTaskType() {
		return TYPE.TEXTURE_MANAGER;
	}
}
