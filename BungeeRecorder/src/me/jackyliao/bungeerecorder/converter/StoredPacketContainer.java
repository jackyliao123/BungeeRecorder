package me.jackyliao.bungeerecorder.converter;

import java.io.DataInputStream;
import java.io.EOFException;

public class StoredPacketContainer {
	public static final byte TYPE_CLIENTBOUND = 0;
	public static final byte TYPE_SERVERBOUND = 1;
	public static final byte TYPE_HINT = 2;

	public long timestamp;
//	public int type;
	public byte[] buf;
	public byte type;
	public StoredPacketContainer(long timestamp, byte[] buf, byte type) {
		this.timestamp = timestamp;
		this.buf = buf;
		this.type = type;
//		type = readVarInt(buf, 0);
	}

//	public static StoredPacketContainer readFromInputStream(DataInputStream input, boolean isClientbound) {
//		try {
//			int timestamp = input.readInt();
//			int length = input.readInt();
//			byte[] buf = new byte[length];
//			input.readFully(buf);
//			return new StoredPacketContainer(timestamp * 10, buf, isClientbound);
//		} catch(EOFException e) {
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

//	public static int readInt(byte[] buf, int idx) {
//		return (buf[idx] << 24) | ((buf[idx + 1] & 0xFF) << 16) | ((buf[idx + 2] & 0xFF) << 8) | (buf[idx + 3] & 0xFF);
//	}

//	public static int readVarInt(byte[] buf, int idx) {
//		int result = 0;
//		int shift = 0;
//		byte read;
//		do {
//			read = buf[idx++];
//			result |= (read & 0x7f) << shift;
//			shift += 7;
//
//			if (shift > 32) {
//				throw new RuntimeException("VarInt is too big");
//			}
//		} while ((read & 0x80) != 0);
//
//		return result;
//	}
}
