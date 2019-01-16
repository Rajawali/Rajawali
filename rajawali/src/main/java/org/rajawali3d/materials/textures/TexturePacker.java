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
/* 
 * Derived from Texture Atlas Generator by Lukasz Brunn - lukasz.dk
 * See <a href="https://github.com/lukaszdk/texture-atlas-generator">https://github.com/lukaszdk/texture-atlas-generator</a>
 */

package org.rajawali3d.materials.textures;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import org.rajawali3d.util.RajLog;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * @author David Trounstine (david@evvid.com)
 */
public class TexturePacker{
	/**
	 * Application context
	 */
	private Context mContext;
	/**
	 * InputStream array for loading bitmaps
	 */
	private InputStream[] mInStreams; 
	/**
	 * Array of names of files to pack
	 */
	private String[] mFileNames;
	/**
	 * Total number of files to pack
	 */
	private int mFileCount;
	/**
	 * Padding around each packed texture
	 */
	private int mPadding;
	/**
	 * Width of each atlas page
	 */
	private int mAtlasWidth;
	/**
	 * Height of each atlas page
	 */
	private int mAtlasHeight;
	/**
	 * Whether resources have been set
	 */
	private boolean mResourcesSet = false;
	/**
	 * Options for processing bitmap data
	 */
	private BitmapFactory.Options BFO = new BitmapFactory.Options();
	/**
	 * Array of atlas page bitmaps
	 */
	private Bitmap[] mAtlasBitmapPages;
	/**
	 * Constructor does nothing except pass in the application context
	 * 
	 * @param context 
	 */
	public TexturePacker(Context context) {
		mContext = context;
	}
	/**
	 * Used for loading images from assets.
	 * If <code>subDir</code> is blank the root <code>assets</code> folder will be searched
	 * Returns a packed <code>TextureAtlas</code>
	 * 
	 * @param atlasWidth
	 * @param altasHeight
	 * @param padding
	 * @param useCompresison
	 * @param subDir
	 * @return
	 */
	public TextureAtlas packTexturesFromAssets(int atlasWidth, int altasHeight, int padding, boolean useCompresison, String subDir) {
		assetsToStreams(subDir);
		return createAtlas(atlasWidth, altasHeight, padding, useCompresison);
	}
	/**
	 * Used for loading images from an array of resourceIDs.
	 * Returns a packed <code>TextureAtlas</code> 
	 * 
	 * @param atlasWidth
	 * @param altasHeight
	 * @param padding
	 * @param useCompresison
	 * @param resourceIDs
	 * @return
	 */
	public TextureAtlas packTexturesFromResources(int atlasWidth, int altasHeight, int padding, boolean useCompresison, int[] resourceIDs) {
		resIDsToStreams(resourceIDs);
		return createAtlas(atlasWidth, altasHeight, padding, useCompresison);
	}
	/*
	 * Build atlas from <code>setResources(InputStream[] inStreams)</code>
	 */
	@SuppressWarnings("unused")
	private TextureAtlas createAtlas(int atlasWidth, int atlasHeight, int padding, boolean useCompression) {
		mPadding = padding;
		mAtlasWidth = atlasWidth;
		mAtlasHeight = atlasHeight;

		if(!mResourcesSet)
			throw new RuntimeException("ERROR: Resources must be set before packing can begin.");

		TextureAtlas tAtlas = new TextureAtlas(mAtlasWidth, mAtlasHeight, useCompression);
		/*
		 * Read InputStreams and convert to bitmaps for data processing
		 * bitmap decoding is delayed for efficiency
		 */
		Tile[] atlasTiles = new Tile[mFileCount];
		BFO.inJustDecodeBounds = true;

		for(int i = 0; i < mFileCount; i ++) {
			Bitmap texture = null;
			Tile tile = new Tile(null, mFileNames[i], 0, 0, 0, 0 );
			tile.stream = mInStreams[i];
			tile.name = mFileNames[i];
			try {
				texture = BitmapFactory.decodeStream(tile.stream, null, BFO);
			} catch (Exception e) {
				RajLog.e("Unable to read "+tile.name+" from stream.");
			}
			tile.width = BFO.outWidth;
			tile.height = BFO.outHeight;
			atlasTiles[i] = tile;
		}
		RajLog.i("Found " + mFileCount + " images to sort and pack.");		
		/*
		 * Sort bitmaps by size
		 */
		Comparator<Tile> tileCompare = new TileComparator();
		Arrays.sort(atlasTiles, tileCompare);
		/*
		 * Resample any images larger than the atlas.
		 * Resampling is limited to powers of two.
		 */
		for(int i = 0; i < mFileCount; i ++) {
			Bitmap texture = null;
			Tile tile = atlasTiles[i];
			BFO.inSampleSize = 1;

			while(tile.width > mAtlasWidth || tile.height > mAtlasHeight) {
				RajLog.w("File: '" + tile.name + "' (" + tile.width + "x" + tile.height + ") is larger than the atlas (" 
						+ mAtlasWidth + "x" + mAtlasHeight + ")\nResizing to " + (tile.width/2) + " " + (tile.height/2));
				BFO.inSampleSize *= 2;
				try {
					texture = BitmapFactory.decodeStream(tile.stream, null, BFO);
					} catch (Exception e) {
						RajLog.e("Unable to read "+mFileNames[i]+" from stream.");
					}
				tile.width = BFO.outWidth;
				tile.height = BFO.outHeight;
			}
			tile.setSampling(BFO.inSampleSize);
			atlasTiles[i] = tile;
		}
		/*
		 * Pass the tiles without Bitmap data to the TextureAtlas object to be returned
		 */
		tAtlas.setTiles(atlasTiles);
		/*
		 * Pack the sorted textures and draw the atlas to Bitmap
		 * Repeat if additional atlas images are required
		 */
		mAtlasBitmapPages = packAtlas(atlasTiles, useCompression);

		tAtlas.setPages(mAtlasBitmapPages);
		return tAtlas;
	}

