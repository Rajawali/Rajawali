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
/** 
 * @author David Trounstine (david@evvid.com)
 */
package rajawali.materials.textures;

import rajawali.materials.textures.TexturePacker.Tile;
import android.graphics.Bitmap;

public class TextureAtlas {
	/**
	 * Atlas width
	 */
	protected float mWidth;
	/**
	 * Atlas height
	 */
	protected float mHeight;
	/**
	 * Atlas texture 
	 */
	protected Bitmap[] mPages;
	/**
	 * Whether compression is used
	 */
	protected boolean mUsesCompression;
	/**
	 * Array of tiles
	 */
	protected Tile[] mTiles;

	/**
	 * Constructor requires a Power of Two <code>width</code> and <code>height</code>
	 * <code>usesCompression</code> should be true if the atlas should be compressed
	 * 
	 * @param width
	 * @param height
	 * @param atlasPages
	 * @param atlasTiles
	 * @param usesCompression
	 */
	public TextureAtlas(int width, int height, Boolean usesCompression) {
		mWidth = width;
		mHeight = height;	
		mUsesCompression = usesCompression;
		mPages = null;
		mTiles = null;
	}
	/**
	 * Get the atlas width.
	 * 
	 * @return {@link float}
	 */
	public float getWidth() {
		return mWidth;
	}
	/**
	 * Set the atlas width.
	 * 
	 * @param width
	 */
	public void setWidth(float width) {
		mWidth = width;
	}
	/**
	 * Get the atlas height.
	 * 
	 * @return {@link float}
	 */
	public float getHeight() {
		return mHeight;
	}
	/**
	 * Set the atlas height.
	 * 
	 * @param height
	 */
	public void setHeight(int height) {
		mHeight = height;
	}
	/**
	 * Get all atlas pages as a Bitmap.
	 * This will return null if compression is used.
	 * 
	 * @return {@link Bitmap}
	 */
	public Bitmap[] getPages() {
		return mPages;
	}
	/**
	 * Set the atlas pages.
	 * 
	 * @param bitmap
	 */
	protected void setPages(Bitmap[] pages) {
		mPages = pages;
	}
	/**
	 * Returns <code>true</code> if compression is used.
	 * 
	 * @return <code>True</code> if compression is used; <code>false</code> otherwise.
	 */
	public boolean getUsesCompression() {
		return mUsesCompression;
	}
	/**
	 * Set <code>true</code> if compression is used.
	 * 
	 * @param compress
	 */
	protected void setUsesCompression(boolean compress) {
		mUsesCompression = compress;
	}
	/**
	 * Get an array of tiles that compose the atlas.
	 * 
	 * @return {@link Tile}
	 */
	public Tile[] getTiles() {
		return mTiles;	
	}
	/**
	 * Set the tile array.
	 * 
	 * @param tiles
	 */
	protected void setTiles(Tile[] tiles) {
		mTiles = tiles;		
	}
	/**
	 * Get the tile with the requested <code>name</code>
	 * 
	 * @param name
	 * @return {@link Tile}
	 */
	public Tile getTileNamed(String name) {
		for(int i = 0; i < mTiles.length; i++) {
			if(name.equals(mTiles[i].name))
				return mTiles[i];
		}
		return null;
	}	
}
