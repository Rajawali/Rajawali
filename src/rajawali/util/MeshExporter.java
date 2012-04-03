package rajawali.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

import rajawali.BaseObject3D;
import rajawali.Geometry3D;

public class MeshExporter {
	private BaseObject3D mObject;
	private String mFileName;
	
	public enum ExportType {
		SERIALIZED,
		OBJ
	}
	
	public MeshExporter(BaseObject3D objectToExport) {
		mObject = objectToExport;
	}
	
	public void export(String fileName, ExportType type) {
		mFileName = fileName;
		switch(type) {
		case SERIALIZED:
			exportToSerialized();
			break;
		case OBJ:
			exportToObj();
			break;
		}
	}
	
	private void exportToObj() {
		Log.d("Rajawali", "Exporting " +mObject.getName()+ " as .obj file");
		Geometry3D g = mObject.getGeometry();
		StringBuffer sb = new StringBuffer();
		
		sb.append("# Exported by Rajawali 3D Engine for Android\n");
		sb.append("o ");
		sb.append(mObject.getName());
		sb.append("\n");
		
		for(int i=0; i<g.getVertices().capacity(); i+=3) {
			sb.append("v ");
			sb.append(g.getVertices().get(i));
			sb.append(" ");
			sb.append(g.getVertices().get(i+1));
			sb.append(" ");
			sb.append(g.getVertices().get(i+2));
			sb.append("\n");
		}
		
		sb.append("\n");
		
		for(int i=0; i<g.getTextureCoords().capacity(); i+=2) {
			sb.append("vt ");
			sb.append(g.getTextureCoords().get(i));
			sb.append(" ");
			sb.append(g.getTextureCoords().get(i+1));
			sb.append("\n");
		}
		
		sb.append("\n");
		
		for(int i=0; i<g.getNormals().capacity(); i+=3) {
			sb.append("vn ");
			sb.append(g.getNormals().get(i));
			sb.append(" ");
			sb.append(g.getNormals().get(i+1));
			sb.append(" ");
			sb.append(g.getNormals().get(i+2));
			sb.append("\n");
		}
		
		sb.append("\n");
		
		for(int i=0; i<g.getIndices().capacity(); i++) {
			if(i%3 == 0)
				sb.append("\nf ");
			int index = g.getIndices().get(i) + 1;
			sb.append(index);
			sb.append("/");
			sb.append(index);
			sb.append("/");
			sb.append(index);
			sb.append(" ");
		}
		
		try
	    {
			File sdcardStorage = Environment.getExternalStorageDirectory();
			String sdcardPath = sdcardStorage.getParent()
					+ java.io.File.separator + sdcardStorage.getName();

			File f = new File(sdcardPath + File.separator + mFileName);
	        FileWriter writer = new FileWriter(f);
	        writer.append(sb.toString());
	        writer.flush();
	        writer.close();
	        
	        Log.d("Rajawali", ".obj export successful: " + sdcardPath + File.separator + mFileName);
	    }
	    catch(IOException e)
	    {
	         e.printStackTrace();
	    }
	}
	
	private void exportToSerialized() {
		
	}
}
