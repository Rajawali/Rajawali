package org.rajawali3d.loader.awd;

import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.loader.awd.exceptions.NotImplementedParsingException;
import org.rajawali3d.util.RajLog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockBitmapTexture extends ABlockParser {

	public static final byte IMAGE_TYPE_EXTERNAL = 0x0;
	public static final byte IMAGE_TYPE_EMBEDDED = 0x1;

	protected String mLookupName;
	protected byte mImageType;
	protected long mDataLength;
	protected Bitmap mBitmap;

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		// Lookup name
		mLookupName = dis.readVarString();

		// Image type
		mImageType = dis.readByte();

		// Data length
		mDataLength = dis.readUnsignedInt();

		// Debug
        if (RajLog.isDebugEnabled()) {
            RajLog.d("  Lookup Name: " + mLookupName);
            RajLog.d("  Data Length: " + mDataLength);
        }

		switch (mImageType) {
		case IMAGE_TYPE_EXTERNAL:
			throw new NotImplementedParsingException();
		case IMAGE_TYPE_EMBEDDED:
			byte[] imageData = new byte[(int) mDataLength];
			dis.readFully(imageData);
			mBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
			break;
		}

		dis.readProperties();
		dis.readUserAttributes(null);
	}

	public Bitmap getTexture() {
		return mBitmap;
	}

}
