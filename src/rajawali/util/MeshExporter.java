package rajawali.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.zip.GZIPOutputStream;

import rajawali.BaseObject3D;
import rajawali.Geometry3D;
import rajawali.SerializedObject3D;
import rajawali.animation.mesh.VertexAnimationFrame;
import rajawali.animation.mesh.VertexAnimationObject3D;
import rajawali.materials.textures.TextureManager;
import rajawali.parser.ObjParser;
import android.content.Context;
import android.os.Environment;

public class MeshExporter {

	private BaseObject3D mObject;
	private String mFileName;
	private boolean mCompressed;
	private File mExportDir = null;

	public enum ExportType {
		AWD,
		SERIALIZED,
		OBJ
	}

	public MeshExporter(BaseObject3D objectToExport) {
		mObject = objectToExport;
	}

	public void setExportDirectory(File exportDir) {
		// The path's validity is the user's responsibility.
		// Any problems are taken care of by the try blocks in the private export methods.
		mExportDir = exportDir;
	}

	public void export(String fileName, ExportType type) {
		export(fileName, type, false);
	}

	public void export(String fileName, ExportType type, boolean compressed) {
		mFileName = fileName;
		mCompressed = compressed;
		switch (type) {
		case AWD:
			exportToAwd();
			break;
		case SERIALIZED:
			exportToSerialized();
			break;
		case OBJ:
			exportToObj();
			break;
		}
	}

	private File getExportFile() {
		File path = mExportDir;
		if (mExportDir == null)
			path = Environment.getExternalStorageDirectory();

		return new File(path, mFileName);
	}

