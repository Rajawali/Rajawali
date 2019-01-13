/**
 * Copyright 2013 Free Beachler, Longevity Software d.b.a. Terawatt Industries
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
package org.rajawali3d.loader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Stack;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.loader.LoaderSTL.StlParseException;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.RajLog;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.util.Log;

/**
 * @author fbeachler
 *
 */
public class LoaderGCode extends AMeshLoader {

	public static final class GCodeParseException extends ParsingException {

		private static final long serialVersionUID = 3677613639116796904L;

		public GCodeParseException(String msg) {
			super(msg);
		}
	}

	public enum GCodeFlavor {
		UNKNOWN(0),
		SLIC3R(1),
		SKEINFORGE(2);

		private int val;

		GCodeFlavor(int val) {
			this.val = val;
		}

		public final int getVal() {
			return val;
		}

		public static GCodeFlavor fromString(String val) {
			GCodeFlavor ret = null;
			if (val == null) {
				return ret;
			}
			String tVal = val.trim().toLowerCase(Locale.US);
			if ("slic3r".equals(tVal)) {
				return SLIC3R;
			}
			if ("skeinforge".equals(tVal)) {
				return SKEINFORGE;
			}
			return UNKNOWN;
		}

		public String toString() {
			switch (this) {
			case UNKNOWN:
				return "UNKNOWN";
			case SLIC3R:
				return "SLIC3R";
			case SKEINFORGE:
				return "SKEINFORGE";
			default:
				return "";
			}
		}
	}

	public enum SupportedCommands {
		G1(0), G21(1), G90(2), G91(3), G92(4), M82(5), M84(6);

		private int val;

		SupportedCommands(int val) {
			this.val = val;
		}

		public final int getVal() {
			return val;
		}

		public static SupportedCommands fromString(String val) throws IllegalArgumentException {
			if (val == null) {
				return null;
			}
			String tVal = val.trim().toLowerCase(Locale.US);
			if ("g1".equals(tVal)) {
				return G1;
			}
			if ("g21".equals(tVal)) {
				return G21;
			}
			if ("g90".equals(tVal)) {
				return G90;
			}
			if ("g91".equals(tVal)) {
				return G91;
			}
			if ("g92".equals(tVal)) {
				return G92;
			}
			if ("m82".equals(tVal)) {
				return M82;
			}
			if ("m84".equals(tVal)) {
				return M84;
			}
			throw new IllegalArgumentException("unsupported gcode: " + val);

		}

		public String toString() {
			switch (this) {
			case G1:
				return "G1";
			case G21:
				return "G21";
			case G90:
				return "G90";
			case G91:
				return "G91";
			case G92:
				return "G92";
			case M82:
				return "M82";
			case M84:
				return "M84";
			default:
				return "";
			}
		}
	}

	/**
	 * Usage of this class has proven too memory intensive.
	 *
	 * @author fbeachler
	 *
	 */
	public static class GCodeLine {

		static final float DEFAULT_THICKNESS = 1.0f;
		static final int DEFAULT_COLOR = 0x9c9c9c;

		private boolean invalidateLine;
		private float thickness;
		private int color;
		private float origin_x;
		private float origin_y;
		private float origin_z;
		private float origin_e;
		private float origin_f;
		private float x;
		private float y;
		private float z;
		private float e;
		private float f;

		public GCodeLine() {
			thickness = DEFAULT_THICKNESS;
			color = DEFAULT_COLOR;
		}

		/**
		 * Parses a gcode motion line and hydrates x, y, z, e, f values.
		 *
		 * <pre>
		 * 	Example:
		 * 		G1 Z1.0 F3000
		 * 		G1 X99.9948 Y80.0611 Z15.0 F1500.0 E981.64869
		 * 		G1 E104.25841 F1800.0
		 * </pre>
		 *
		 * @param in
		 */
		public GCodeLine(final String in) {
			thickness = DEFAULT_THICKNESS;
			color = DEFAULT_COLOR;
			init();
			if (null != in) {
				String gin = in;
				String[] parts = gin.toLowerCase(Locale.US).split(" ");
				for (int i = 0; i < parts.length; i++) {
					String part = parts[i].trim();
					try {
						String sVal = part.substring(1, part.length() - 1);
						sVal = sVal.replaceAll("[,;\\s]+", "");
						if (sVal.trim().length() == 0) {
							continue;
						}
						float val = Float.parseFloat(sVal);
						if (part.startsWith("x")) {
							x = val;
							continue;
						}
						if (part.startsWith("y")) {
							y = val;
							continue;
						}
						if (part.startsWith("z")) {
							z = val;
							continue;
						}
						if (part.startsWith("e")) {
							e = val;
							continue;
						}
						if (part.startsWith("f")) {
							f = val;
							continue;
						}
					} catch (Exception e) {
						RajLog.e("there was an error parsing gcode=" + gin);
					}
				}
			}
		}

