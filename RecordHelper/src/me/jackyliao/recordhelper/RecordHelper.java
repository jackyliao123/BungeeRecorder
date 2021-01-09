package me.jackyliao.recordhelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class RecordHelper extends JavaPlugin {
	public static final String CHANNEL_NAME = "bungeerecorder:hints";

	public HintSender hintSender = new HintSender(this);

//	public HashMap<Player, ItemStack[]> playerInventories = new HashMap<>();
//	public HashSet<Player> playerNeedsUpdating = new HashSet<>();
	public HashMap<Player, ItemStack[]> playerArmor = new HashMap<>();

	public void checkInventories() {
		EquipmentSlot[] equipmentSlots = EquipmentSlot.values();
		for(Player player : playerArmor.keySet()) {
			ItemStack[] newContents = getAllEquipment(player);
			ItemStack[] oldContents = playerArmor.get(player);
			for(int i = 0; i < newContents.length; ++i) {
				if(newContents[i] != null) {
					if(!newContents[i].equals(oldContents[i])) {
						hintSender.sendEntityEquipment(player, equipmentSlots[i], newContents[i]);
					}
				} else {
					if(oldContents[i] != null) {
						hintSender.sendEntityEquipment(player, equipmentSlots[i], null);
					}
				}
			}
			playerArmor.put(player, newContents);
		}
//		playerNeedsUpdating.clear();
	}

	public ItemStack[] cloneInventory(ItemStack[] stacks) {
		ItemStack[] newStack = new ItemStack[stacks.length];
		for(int i = 0; i < stacks.length; ++i) {
			if(stacks[i] != null) {
				newStack[i] = stacks[i].clone();
			}
		}
		return newStack;
	}

	public ItemStack[] getAllEquipment(Player player) {
		PlayerInventory inv = player.getInventory();
		ItemStack[] items = {
				inv.getItemInMainHand(),
				inv.getItemInOffHand(),
				inv.getBoots(),
				inv.getLeggings(),
				inv.getChestplate(),
				inv.getHelmet()
		};

		for(int i = 0; i < items.length; ++i) {
			if(items[i] != null) {
				items[i] = items[i].clone();
			}
		}

		return items;
	}

	public void updatePlayerInventoryCache(Player player) {
		playerArmor.put(player, new ItemStack[EquipmentSlot.values().length]);
//		playerInventories.put(player, cloneInventory(player.getInventory().getContents()));
	}

	public void deletePlayerInventoryCache(Player player) {
		playerArmor.remove(player);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL_NAME);
		getServer().getPluginManager().registerEvents(new RecordEventListener(this), this);
		new BukkitRunnable() {
			@Override
			public void run() {
				checkInventories();
			}
		}.runTaskTimer(this, 0, 1);
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}
}
