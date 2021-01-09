package me.jackyliao.bungeerecorder.converter;

import java.io.*;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public class BungeeRecordReader {

	private DataInputStream input;

	public int version;
	public int protocolVersion;
	public long startingTime;
	public UUID playerUUID;
	public String playerName;

	public BungeeRecordReader(InputStream inputStream) throws IOException {
		input = new DataInputStream(new GZIPInputStream(inputStream));
		version = input.readInt();
		protocolVersion = input.readInt();
		startingTime = input.readLong();
		playerUUID = new UUID(input.readLong(), input.readLong());
		playerName = input.readUTF();
	}

	public StoredPacketContainer readPacket() throws IOException {
		try {
			byte type = input.readByte();
			long timestamp = input.readLong();
			int length = input.readInt();
			byte[] b = new byte[length];
			input.readFully(b);
			return new StoredPacketContainer(timestamp, b, type);
		} catch(EOFException e) {
			return null;
		}
	}

	public void close() throws IOException {
		input.close();
	}
}
