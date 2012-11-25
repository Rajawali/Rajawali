package rajawali;

import java.nio.Buffer;

import android.opengl.GLES20;

import rajawali.Geometry3D.BufferType;

public class BufferInfo {
	public int bufferHandle = -1;
	public BufferType bufferType;
	public Buffer buffer;
	public int target;
	public int byteSize;
	public int usage;
	
	public BufferInfo() {
		this.usage = GLES20.GL_STATIC_DRAW;
	}
	
	public BufferInfo(BufferType bufferType, Buffer buffer) {
		this.bufferType = bufferType;
		this.buffer = buffer;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb
			.append("Handle: ").append(bufferHandle)
			.append(" type: ").append(bufferType)
			.append(" target: ").append(target)
			.append(" byteSize: ").append(byteSize)
			.append(" usage: ").append(usage);
		return sb.toString();
	}
}