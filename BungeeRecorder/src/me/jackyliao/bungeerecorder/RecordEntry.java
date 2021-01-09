package me.jackyliao.bungeerecorder;

import io.netty.buffer.ByteBuf;

public class RecordEntry {
	public static final byte OP_WRITE_HEADERS = 0;
	public static final byte OP_APPEND_PACKET = 1;

	public static final byte TYPE_NONE = -1;
	public static final byte TYPE_CLIENTBOUND = 0;
	public static final byte TYPE_SERVERBOUND = 1;
	public static final byte TYPE_HINT = 2;

	public byte operation;
	public long timestamp;
	public byte type;
	public ByteBuf buf;
	public int length;

	public RecordEntry(byte operation, long timestamp, byte type, ByteBuf buf) {
		this.operation = operation;
		this.timestamp = timestamp;
		this.type = type;
		if(buf != null) {
			this.buf = buf.copy();
			this.length = buf.writerIndex();
		}
	}
}
