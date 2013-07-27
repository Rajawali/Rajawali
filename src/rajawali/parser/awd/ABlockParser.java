package rajawali.parser.awd;

import rajawali.materials.AMaterial;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.CubeMapTexture;
import rajawali.materials.textures.Texture;
import rajawali.parser.AWDParser;
import rajawali.parser.AWDParser.IBlockParser;
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

	private static Bitmap defaultTextureBitmap;

	protected static ATexture getDefaultCubeMapTexture() {
		initDefaultTexture();
		return new CubeMapTexture("DefaultCubeMapTexture", new Bitmap[] { defaultTextureBitmap, defaultTextureBitmap,
				defaultTextureBitmap, defaultTextureBitmap, defaultTextureBitmap, defaultTextureBitmap });
	}
	
	protected static AMaterial getDefaultMaterial() {
		return new SimpleMaterial();
	}

	protected static ATexture getDefTexture() {
		initDefaultTexture();
		return new Texture("DefaultTexture", defaultTextureBitmap);
	}

	private static final void initDefaultTexture() {
		if (defaultTextureBitmap == null) {
			defaultTextureBitmap = Bitmap.createBitmap(8, 8, Config.ARGB_4444);

			// Draw a checker board pattern
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if (((j & 1) ^ (i & 1)) == 1)
						defaultTextureBitmap.setPixel(i, j, 0xFFFFFFFF);
				}
			}
		}
	}
}
