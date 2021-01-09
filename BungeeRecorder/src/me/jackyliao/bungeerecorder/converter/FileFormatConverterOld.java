//package me.jackyliao.bungeerecorder.converter;
//
//import java.io.*;
//import java.util.zip.Deflater;
//import java.util.zip.GZIPInputStream;
//import java.util.zip.GZIPOutputStream;
//
//public class FileFormatConverterOld {
//	public static byte[] hexToBytes(String hex) {
//		byte[] arr = new byte[hex.length() / 2];
//		for(int i = 0; i < arr.length; ++i) {
//			arr[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
//		}
//		return arr;
//	}
//	public static void main(String[] args) throws IOException {
//		if(args.length < 2) {
//			System.out.println("Usage: FileFormatConverter [clientbound file] [serverbound file]");
//			return;
//		}
//		FileInputStream clientbound = new FileInputStream(args[0]);
//		FileInputStream serverbound = new FileInputStream(args[1]);
//		long start = System.nanoTime();
//		GZIPInputStream input = new GZIPInputStream(clientbound);
//		GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream("test.copy"), 65536);
//		int read;
//		byte[] buf = new byte[65536];
//		int totalBytes = 0;
//		while((read = input.read(buf)) != -1) {
//			totalBytes += read;
//			output.write(buf, 0, read);
//			output.flush();
//		}
//		input.close();
//		output.close();
//		long end = System.nanoTime();
//		System.out.println(totalBytes / ((end - start) / 1e9));
////		FileOutputStream output = new FileOutputStream("/home/jacky/.minecraft/replay_recordings/output.mcpr");
////		ReplayModWriter writer = new ReplayModWriter(output);
////		writer.savePacket(new StoredPacketContainer(0, hexToBytes("02c018ff996ac8371592d48e0a7d7bda200c6a61636b796c69616f313233"), true));
////		combine(clientbound, serverbound, writer);
////		writer.saveMetadata();
//	}
//
//	public static void combine(InputStream clientboundFile, InputStream serverboundFile, ReplayModWriter writer) throws IOException {
//		DataInputStream clientboundInput = new DataInputStream(new GZIPInputStream(clientboundFile));
//		DataInputStream serverboundInput = new DataInputStream(new GZIPInputStream(serverboundFile));
//		StoredPacketContainer clientboundPacket = StoredPacketContainer.readFromInputStream(clientboundInput, true);
//		StoredPacketContainer serverboundPacket = StoredPacketContainer.readFromInputStream(serverboundInput, false);
//		while(clientboundPacket != null || serverboundPacket != null) {
//			boolean processServerbound = false;
//			if(clientboundPacket == null) {
//				process(serverboundPacket, writer);
//				processServerbound = true;
//			} else if(serverboundPacket == null) {
//				process(clientboundPacket, writer);
//			} else if(clientboundPacket.timestamp < serverboundPacket.timestamp) {
//				process(clientboundPacket, writer);
//			} else {
//				process(serverboundPacket, writer);
//				processServerbound = true;
//			}
//
//			if(processServerbound) {
//				serverboundPacket = StoredPacketContainer.readFromInputStream(serverboundInput, false);
//			} else {
//				clientboundPacket = StoredPacketContainer.readFromInputStream(clientboundInput, true);
//			}
//		}
//	}
//
//	public static void process(StoredPacketContainer packet, ReplayModWriter writer) throws IOException {
//		writer.savePacket(packet);
//	}
//
//}