	private Bitmap[] packAtlas(Tile[] atlasTiles, boolean useCompression) {
		ArrayList<Bitmap> pageCollection = new ArrayList<Bitmap>();
		Bitmap atlasPage = Bitmap.createBitmap(mAtlasWidth, mAtlasHeight, Bitmap.Config.ARGB_8888);
		Canvas atlasCanvas = new Canvas(atlasPage); 
		Node root = new Node(0,0, mAtlasWidth, mAtlasHeight);
		int pageNum = 0;
		checkPOT(atlasPage, "Atlas Page "+pageNum);
		/*
		 * Loop through array of Tiles and attempt to insert each into a binary tree.
		 * When the atlas page is filled a new page is created.
		 */
		for(int i = 0; i < mFileCount; i++) {
			Bitmap tileImage = null;
			BFO.inJustDecodeBounds = false;
			BFO.inSampleSize = 1;
			Tile tile = atlasTiles[i];

			Node node = root.Insert(tile);

			if(node != null) {
				BFO.inSampleSize = tile.getSampling();
				try {
					tile.stream.reset();
					tileImage = BitmapFactory.decodeStream(tile.stream, null, BFO);
				} catch (Exception e) {
					RajLog.e("Unable to read "+tile.name+" from stream.");
				}
				tile.x = node.rect.left;
				tile.y = node.rect.top;
				checkPOT(tileImage, tile.name);
				atlasCanvas.drawBitmap(tileImage, tile.x, tile.y, null);
				tileImage = null;
			}
			else {
				pageCollection.add(atlasPage);
				atlasPage = Bitmap.createBitmap(mAtlasWidth, mAtlasHeight, Bitmap.Config.ARGB_8888);
				atlasCanvas = new Canvas(atlasPage); 
				checkPOT(atlasPage, "Atlas Page "+pageNum);
				root = new Node(0,0, mAtlasWidth, mAtlasHeight);
				pageNum++;
			}
			tile.setPage(pageNum);
		}
		pageCollection.add(atlasPage);
		Bitmap[] atlasPages = pageCollection.toArray(new Bitmap[pageCollection.size()]);
		return atlasPages;
	}
	/*
	 * Retrieve asset names from files in <code>subDirName</code>
	 * collect file names and number of files
	 * and set each file as an <code>InputStream</code> to the <code>files</code> array
	 */
	private void assetsToStreams(String subDirName) {
		AssetManager am = mContext.getAssets();
		try {
			mFileNames = am.list(subDirName);
		} catch (Exception e) {
			 RajLog.e("Unable to read files from assets/"+subDirName+".");
		}
		mFileCount = mFileNames.length;
		if(mFileCount == 0)
			RajLog.e("No assets found");
		else {
			mFileCount = mFileNames.length;		
			InputStream[] streams = new InputStream[mFileCount];
			for( int i = 0; i < mFileCount; i++) {
				try {
					streams[i] = am.open(subDirName+"/"+mFileNames[i]);
					mFileNames[i] = mFileNames[i].substring(0, mFileNames[i].indexOf("."));
				} catch (Exception e) {
					 RajLog.e("Unable to open file: assets/"+subDirName+"/"+mFileNames[i]+".");
				}
			}
			setStreams(streams);
		}
	}
	/*
	 * Retrieve array of resource IDs, collect file names and number of files
	 * and set each file as an <code>InputStream</code> to the <code>files</code> array
	 */
	private void resIDsToStreams(int[] resourceIDs) {
		mFileCount = resourceIDs.length;
		mFileNames = new String[mFileCount];
		if(mFileCount == 0)
			RajLog.e("No resources found");
		else{
			InputStream[] is = new InputStream[mFileCount];
			for(int i = 0; i < mFileCount; i++){
				is[i] = mContext.getResources().openRawResource(resourceIDs[i]);
				mFileNames[i] = mContext.getResources().getResourceEntryName(resourceIDs[i]);
			}
			setStreams(is);
		}
	}
	/*
	 * Set the <code>InputStream</code> objects to be packed
	 */
	private void setStreams(InputStream[] inStreams) {
		mInStreams = inStreams;
		mResourcesSet = true;		
	}
	/*
	 * Check that bitmaps are power of two sizes
	 */
	private static final void checkPOT(final Bitmap bitmap, String name) {
		final int x = bitmap.getWidth();
		final int y = bitmap.getHeight();
		if (!((x != 0) && (x & (x - 1)) == 0) || !((y != 0) && (y & (y - 1)) == 0))
			RajLog.w("Loaded texture "+name+" is not a power of two! Texture may fail to render on certain devices.");
	}

