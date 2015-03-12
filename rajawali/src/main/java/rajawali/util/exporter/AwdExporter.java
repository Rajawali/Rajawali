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
package rajawali.util.exporter;

import java.io.FileOutputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import rajawali.Geometry3D;
import rajawali.util.LittleEndianOutputStream;
import rajawali.util.MeshExporter.ExporterException;
import rajawali.util.RajLog;

public class AwdExporter extends AExporter {

	@Override
	public void export() throws Exception {

		RajLog.d("Exporting as AWD2 file");
		RajLog.d("Writing to " + exportFile.getAbsolutePath());

		try {
			// Create a buffered LittleEndianOutputStream for writing
			final FileOutputStream fos = new FileOutputStream(exportFile);
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
			throw new ExporterException("Failed to write model to AWD format.", e);
		}

	}

	@Override
	public String getExtension() {
		return new String("awd");
	}

	/**
	 * Calculate the length of the TriangleBlock that will be written.
	 * 
	 * @param geom
	 * @return
	 */
	private int awdGetGeomLength(Geometry3D geom) {
		return 24 + (geom.getNumIndices() * 2) + (geom.getNumVertices() * 4) + (geom.getNormals().limit() * 4)
				+ (geom.getTextureCoords().limit() * 4);
	}

	/**
	 * Write a {@link FloatBuffer}, {@link ShortBuffer}, or {@link IntBuffer} as an AWD formatted AttributeList.
	 * 
	 * @param los
	 * @param type
	 * @param typeF
	 * @param data
	 * @throws Exception
	 */
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

}
