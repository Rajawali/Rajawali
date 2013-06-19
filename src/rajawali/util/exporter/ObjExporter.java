package rajawali.util.exporter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import rajawali.Geometry3D;
import rajawali.util.RajLog;

public class ObjExporter extends AExporter {

	@Override
	public void export() throws Exception {

		RajLog.d("Exporting " + mObject.getName() + " as .obj file");
		Geometry3D g = mObject.getGeometry();
		StringBuffer sb = new StringBuffer(9000);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(exportFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

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
			bufferStringWriting(sb, bw);
		}

		sb.append("\n");

		for (int i = 0; i < g.getTextureCoords().capacity(); i += 2) {
			sb.append("vt ");
			sb.append(g.getTextureCoords().get(i));
			sb.append(" ");
			sb.append(g.getTextureCoords().get(i + 1));
			sb.append("\n");
			bufferStringWriting(sb, bw);
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
			bufferStringWriting(sb, bw);
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
			bufferStringWriting(sb, bw);
		}

		try {
			// Write any remaining data to the file
			bw.append(sb.toString());
			bw.flush();
			bw.close();

			RajLog.d(".obj export successful: " + exportFile.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getExtension() {
		return new String("obj");
	}

	/**
	 * Helper method for writing chunks of data to the given writer. If data is written the passed StringBuffer will be
	 * emptied.
	 * 
	 * @param stringBuilder
	 * @param writer
	 */
	private void bufferStringWriting(StringBuffer stringBuilder, Writer writer) {
		if (stringBuilder.length() >= 8192) {
			try {
				writer.write(stringBuilder.toString());
				stringBuilder.delete(0, stringBuilder.length());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
