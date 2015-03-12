package rajawali.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;


/**
 * Internal class for managing shader loading.
 * 
 * This class is mostly internal unfortunately loading of resources requires context so this class has help from
 * AMaterial to statically pass the context and store it as a weak reference. Unfortunately there is no way around
 * this that I can see but I am open for suggestions of a better solution.
 * 
 * @author Ian Thomas (toxicbakery aka damnusernames http://toxicbakery.com/)
 * 
 */
public final class RawShaderLoader {

	@SuppressLint("UseSparseArrays")
	private static final HashMap<Integer, String> mRawMaterials = new HashMap<Integer, String>();

	// Prevent memory leaks as referencing the context can be dangerous.
	public static WeakReference<Context> mContext;

	/**
	 * Read a material from the raw resources folder. Subsequent calls will return from memory.
	 * 
	 * @param resID
	 * @return
	 */
	public static final String fetch(final int resID) {
		if (mRawMaterials.containsKey(resID))
			return mRawMaterials.get(resID);

		final StringBuilder sb = new StringBuilder();

		try {
			final Resources res = mContext.get().getResources();
			final InputStreamReader isr = new InputStreamReader(res.openRawResource(resID));
			final BufferedReader br = new BufferedReader(isr);

			String line;
			while ((line = br.readLine()) != null)
				sb.append(line).append("\n");

			mRawMaterials.put(resID, sb.toString());

			isr.close();
			br.close();
		} catch (Exception e) {
			RajLog.e("Failed to read material: " + e.getMessage());
			e.printStackTrace();
		}

		return mRawMaterials.get(resID);
	}
}
