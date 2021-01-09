package me.jackyliao.bungeerecorder.converter;

import java.io.IOException;

public abstract class ProtocolTransformer {
	public int protocolVersion;
	public BungeeRecordReader reader;
	public ReplayModWriter writer;

	public ProtocolTransformer(int protocolVersion, BungeeRecordReader reader, ReplayModWriter writer) {
		this.protocolVersion = protocolVersion;
		this.reader = reader;
		this.writer = writer;
	}

	public void init() throws IOException {
	}

	public void clientboundPacket(StoredPacketContainer container) throws IOException {
	}

	public void serverboundPacket(StoredPacketContainer container) throws IOException {
	}

	public void hintPacket(StoredPacketContainer container) throws IOException {
	}
}