	private void exportToAwd() {
		final File f = getExportFile();

		RajLog.d("Exporting as AWD2 file");
		RajLog.d("Writing to " + f.getAbsolutePath());

		try {
			// Create a buffered LittleEndianOutputStream for writing
			final FileOutputStream fos = new FileOutputStream(f);
			final LittleEndianOutputStream los = new LittleEndianOutputStream(fos);

			// Write the magic string "AWD"
			los.writeByte(65);
			los.writeByte(87);
			los.writeByte(68);

			// Write the document header data
			los.write(2);
			los.write(0);
			los.writeShort(0);
			los.write(0);

			// Write the file size, this should really print the total size post header but it would be difficult to
			// determine the total size before writing.
			los.writeInt(0);

			// Start the Triangle block
			los.writeInt(0);// ID
			los.write(0);// Namespace
			los.write(1);// Type
			los.write(0);// Flags
			los.writeInt(0); // Length

			// Write the name
			los.writeShort(mObject.getName() == null ? 0 : mObject.getName().length());
			if (mObject.getName() != null)
				los.write(mObject.getName().getBytes());

			// Write the sub geometry count
			los.writeShort(1);

			// Write the properties
			los.writeInt(0);
			
			final Geometry3D geom = mObject.getGeometry();
			
			// Write the sub mesh length
			los.writeInt(awdGetGeomLength(geom));
			
			// Write the properties
			los.writeInt(0);

			// Write the geometry
			writeAwdAttributeList(los, 1, 0, geom.getVertices());
			writeAwdAttributeList(los, 2, 0, geom.getIndices());
			writeAwdAttributeList(los, 3, 0, geom.getTextureCoords());
			writeAwdAttributeList(los, 4, 0, geom.getNormals());

			// Close (fos has to be closed separate of the los or writing does not close properly)
			fos.close();
			los.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int awdGetGeomLength(Geometry3D geom) {
		return 24 + (geom.getNumIndices() * 2) + (geom.getNumVertices() * 4) + (geom.getNormals().limit() * 4)
				+ (geom.getTextureCoords().limit() * 4);
	}

	private void writeAwdAttributeList(LittleEndianOutputStream los, int type, int typeF, Buffer data)
			throws Exception {
		// Type
		los.writeByte(type);
		los.writeByte(typeF);

		if (data instanceof IntBuffer) {
			// Length of mesh data
			los.writeInt(data.limit() * 2);
			
			final IntBuffer buf = (IntBuffer) data;
			for (int i = 0, j = data.limit(); i < j; i++)
				los.writeShort(buf.get());
		} else if (data instanceof ShortBuffer) {
			// Length of mesh data
			los.writeInt(data.limit() * 2);
			
			final ShortBuffer buf = (ShortBuffer) data;
			for (int i = 0, j = data.limit(); i < j; i++)
				los.writeShort(buf.get());
		} else if (data instanceof FloatBuffer) {
			// Length of mesh data
			los.writeInt(data.limit() * 4);
			
			final FloatBuffer buf = (FloatBuffer) data;
			for (int i = 0, j = data.limit(); i < j; i++)
				los.writeFloat(buf.get());
		}
	}

	private void exportToObj() {
		RajLog.d("Exporting " + mObject.getName() + " as .obj file");
		Geometry3D g = mObject.getGeometry();
		StringBuffer sb = new StringBuffer();

		sb.append("# Exported by Rajawali 3D Engine for Android\n");
		sb.append("o ");
		sb.append(mObject.getName());
		sb.append("\n");

		for (int i = 0; i < g.getVertices().capacity(); i += 3) {
			sb.append("v ");
			sb.append(g.getVertices().get(i));
			sb.append(" ");
			sb.append(g.getVertices().get(i + 1));
			sb.append(" ");
			sb.append(g.getVertices().get(i + 2));
			sb.append("\n");
		}

		sb.append("\n");

		for (int i = 0; i < g.getTextureCoords().capacity(); i += 2) {
			sb.append("vt ");
			sb.append(g.getTextureCoords().get(i));
			sb.append(" ");
			sb.append(g.getTextureCoords().get(i + 1));
			sb.append("\n");
		}

		sb.append("\n");

		for (int i = 0; i < g.getNormals().capacity(); i += 3) {
			sb.append("vn ");
			sb.append(g.getNormals().get(i));
			sb.append(" ");
			sb.append(g.getNormals().get(i + 1));
			sb.append(" ");
			sb.append(g.getNormals().get(i + 2));
			sb.append("\n");
		}

		sb.append("\n");

		boolean isIntBuffer = g.getIndices() instanceof IntBuffer;

		for (int i = 0; i < g.getIndices().capacity(); i++) {
			if (i % 3 == 0)
				sb.append("\nf ");
			int index = isIntBuffer ? ((IntBuffer) g.getIndices()).get(i) + 1
					: ((ShortBuffer) g.getIndices()).get(i) + 1;
			sb.append(index);
			sb.append("/");
			sb.append(index);
			sb.append("/");
			sb.append(index);
			sb.append(" ");
		}

		try {

			File f = getExportFile();
			FileWriter writer = new FileWriter(f);
			writer.append(sb.toString());
			writer.flush();
			writer.close();

			RajLog.d(".obj export successful: " + f.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Make sure this line is in your AndroidManifer.xml file, under <manifest>: <uses-permission
	 * android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	 */
	private void exportToSerialized() {
		FileOutputStream fos;
		try {
			File f = getExportFile();
			fos = new FileOutputStream(f);
			ObjectOutputStream os = null;
			if (mCompressed) {
				GZIPOutputStream gz = new GZIPOutputStream(fos);
				os = new ObjectOutputStream(gz);
			} else {
				os = new ObjectOutputStream(fos);
			}

			SerializedObject3D ser = mObject.toSerializedObject3D();

			if (mObject instanceof VertexAnimationObject3D) {
				VertexAnimationObject3D o = (VertexAnimationObject3D) mObject;
				int numFrames = o.getNumFrames();
				float[][] vs = new float[numFrames][];
				float[][] ns = new float[numFrames][];
				String[] frameNames = new String[numFrames];

				for (int i = 0; i < numFrames; ++i) {
					VertexAnimationFrame frame = (VertexAnimationFrame) o.getFrame(i);
					Geometry3D geom = frame.getGeometry();
					float[] v = new float[geom.getVertices().limit()];
					geom.getVertices().get(v);
					float[] n = new float[geom.getNormals().limit()];
					geom.getNormals().get(n);
					vs[i] = v;
					ns[i] = n;
					frameNames[i] = frame.getName();
				}

				ser.setFrameVertices(vs);
				ser.setFrameNormals(ns);
				ser.setFrameNames(frameNames);
			}

			os.writeObject(ser);
			os.close();
			RajLog.i("Successfully serialized " + mFileName);
		} catch (Exception e) {
			RajLog.e("Serializing " + mFileName + " was unsuccessfull.");
			e.printStackTrace();
		}

	}

	public static void serializeObj(Context context, TextureManager textureManager, int resourceId, String outputName) {
		serializeObj(context, textureManager, resourceId, outputName, false, null);
	}

	public static void serializeObj(Context context, TextureManager textureManager, int resourceId, String outputName,
			Boolean compress) {
		serializeObj(context, textureManager, resourceId, outputName, compress, null);
	}

	public static void serializeObj(Context context, TextureManager textureManager, int resourceId, String outputName,
			File exportDir) {
		serializeObj(context, textureManager, resourceId, outputName, false, exportDir);
	}

	public static void serializeObj(Context context, TextureManager textureManager, int resourceId, String outputName,
			Boolean compress, File exportDir) {
		final ObjParser objParser = new ObjParser(context.getResources(), textureManager, resourceId);
		try {
			objParser.parse();
			final BaseObject3D obj = objParser.getParsedObject();
			final MeshExporter exporter = new MeshExporter(obj);
			exporter.setExportDirectory(exportDir);
			exporter.export(outputName, ExportType.SERIALIZED, compress);
		} catch (Exception e) {
			RajLog.e("Failed to serialize obj: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
