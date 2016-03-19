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
package org.rajawali3d.materials;

import android.content.Context;
import org.rajawali3d.renderer.Renderer;

import java.util.List;


public abstract class AResourceManager {
	/**
	 * The application context
	 */
	protected Context        mContext;
	/**
	 * The current renderer
	 */
	protected Renderer       mRenderer;
	/**
	 * A list of {@link Renderer} instances that use the TextureManager
	 */
	protected List<Renderer> mRenderers;

	/**
	 * Registers a {@link Renderer} instance that will start using the TextureManager.
	 *
	 * @param renderer
	 */
	public void registerRenderer(Renderer renderer)
	{
        boolean alreadyRegistered = false;

        for (int i=0;i<mRenderers.size();i++) {
            if (renderer == mRenderers.get(i)) {
                alreadyRegistered = true;
                break;
            }
        }

        if (!alreadyRegistered) {
            mRenderers.add(renderer);
        }

		mRenderer = renderer;
	}

	/**
	 * Unregisters a {@link Renderer} instance that will stop using the TextureManager.
	 *
	 * @param renderer
	 */
	public void unregisterRenderer(Renderer renderer)
	{
		mRenderers.remove(renderer);
	}

	/**
	 * Sets the application context
	 *
	 * @param context
	 */
	public void setContext(Context context)
	{
		mContext = context;
	}

	/**
	 * Returns the application context.
	 *
	 * @return
	 */
	public Context getContext()
	{
		return mContext;
	}

	/**
	 * Gets the current renderer.
	 *
	 * @return
	 */
	public Renderer getRenderer()
	{
		return mRenderer;
	}
}