	/*
	 * Packing algorithm derived from http://www.blackpawn.com/texts/lightmaps/
	 */
	private class Node {
		protected Rect rect;
		protected Node child[];
		protected Tile tile;

		protected Node(int x, int y, int width, int height ) {
			rect = new Rect(x, y, x+width, y+height);
			child = new Node[2];
			child[0] = null;
			child[1] = null;
			tile = null;
		}

		protected boolean isLeaf() {
			return child[0] == null && child[1] == null;
		}

		protected Node Insert(Tile tile) {
			if(!isLeaf()) {				
				Node node = child[0].Insert(tile);
				if(node != null)
					return node;
				return child[1].Insert(tile);
			} else {
				if(this.tile != null)
					return null;

				if(tile.width > rect.width() || tile.height > rect.height())
					return null;

				if(tile.width == rect.width() && tile.height == rect.height()) {
					this.tile = tile;
					return this;
				}

				int dw = rect.width() - tile.width;
				int dh = rect.height() - tile.height;

				if(dw > dh) {
					child[0] = new Node(rect.left, rect.top, tile.width, rect.height());
					child[1] = new Node(mPadding + rect.left + tile.width, rect.top, rect.width() - tile.width - mPadding, rect.height());
				} else {
					child[0] = new Node(rect.left, rect.top, rect.width(), tile.height);
					child[1] = new Node(rect.left, mPadding + rect.top + tile.height, rect.width(), rect.height() - tile.height - mPadding);
				}
				return child[0].Insert(tile);
			}
		}
	}
	/**
	 * <code>Tile</code> is a container used to store pertinent data about each packed image.
	 * 
	 * @author David Trounstine  (david@evvid.com)
	 */
	public class Tile {
		public InputStream stream;
		public String name;
		public int x;
		public int y;
		public int width;
		public int height;
		protected int page;
		protected int sampling = 1;

		protected Tile(InputStream inStream, String name, int x, int y, int width, int height)	{
			this.stream = inStream;
			this.name = name;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public Bitmap getPage() {
			return mAtlasBitmapPages[page];
		}

		protected void setPage(int page) {
			this.page = page;
		}

		protected int getSampling() {
			return this.sampling;
		}
		
		protected void setSampling(int sampling) {
			this.sampling = sampling;
		}

	}
	/*
	 * Comparator to sort tiles into descending order by size/area
	 */
	private class TileComparator implements Comparator<Tile> {
		public int compare(Tile t1, Tile t2) {
			int a1 = t1.width * t1.height;
			int a2 = t2.width * t1.height;

			if(a1 != a2)
				return a2 - a1;
			else
				return t1.name.compareTo(t2.name);
		}
	}
}
