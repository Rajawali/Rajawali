package rajawali;

import java.nio.Buffer;

import rajawali.Geometry3D.BufferType;

public class BufferInfo {
	public int bufferHandle = -1;
	public BufferType bufferType;
	public Buffer buffer;
	public int target;
	public int byteSize;
	public int usage;
	
	public BufferInfo() {
		
	}
	
	public BufferInfo(BufferType bufferType, Buffer buffer) {
		this.bufferType = bufferType;
		this.buffer = buffer;
	}
}