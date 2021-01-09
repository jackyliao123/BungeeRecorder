package me.jackyliao.bungeerecorder;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.concurrent.*;

public class BungeeRecorder extends Plugin {
	public volatile boolean running = false;
	public static final int VERSION = 0;
	public static final String CHANNEL_NAME = "bungeerecorder:hints";
	public File recordDirectory = new File("playerRecordings");
	public ConnectionListener listener;

	ThreadPoolExecutor executor;

	public Thread monitorThread = new Thread("Record Queue Monitor Thread") {
		public void run() {
			while(running) {
				synchronized(listener.uuidFileMapping) {
					for(RecordFile file : listener.uuidFileMapping.values()) {
						if(file.droppedPacketsCtr > 0) {
							getLogger().warning("Can't keep up, dropped " + file.droppedPacketsCtr + " packets for " + file.playerUUID);
							file.droppedPacketsCtr = 0;
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e) {
				}
			}
		}
	};

	public void submitProcessFile(RecordFile file) {
		executor.submit(file::processAllPendingOperations);
	}

	public BungeeRecorder() {
		try {
			ReflectionHelper.initialize();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onEnable() {
		getLogger().info("BungeeRecorder onEnable");
		if(!recordDirectory.exists()) {
			recordDirectory.mkdirs();
		}
		listener = new ConnectionListener(this);
		getProxy().registerChannel(CHANNEL_NAME);
		getProxy().getPluginManager().registerListener(this, listener);
		running = true;
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		System.out.println("Thread pool started: core = " + executor.getCorePoolSize() + " max=" + executor.getMaximumPoolSize() + " cur=" + executor.getPoolSize());
		monitorThread.start();
	}

	@Override
	public void onDisable() {
		getLogger().info("BungeeRecorder onDisable");
		listener.cleanup();
		running = false;
		executor.shutdown();
		monitorThread.interrupt();
		try {
			executor.awaitTermination(60, TimeUnit.SECONDS);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
