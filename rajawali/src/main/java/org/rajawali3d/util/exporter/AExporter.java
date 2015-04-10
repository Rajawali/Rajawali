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
package org.rajawali3d.util.exporter;

import org.rajawali3d.Object3D;
import org.rajawali3d.util.MeshExporter;

import java.io.File;

@Deprecated
public abstract class AExporter {

	protected Object3D mObject;
	protected boolean mCompressed;
	protected File exportFile;
	protected MeshExporter mMeshExporter;

	public abstract void export() throws Exception;

	/**
	 * Extension to be used for the given file type when exporting.
	 * 
	 * @return
	 */
	public String getExtension() {
		return new String("raw");
	}

	/**
	 * Enable compression for export. This is an flag is left up to the exporter to implement. As such, not all
	 * exporters will support this flag.
	 * 
	 * @param flag
	 */
	public void setCompressed(boolean flag) {
		mCompressed = flag;
	}

	/**
	 * Set the {@link MeshExporter} instance to be used by the exporter.
	 * 
	 * @param meshExporter
	 */
	public void setExporter(MeshExporter meshExporter) {
		mMeshExporter = meshExporter;
	}

	/**
	 * The file the exporter should write to.
	 * 
	 * @param file
	 */
	public void setExportFile(File file) {
		exportFile = file;
	}

	/**
	 * Model to be used for exporting.
	 * 
	 * @param obj
	 */
	public void setExportModel(Object3D obj) {
		mObject = obj;
	}
}
