package me.jackyliao.bungeerecorder.converter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileFormatConverter {
	public BungeeRecordReader reader;
	public ReplayModWriter writer;
	public ProtocolTransformer transformer;
//
//	public static byte[] hexToBytes(String hex) {
//		byte[] arr = new byte[hex.length() / 2];
//		for(int i = 0; i < arr.length; ++i) {
//			arr[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
//		}
//		return arr;
//	}

	public FileFormatConverter(InputStream input, OutputStream output) throws IOException {
		reader = new BungeeRecordReader(input);
		writer = new ReplayModWriter(output);

		transformer = new ProtocolTransformer754(reader, writer);
		transformer.init();
	}

	public void convertPackets() throws IOException {
		StoredPacketContainer container;
		while((container = reader.readPacket()) != null) {
			if(container.type == StoredPacketContainer.TYPE_CLIENTBOUND) {
				transformer.clientboundPacket(container);
			} else if(container.type == StoredPacketContainer.TYPE_SERVERBOUND){
				transformer.serverboundPacket(container);
			} else if(container.type == StoredPacketContainer.TYPE_HINT) {
				transformer.hintPacket(container);
			}
			process(container, writer);
		}
	}

	public void finish() throws IOException {
		writer.saveMetadataAndClose(reader.startingTime, reader.protocolVersion, reader.playerName);
		reader.close();
	}

	private void process(StoredPacketContainer packet, ReplayModWriter writer) throws IOException {
	}

	public static void main(String[] args) throws IOException {
		if(args.length < 2) {
			System.out.println("Usage: FileFormatConverter [input] [output]");
			return;
		}
		FileFormatConverter converter = new FileFormatConverter(new FileInputStream(args[0]), new FileOutputStream(args[1]));
		converter.convertPackets();
		converter.finish();
	}
}
