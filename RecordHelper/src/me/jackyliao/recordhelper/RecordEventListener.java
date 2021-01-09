package me.jackyliao.recordhelper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.wrappers.BlockPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.v1_16_R3.Block;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Method;

public class RecordEventListener implements Listener {
	public RecordHelper plugin;
	public RecordEventListener(RecordHelper plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.updatePlayerInventoryCache(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.deletePlayerInventoryCache(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event) {
		plugin.hintSender.sendPlayerBlockBreak(event.getPlayer(), event.getBlock());
	}

//	@EventHandler(priority = EventPriority.MONITOR)
//	public void onInventoryClick(InventoryClickEvent event) {
//		HumanEntity whoClicked = event.getWhoClicked();
//		if(whoClicked instanceof Player) {
//			plugin.playerNeedsUpdating.add((Player) whoClicked);
//		}
//	}
//
//	@EventHandler(priority = EventPriority.MONITOR)
//	public void onInventoryDrag(InventoryDragEvent event) {
//		HumanEntity whoClicked = event.getWhoClicked();
//		if(whoClicked instanceof Player) {
//			plugin.playerNeedsUpdating.add((Player) whoClicked);
//		}
//	}
}
