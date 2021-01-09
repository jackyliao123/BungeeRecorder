package me.jackyliao.recordhelper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.Hash;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class HintSender {
	public static Method packetWriteByteBufMethod;
	public static HashMap<Integer, EquipmentSlot> slotToEquipmentSlot = new HashMap<>();

	static {
		packetWriteByteBufMethod = MinecraftMethods.getPacketWriteByteBufMethod();
		slotToEquipmentSlot.put(36, EquipmentSlot.FEET);
		slotToEquipmentSlot.put(37, EquipmentSlot.LEGS);
		slotToEquipmentSlot.put(38, EquipmentSlot.CHEST);
		slotToEquipmentSlot.put(39, EquipmentSlot.HEAD);
		slotToEquipmentSlot.put(40, EquipmentSlot.OFF_HAND);
	}

	public RecordHelper plugin;

	public HintSender(RecordHelper plugin) {
		this.plugin = plugin;
	}

	public void sendPlayerBlockBreak(Player player, org.bukkit.block.Block block) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_EVENT);
		packet.getIntegers().write(0, Effect.STEP_SOUND.getId()).write(1, Block.getCombinedId(((CraftBlock) block).getNMS()));
		Location location = block.getLocation();
		BlockPosition position = new BlockPosition(location.toVector());
		packet.getBlockPositionModifier().write(0, position);
		dispatchHint(player, 1, packet);
	}
//
//	public void sendPlayerSlotChange(Player player, int slot, ItemStack itemStack) {
//		PacketContainer packet = new PacketContainer(PacketType.Play.Server.SET_SLOT);
//		packet.getIntegers().write(0, -2).write(1, slot);
//		packet.getItemModifier().write(0, itemStack);
//		dispatchHint(player, 2, packet);
//
//		EquipmentSlot equipSlot = slotToEquipmentSlot.get(slot);
//		if(equipSlot != null) {
//			sendEntityEquipment(player, equipSlot, itemStack);
//		}
//	}

	public void sendEntityEquipment(Player player, EquipmentSlot slot, ItemStack itemStack) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

		EnumItemSlot enumSlot = null;
		switch(slot) {
			case HAND:
				enumSlot = EnumItemSlot.MAINHAND;
				break;
			case OFF_HAND:
				enumSlot = EnumItemSlot.OFFHAND;
				break;
			case FEET:
				enumSlot = EnumItemSlot.FEET;
				break;
			case LEGS:
				enumSlot = EnumItemSlot.LEGS;
				break;
			case CHEST:
				enumSlot = EnumItemSlot.CHEST;
				break;
			case HEAD:
				enumSlot = EnumItemSlot.HEAD;
				break;
		}

		packet.getIntegers().write(0, player.getEntityId());
		packet.getLists(new EquivalentConverter<Pair>() {
			@Override
			public Object getGeneric(Pair pair) {
				return new Pair(pair.getFirst(), CraftItemStack.asNMSCopy((ItemStack) pair.getSecond()));
			}

			@Override
			public Pair<EquipmentSlot, ItemStack> getSpecific(Object o) {
				Pair pair = (Pair) o;
				return new Pair(pair.getFirst(), CraftItemStack.asBukkitCopy((net.minecraft.server.v1_16_R3.ItemStack) pair.getSecond()));
			}

			@Override
			public Class<Pair> getSpecificType() {
				return Pair.class;
			}
		}).write(0, Arrays.asList(new Pair(enumSlot, itemStack)));
		dispatchHint(player, 3, packet);
	}

	public void dispatchHint(Player player, int id, PacketContainer packet) {
		try {
			ByteBuf buffer = PacketContainer.createPacketBuffer();
			buffer.writeByte(id);
			packetWriteByteBufMethod.invoke(packet.getHandle(), buffer);
			byte[] bytes = new byte[buffer.readableBytes()];
			buffer.readBytes(bytes);
			buffer.release();
			player.sendPluginMessage(plugin, RecordHelper.CHANNEL_NAME, bytes);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
