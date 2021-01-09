package me.jackyliao.bungeerecorder.converter;

import com.google.gson.JsonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ReplayModWriter {
	public ZipOutputStream zipOutputStream;
	public DataOutputStream dataOutput;
	public ZipEntry currentEntry;
	public int lastTimestamp = 0;
	public ReplayModWriter(OutputStream output) throws IOException {
		zipOutputStream = new ZipOutputStream(output);
		dataOutput = new DataOutputStream(zipOutputStream);
		currentEntry = new ZipEntry("recording.tmcpr");
		zipOutputStream.putNextEntry(currentEntry);
	}

	public void writePacket(int timestamp, ByteBuffer buf) throws IOException {
		lastTimestamp = Math.max(timestamp, lastTimestamp);
		dataOutput.writeInt(timestamp);
		dataOutput.writeInt(buf.position());
		dataOutput.write(buf.array(), 0, buf.position());
	}

	public void saveMetadataAndClose(long date, int protocolVersion, String name) throws IOException {
		dataOutput.flush();
		zipOutputStream.closeEntry();
		long crc = currentEntry.getCrc();
		currentEntry = new ZipEntry("recording.tmcpr.crc32");
		zipOutputStream.putNextEntry(currentEntry);
		zipOutputStream.write(Long.toString(crc).getBytes(StandardCharsets.US_ASCII));
		zipOutputStream.closeEntry();
		currentEntry = new ZipEntry("mods.json");
		zipOutputStream.putNextEntry(currentEntry);
		zipOutputStream.write("{\"requiredMods\":[]}".getBytes(StandardCharsets.US_ASCII));
		zipOutputStream.closeEntry();
		currentEntry = new ZipEntry("metaData.json");
		zipOutputStream.putNextEntry(currentEntry);
		zipOutputStream.write(("{\"singleplayer\":false,\"duration\":" + lastTimestamp + ",\"customServerName\":\"" + name + "\",\"date\":" + date + ",\"fileFormat\":\"MCPR\",\"fileFormatVersion\":14,\"protocol\":" + protocolVersion + ",\"generator\":\"BungeeRecorderFileConverter\",\"selfId\":-1,\"players\":[]}").getBytes(StandardCharsets.US_ASCII));
		zipOutputStream.closeEntry();
		zipOutputStream.close();
	}
}
