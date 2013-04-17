package rajawali.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import rajawali.renderer.RajawaliRenderer;
import android.content.res.Resources;
import android.os.Environment;

public abstract class AParser implements IParser {
	protected Resources mResources;
	protected int mResourceId;
	protected String mFileOnSDCard;
	protected File mFile;

	public AParser(RajawaliRenderer renderer, String fileOnSDCard)
	{
		this(renderer.getContext().getResources(), 0);
		mFileOnSDCard = fileOnSDCard;
	}
	
	public AParser(RajawaliRenderer renderer, int resourceId)
	{
		this(renderer.getContext().getResources(), resourceId);
	}
	
	public AParser(Resources resources, int resourceId) 
	{
		mResources = resources;
		mResourceId = resourceId;
	}
	
	public IParser parse() throws ParsingException {
		if(mFileOnSDCard != null) {
			File sdcard = Environment.getExternalStorageDirectory();
			mFile = new File(sdcard, mFileOnSDCard);
		}
		return this;
	}
	
	protected String readString(InputStream stream) throws IOException {
        String result = new String();
        byte inByte;
        while ((inByte = (byte) stream.read()) != 0)
                result += (char) inByte;
        return result;
	}
	
	protected int readInt(InputStream stream) throws IOException {
	        return stream.read() | (stream.read() << 8) | (stream.read() << 16)
	                        | (stream.read() << 24);
	}
	
	protected int readShort(InputStream stream) throws IOException {
	        return (stream.read() | (stream.read() << 8));
	}
	
	protected float readFloat(InputStream stream) throws IOException {
	        return Float.intBitsToFloat(readInt(stream));
	}
	
	protected String getOnlyFileName(String fileName) {
		String fName = new String(fileName);
		int indexOf = fName.lastIndexOf("\\");
		if(indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		indexOf = fName.lastIndexOf("/");
		if(indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		return fName.toLowerCase(Locale.ENGLISH).replaceAll("\\s", "_");
	}
	
	protected String getFileNameWithoutExtension(String fileName) {
		String fName = fileName.substring(0, fileName.lastIndexOf("."));
		int indexOf = fName.lastIndexOf("\\");
		if(indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		indexOf = fName.lastIndexOf("/");
		if(indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		return fName.toLowerCase(Locale.ENGLISH).replaceAll("\\s", "_");
	}
	
	public static class ParsingException extends Exception {
		private static final long serialVersionUID = 3732833696361901287L;
		
		public ParsingException() {
			super();
		}
		
		public ParsingException(final String msg) {
			super(msg);
		}
		
		public ParsingException(final Throwable throwable) {
			super(throwable);
		}
		
		public ParsingException(final String msg, final Throwable throwable) {
			super(msg, throwable);
		}
		
	}
}