		public void init() {
			this.x = this.y = this.z = this.e = this.f = 0xFFFFFFFF;
		}

		/**
		 * @return the origin_x
		 */
		public float getOrigin_x() {
			return origin_x;
		}

		/**
		 * @param origin_x
		 *            the origin_x to set
		 */
		public void setOrigin_x(float origin_x) {
			this.origin_x = origin_x;
		}

		/**
		 * @return the origin_y
		 */
		public float getOrigin_y() {
			return origin_y;
		}

		/**
		 * @param origin_y
		 *            the origin_y to set
		 */
		public void setOrigin_y(float origin_y) {
			this.origin_y = origin_y;
		}

		/**
		 * @return the origin_z
		 */
		public float getOrigin_z() {
			return origin_z;
		}

		/**
		 * @param origin_z
		 *            the origin_z to set
		 */
		public void setOrigin_z(float origin_z) {
			this.origin_z = origin_z;
		}

		/**
		 * @return the origin_e
		 */
		public float getOrigin_e() {
			return origin_e;
		}

		/**
		 * @param origin_e
		 *            the origin_e to set
		 */
		public void setOrigin_e(float origin_e) {
			this.origin_e = origin_e;
		}

		/**
		 * @return the origin_f
		 */
		public float getOrigin_f() {
			return origin_f;
		}

		/**
		 * @param origin_f
		 *            the origin_f to set
		 */
		public void setOrigin_f(float origin_f) {
			this.origin_f = origin_f;
		}

		/**
		 * @return the x
		 */
		public float getX() {
			return x;
		}

		/**
		 * @param x
		 *            the x to set
		 */
		public void setX(float x) {
			this.x = x;
			invalidateLine = true;
		}

		/**
		 * @return the y
		 */
		public float getY() {
			return y;
		}

		/**
		 * @param y
		 *            the y to set
		 */
		public void setY(float y) {
			this.y = y;
			invalidateLine = true;
		}

		/**
		 * @return the z
		 */
		public float getZ() {
			return z;
		}

		/**
		 * @param z
		 *            the z to set
		 */
		public void setZ(float z) {
			this.z = z;
			invalidateLine = true;
		}

		/**
		 * @return the e
		 */
		public float getE() {
			return e;
		}

		/**
		 * @param e
		 *            the e to set
		 */
		public void setE(float e) {
			this.e = e;
			invalidateLine = true;
		}

		/**
		 * @return the f
		 */
		public float getF() {
			return f;
		}

		/**
		 * @param f
		 *            the f to set
		 */
		public void setF(float f) {
			this.f = f;
			invalidateLine = true;
		}

		/**
		 * @param line
		 */
		public void setOrigin(GCodeLine line) {
			if (null == line) {
				this.origin_x = this.origin_y = this.origin_z = this.origin_e = this.origin_f = 0xFFFFFFFF;
			} else {
				this.origin_x = line.getOrigin_x();
				this.origin_y = line.getOrigin_y();
				this.origin_z = line.getOrigin_z();
				this.origin_e = line.getOrigin_e();
				this.origin_f = line.getOrigin_f();
			}
		}
	}

	public static class GCodeLayer {

		private Stack<Vector3> points;

		public GCodeLayer() {
			points = new Stack<Vector3>();
		}

		/**
		 * @return the points
		 */
		public Stack<Vector3> getPoints() {
			return points;
		}

		/**
		 * @param points
		 *            the points to set
		 */
		public void setPoints(Stack<Vector3> points) {
			this.points = points;
		}
	}

	/**
	 * GCode layers. private Stack<GCodeLayer> layers;
	 */

	/**
	 * Stores meta data extracted from comments
	 */
	private HashMap<String, String> metaData;

	public LoaderGCode(String filename) {
		super(filename);
		init();
	}

	public LoaderGCode(File file) {
		super(file);
		init();
	}

	public LoaderGCode(Renderer renderer, File file) {
		super(renderer, file);
		init();
	}

	public LoaderGCode(Renderer renderer, String fileOnSDCard) {
		super(renderer, fileOnSDCard);
		init();
	}

