package rajawali.util.exporter;

import java.io.File;

import rajawali.BaseObject3D;
import rajawali.util.MeshExporter;

public abstract class AExporter {

	protected BaseObject3D mObject;
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
	public void setExportModel(BaseObject3D obj) {
		mObject = obj;
	}
}
