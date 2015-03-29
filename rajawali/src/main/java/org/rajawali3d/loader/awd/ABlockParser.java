package org.rajawali3d.loader.awd;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.CubeMapTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.loader.LoaderAWD.IBlockParser;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

/**
 * Base class for parsing blocks. Blocks are instantiated by the {@link AWDParser} directly and are not intended for any
 * other use case.
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public abstract class ABlockParser implements IBlockParser {

	protected static final int FLAG_BLOCK_PRECISION_MATRIX = 0x01;
	protected static final int FLAG_BLOCK_PRECISION_GEOMETRY = 0x02;
	protected static final int FLAG_BLOCK_PRECISION_PROPERTIES = 0x04;
	protected static final int FLAG_BLOCK_PRECISION_COMPRESSION = 0x08;
	protected static final int FLAG_BLOCK_PRECISION_COMPRESSION_LZMA = 0x16;
	
	private static final int BITMAP_SIZE = 8;

	private static Bitmap defaultTextureBitmap;

	static {
		defaultTextureBitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Config.RGB_565);

		// Draw a checker board pattern
		for (int i = 0; i < BITMAP_SIZE; ++i) {
			for (int j = 0; j < BITMAP_SIZE; ++j)
				defaultTextureBitmap.setPixel(i, j, ((j & 1) ^ (i & 1)) == 1 ? 0xFFFFFF : 0);
		}
	}

	protected static ATexture getDefaultCubeMapTexture() {
		return new CubeMapTexture("DefaultCubeMapTexture", new Bitmap[] { defaultTextureBitmap, defaultTextureBitmap,
				defaultTextureBitmap, defaultTextureBitmap, defaultTextureBitmap, defaultTextureBitmap });
	}

	protected static Material getDefaultMaterial() {
		return new Material();
	}

	protected static ATexture getDefaultTexture() {
		return new Texture("AWD_DefaultTexture", defaultTextureBitmap);
	}
}
