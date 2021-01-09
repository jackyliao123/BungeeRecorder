package me.jackyliao.bungeerecorder;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;

public class RecordFile {

	public DataOutputStream fileOutput;

	public static DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
	static {
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public BungeeRecorder plugin;
	public long startingTime;
	public File directory;
	public File recordFile;
	public UUID playerUUID;
	public String playerName;
	public int protocolVersion;
	public boolean isLegacy;

	public int savedPackets = 0;
	public int droppedPacketsTotal = 0;
	public int droppedPacketsCtr = 0;

	public volatile boolean idle = true;
	public volatile boolean shouldClose;
	public volatile boolean closed;

	public LinkedBlockingQueue<RecordEntry> entryQueue = new LinkedBlockingQueue<>(65536);

	public RecordFile(BungeeRecorder plugin, File directory, UUID playerUUID, String playerName, int protocolVersion, boolean isLegacy) {
		this.plugin = plugin;
		this.startingTime = System.nanoTime();
		this.directory = directory;
		this.playerUUID = playerUUID;
		this.playerName = playerName;
		this.protocolVersion = protocolVersion;
		this.isLegacy = isLegacy;
	}

	public void openFileAndWriteHeaders() throws IOException {
		recordFile = new File(directory, playerUUID + "_" + format.format(new Date()));
		fileOutput = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(recordFile), 8192));
		fileOutput.writeInt(BungeeRecorder.VERSION);
		fileOutput.writeInt(protocolVersion);
		fileOutput.writeLong(System.currentTimeMillis());
		if(playerUUID != null) {
			fileOutput.writeLong(playerUUID.getMostSignificantBits());
			fileOutput.writeLong(playerUUID.getLeastSignificantBits());
		} else {
			fileOutput.writeLong(0);
			fileOutput.writeLong(0);
		}
		fileOutput.writeUTF(playerName);
	}

	public void appendPacket(RecordEntry entry) throws IOException {
		++savedPackets;
		fileOutput.writeByte(entry.type);
		fileOutput.writeLong(entry.timestamp - startingTime);
		fileOutput.writeInt(entry.length);
		entry.buf.readerIndex(0);
		entry.buf.readBytes(fileOutput, entry.length);
		fileOutput.flush();
	}

	public void processAllPendingOperations() {
		RecordEntry entry;
		while(true) {
			entry = entryQueue.poll();
			if(entry == null) {
				synchronized (this) {
					if(entryQueue.isEmpty()) {
						idle = true;
						break;
					}
				}
				continue;
			}
			try {
				if (entry.operation == RecordEntry.OP_WRITE_HEADERS) {
					openFileAndWriteHeaders();
					plugin.getLogger().info("Started recording to " + recordFile.getName());
				} else if (entry.operation == RecordEntry.OP_APPEND_PACKET) {
					appendPacket(entry);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			if(entry.buf != null) {
				entry.buf.release();
			}
		}
		if(shouldClose) {
			try {
				fileOutput.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			closed = true;
			plugin.getLogger().info("Finished recording to " + recordFile.getName() + ", saved " + savedPackets + " packets (dropped " + droppedPacketsTotal + ")");
		}
	}

	public boolean enqueue(RecordEntry entry) {
		synchronized (this) {
			if(!entryQueue.offer(entry)) {
				++droppedPacketsCtr;
				++droppedPacketsTotal;
				return false;
			}
			if(idle) {
				plugin.submitProcessFile(this);
				idle = false;
			}
		}
		return true;
	}

	public void queueClose() {
		shouldClose = true;
		synchronized (this) {
			if(idle) {
				plugin.submitProcessFile(this);
				idle = false;
			}
		}
	}
}