	public LoaderGCode(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, textureManager, resourceId);
		init();
	}

	public void init() {
		// layers = new Stack<LoaderGCode.GCodeLayer>();
	}

	/**
	 * @return the layers
	 */
	// public Stack<GCodeLayer> getLayers() {
	// return layers;
	// }

	/**
	 * @param layers
	 *            the layers to set
	 */
	// protected void setLayers(Stack<GCodeLayer> layers) {
	// this.layers = layers;
	// }

	/**
	 * @return the metaData
	 */
	public HashMap<String, String> getMetaData() {
		return metaData;
	}

	/**
	 * @param metaData
	 *            the metaData to set
	 */
	public void setMetaData(HashMap<String, String> metaData) {
		this.metaData = metaData;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see rajawali.parser.AMeshLoader#parse()
	 */
	@Override
	public AMeshLoader parse() throws ParsingException {
		super.parse();
		try {
			// Open the file
			BufferedInputStream buffer = null;
			buffer = getBufferedInputStream();
			GCodeFlavor type = tasteFlavor(buffer);
			switch (type) {
			case SLIC3R:
				metaData = readSlic3rComments(buffer);
				mRootObject = readGCode(buffer);
				break;
			case SKEINFORGE:
				metaData = readSkeinforgeComments(buffer);
				mRootObject = readGCode(buffer);
			case UNKNOWN:
			default:
				mRootObject = readGCode(buffer);
				break;
			}
			// Cleanup
			if (buffer != null) {
				buffer.close();
			}
		} catch (FileNotFoundException e) {
			RajLog.e(new StringBuilder().append("[").append(getClass().getCanonicalName())
					.append("] Could not find file.").toString());
			throw new ParsingException("File not found.", e);
		} catch (NumberFormatException e) {
			RajLog.e(Log.getStackTraceString(e));
			throw new ParsingException("Unexpected value.", e);
		} catch (IOException e) {
			RajLog.e(Log.getStackTraceString(e));
			throw new ParsingException("File reading failed.", e);
		} catch (Exception e) {
			RajLog.e(Log.getStackTraceString(e));
			throw new ParsingException("Unexpected exception occured.", e);
		}

		return this;
	}

	/**
	 * Open a BufferedReader for the current resource or file with a buffer size of 8192 bytes.
	 *
	 * @return
	 * @throws FileNotFoundException
	 */
	@Override
	protected BufferedInputStream getBufferedInputStream() throws FileNotFoundException {
		return super.getBufferedInputStream(512);
	}

	/**
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	protected HashMap<String, String> readSlic3rComments(BufferedInputStream buffer) throws IOException {
		buffer.mark(4096);
		HashMap<String, String> ret = new HashMap<String, String>();
		String line;
		StringBuilder lnBuilder = new StringBuilder(1024);
		int i = 0;
		while (buffer.available() > 0 && i < 4096) {
			byte b = (byte) buffer.read();
			i++;
			if (b != '\n') {
				lnBuilder.append((char) b);
				continue;
			} else {
				line = lnBuilder.toString();
				lnBuilder.delete(0, (lnBuilder.length() > 0 ? lnBuilder.length() - 1 : 0));
			}
			if (!line.startsWith(";")) {
				break;
			}
			String[] parts = line.split("=");
			if (parts.length < 2) {
				continue;
			}
			String key = parts[0].trim();
			String value = parts[1].trim();
			ret.put(key, value);
		}
		buffer.reset();
		return ret;
	}

	/**
	 * @param buffer
	 * @return
     */
	protected HashMap<String, String> readSkeinforgeComments(BufferedInputStream buffer) {
		// FIXME no-op
		return new HashMap<String, String>();
	}

	/**
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	protected Object3D readGCode(BufferedInputStream buffer) throws IOException {
		RajLog.i("GCodePaser: reading file");
		Object3D ret = new Object3D();
		String codeLine;
		// Read the facet
		SupportedCommands cmd = null;
		GCodeLine motion = null, prevMotion = null;
		GCodeLayer currentLayer = new GCodeLayer();
		float lastZPos = 0, lastExtrudeZ = 0, x, y;
		@SuppressWarnings("unused")
		float units = 1F; // default for millis
		boolean relative = false;
		prevMotion = new GCodeLine();
		prevMotion.setX(0.0f);
		prevMotion.setY(0.0f);
		prevMotion.setZ(0.0f);
		prevMotion.setE(0.0f);
		prevMotion.setF(0.0f);
		StringBuilder lnBuilder = new StringBuilder(1024);
		int lineNum = 0;
		while (buffer.available() > 0) {
			byte b = (byte) buffer.read();
			if (b != '\n') {
				lnBuilder.append((char) b);
				continue;
			} else {
				codeLine = lnBuilder.toString();
				lnBuilder.delete(0, lnBuilder.length());
			}
			// while ((codeline = buffer.readLine()) != null) {
			if (codeLine.startsWith(";")) {
				continue;
			}
			String[] tokens = codeLine.split(" ");
			if (null == tokens || tokens.length == 0) {
				continue;
			}
			try {
				cmd = SupportedCommands.fromString(tokens[0]);
			} catch (IllegalArgumentException e) {
				RajLog.w("encountered unsupported gcode:" + tokens[0]);
				continue;
			}
			switch (cmd) {
			case G1:
				motion = new GCodeLine(codeLine);
				motion.setOrigin(prevMotion);
				// TODO color lines based on travel and speed
				if (motion.getX() == 0xFFFFFFFF && motion.getY() == 0xFFFFFFFF && motion.getZ() == 0xFFFFFFFF) {
					// extrusion or speed setting
					continue;
				}
				x = motion.getX() == 0xFFFFFFFF ? 0 : motion.getX();
				y = motion.getY() == 0xFFFFFFFF ? 0 : motion.getY();
				if (motion.getZ() != 0xFFFFFFFF) {
					lastZPos = motion.getZ();
					if (x == 0 && y == 0) {
						continue;
					}
				}
				currentLayer.getPoints().add(new Vector3(x, y, lastZPos));
				if (motion.getE() != 0xFFFFFFFF) {
					float delta;
					if (relative) {
						delta = motion.getE() - prevMotion.getE();
					} else {
						delta = motion.getE();
						if (delta == 0xFFFFFFFF) {
							delta = 0;
						}
					}
					if (delta > 0) {
						// current motion is extruding
						// check if extruding at new Z
						if (lastZPos > lastExtrudeZ) { // || layers.empty()
							lastExtrudeZ = lastZPos;
							ret.addChild(new Line3D(currentLayer.getPoints(), 1f, Color.argb(255, 0x55, 0x11, 0xEF)));
							currentLayer = new GCodeLayer();
						}
					}
				}
				prevMotion = motion;
				break;
			case G21:
				// G21: Set Units to Millimeters
				// Example: G21
				units = 1F;
				break;
			case G90:
				// G90: Set to Absolute Positioning
				// Example: G90
				relative = false;
				break;
			case G91:
				// G91: Set to Relative Positioning
				// Example: G91
				relative = true;
				break;
			case G92:
				// G92: Set Position
				// Example: G92 E0
				motion = new GCodeLine(codeLine);
				if (motion.getX() != -0xFFFFFFFF) {
					prevMotion.setX(motion.getX());
				}
				if (motion.getY() != -0xFFFFFFFF) {
					prevMotion.setY(motion.getY());
				}
				if (motion.getZ() != -0xFFFFFFFF) {
					prevMotion.setZ(motion.getZ());
				}
				if (motion.getE() != -0xFFFFFFFF) {
					prevMotion.setE(motion.getE());
				}
				break;
			case M82:
				// no-op, so long as M83 is not supported.
				break;
			case M84:
				// M84: Stop idle hold
				// Example: M84
				// no-op
				break;
			}
			// RajLog.d("gcode parser parsed line #" + lineNum++);
		}
		return ret;
	}

	/**
	 * Determine the content generator (i.e. Slic3r, Skeinforge) for the given file.
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws StlParseException
	 */
	public static final GCodeFlavor tasteFlavor(File file) throws IOException, GCodeParseException {
		if (file.exists())
			throw new GCodeParseException("Passed file does not exist.");

		if (!file.isFile())
			throw new GCodeParseException("This is not a file.");

		final BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(file));
		GCodeFlavor ret = tasteFlavor(buffer);
		buffer.close();

		return ret;
	}

	/**
	 * Determine the content generator (i.e. Slic3r, Skeinforge) for the given resource.
	 *
	 * @param res
	 * @param resId
	 * @return
	 * @throws IOException
	 */
	public static final GCodeFlavor tasteFlavor(Resources res, int resId) throws IOException, NotFoundException {
		BufferedInputStream buffer = new BufferedInputStream(res.openRawResource(resId));
		GCodeFlavor ret = tasteFlavor(buffer);
		buffer.close();

		return ret;
	}

	/**
	 * Determine the content generator (i.e. Slic3r, Skeinforge) for the given BufferedReader.
	 *
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public static final GCodeFlavor tasteFlavor(BufferedInputStream buffer) throws IOException {
		int rl = 200;
		final byte[] readAhead = new byte[rl];
		buffer.mark(readAhead.length);
		int i = 0;
		while (buffer.available() > 0 && i < rl) {
			readAhead[i] = (byte) buffer.read();
			i++;
		}
		buffer.reset();
		final String readAheadString = new String(readAhead).toLowerCase(Locale.US);
		// check for slic3r header
		if (readAheadString.contains("generated by slic3r")) {
			return GCodeFlavor.SLIC3R;
		} else if (readAheadString.contains("skeinforge")) {
			return GCodeFlavor.SKEINFORGE;
		}
		// don't know if it's slic3r generated
		return GCodeFlavor.UNKNOWN;
	}
}
