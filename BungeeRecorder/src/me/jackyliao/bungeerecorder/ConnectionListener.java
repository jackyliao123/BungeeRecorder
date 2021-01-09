package me.jackyliao.bungeerecorder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.PacketWrapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ConnectionListener implements Listener {
	public BungeeRecorder plugin;

	public final HashMap<UUID, RecordFile> uuidFileMapping = new HashMap<>();
	public final HashMap<Connection, RecordFile> connectionFileMapping = new HashMap<>();

	public ConnectionListener(BungeeRecorder plugin) {
		this.plugin = plugin;
	}

//	@EventHandler
//	public void onClientConnect(ClientConnectEvent event) {
//	}
//	@EventHandler
//	public void onPlayerHandshake(PlayerHandshakeEvent event) {
//	}
//	@EventHandler
//	public void onPreLogin(PreLoginEvent event) {
//	}
//	@EventHandler
//	public void onLogin(LoginEvent event) {
//	}

	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) {
		if(e.getTag().equals(BungeeRecorder.CHANNEL_NAME)) {
			RecordFile file;
			synchronized(connectionFileMapping) {
				file = connectionFileMapping.get(e.getReceiver());
			}
			if(file != null) {
				file.enqueue(new RecordEntry(RecordEntry.OP_APPEND_PACKET, System.nanoTime(), RecordEntry.TYPE_HINT, Unpooled.wrappedBuffer(e.getData())));
			}
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		System.err.println(player.getPendingConnection());

		RecordFile file = new RecordFile(plugin, plugin.recordDirectory, player.getUniqueId(), player.getName(), player.getPendingConnection().getVersion(), player.getPendingConnection().isLegacy());
		synchronized(uuidFileMapping) {
			uuidFileMapping.put(player.getUniqueId(), file);
		}
		synchronized (connectionFileMapping) {
			connectionFileMapping.put(player, file);
		}

		if(!file.enqueue(new RecordEntry(RecordEntry.OP_WRITE_HEADERS, System.nanoTime(), RecordEntry.TYPE_NONE, null))) {
			plugin.getLogger().warning("Failed to enqueue file creation for " + player.getUniqueId() + ". They will not be recorded");
			return;
		}

		try {
			ChannelWrapper wrapper = (ChannelWrapper) ReflectionHelper.fieldInitialHandler_ch.get(event.getPlayer().getPendingConnection());
			Channel channel = (Channel) ReflectionHelper.fieldChannelWrapper_ch.get(wrapper);
			channel.pipeline().addAfter(PipelineUtils.PACKET_DECODER, "bungeeRecorderServerbound", new ChannelDuplexHandler() {
				@Override
				public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
					PacketWrapper packet = (PacketWrapper) msg;

					ByteBuf buf = packet.buf;

					file.enqueue(new RecordEntry(RecordEntry.OP_APPEND_PACKET, System.nanoTime(), RecordEntry.TYPE_SERVERBOUND, buf));

					super.channelRead(ctx, msg);
				}
			});
			channel.pipeline().addBefore(PipelineUtils.PACKET_ENCODER, "bungeeRecorderClientbound", new ChannelDuplexHandler() {
				@Override
				public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
					ByteBuf buf = (ByteBuf) msg;

					file.enqueue(new RecordEntry(RecordEntry.OP_APPEND_PACKET, System.nanoTime(), RecordEntry.TYPE_CLIENTBOUND, buf));

					super.write(ctx, msg, promise);
				}
			});
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		try {
			ProxiedPlayer player = event.getPlayer();
			RecordFile file;
			synchronized(uuidFileMapping) {
				file = uuidFileMapping.remove(player.getUniqueId());
			}
			synchronized(connectionFileMapping) {
				connectionFileMapping.remove(player.getPendingConnection());
			}
			file.queueClose();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void cleanup() {
		synchronized(uuidFileMapping) {
			Iterator<Map.Entry<UUID, RecordFile>> it = uuidFileMapping.entrySet().iterator();
			while(it.hasNext()) {
				RecordFile file = it.next().getValue();
				file.queueClose();
				it.remove();
			}
		}
		synchronized (connectionFileMapping) {
			connectionFileMapping.clear();
		}
	}

}
