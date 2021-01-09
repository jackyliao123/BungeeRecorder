package me.jackyliao.bungeerecorder.converter;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class ProtocolTransformer754 extends ProtocolTransformer {

	public int playerEntityId;
	public double playerX, playerY, playerZ, playerYaw, playerPitch;
	public double prevX, prevY, prevZ, prevYaw, prevPitch;
	public boolean playerOnGround;

	public ProtocolTransformer754(BungeeRecordReader reader, ReplayModWriter writer) {
		super(754, reader, writer);
	}

	public void init() throws IOException {
		ByteBuffer loginPacket = ByteBuffer.allocate(128).order(ByteOrder.BIG_ENDIAN);
		loginPacket.put((byte) 0x02);
		loginPacket.putLong(reader.playerUUID.getMostSignificantBits());
		loginPacket.putLong(reader.playerUUID.getLeastSignificantBits());
		loginPacket.put((byte) reader.playerName.length());
		loginPacket.put(reader.playerName.getBytes(StandardCharsets.UTF_8));

		writer.writePacket(0, loginPacket);
	}

	public void clientboundPacket(StoredPacketContainer packet) throws IOException {
//		System.out.println("Clientbound: " + Integer.toHexString(packet.type));
		ByteBuffer buf = ByteBuffer.wrap(packet.buf);
		int timestampMs = (int) (packet.timestamp / 1000000);
		byte type = buf.get();
//		if(type == 0x07) {
//			return;
//		}

		buf.position(buf.capacity());
		writer.writePacket(timestampMs, buf);
		buf.position(1);

		switch(type) {
			case 0x24: // JOIN GAME
				playerEntityId = buf.getInt();
				System.out.println("JOIN as id = " + playerEntityId);
				break;
			case 0x34: // PLAYER TELEPORT
				processTeleport(buf, timestampMs);
				break;
			case 0x32: // PLAYER INFO
				if(ByteUtils.readVarInt(buf) == 0) { // ACTION == ADD_PLAYER
					int numPlayers = ByteUtils.readVarInt(buf);
					for(int i = 0; i < numPlayers; ++i) {
						UUID uuid = ByteUtils.readUUID(buf);
						if(uuid.equals(reader.playerUUID)) {
							System.out.println("PLAYER SPAWN FOUND");
							writeSpawnPlayerPacket(timestampMs);
						}
						String name = ByteUtils.readString(buf);
						int props = ByteUtils.readVarInt(buf);
						for(int j = 0; j < props; ++j) {
							String propName = ByteUtils.readString(buf);
							String propValue = ByteUtils.readString(buf);
							boolean isSigned = buf.get() == 1;
							if(isSigned) {
								String signature = ByteUtils.readString(buf);
							}
						}
						int gamemode = ByteUtils.readVarInt(buf);
						int ping = ByteUtils.readVarInt(buf);
						boolean hasDisplayName = buf.get() == 1;
						if(hasDisplayName) {
							String displayName = ByteUtils.readString(buf);
						}
					}
				}
				break;
			case 0x39: // RESPAWN
				writeSpawnPlayerPacket(timestampMs);
				break;
			case 0x0B: // BLOCK CHANGE
				System.out.println(timestampMs + " BLOCK CHANGE");
				break;
			case 0x3B: // MULTI BLOCK CHANGE
				System.out.println(timestampMs + " MULTI BLOCK CHANGE");
				break;
//			case 0x07: // ACKNOWLEDGE PLAYER DIGGING
//				{
//					long position = buf.getLong();
//					int block = ByteUtils.readVarInt(buf);
//					int status = ByteUtils.readVarInt(buf);
//					boolean success = buf.get() == 1;
//					if(success && status == 2) {
////						writeBlockBreakAnimation(timestampMs, position, 0);
//						writeEffect(timestampMs, 2001, position, block, false);
//					}
//				}
//				break;
			case 0x21:
				System.out.println(timestampMs + " EFFECT");
				System.out.println(Arrays.toString(buf.array()));
				break;
			case 0x15:
				System.out.println(timestampMs + " SET SLOT");
				System.out.println(Arrays.toString(buf.array()));
				break;
			case 0x13:
				System.out.println(timestampMs + " WINDOW ITEMS");
				System.out.println(Arrays.toString(buf.array()));
				break;
		}
	}

	public void serverboundPacket(StoredPacketContainer packet) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(packet.buf);
		int timestampMs = (int) (packet.timestamp / 1000000);
		byte type = buf.get();
		switch(type) {
			case 0x12: // POSITION
				processMovement(buf, true, false, timestampMs);
				break;
			case 0x13: // POSITION AND ROTATION
				processMovement(buf, true, true, timestampMs);
				break;
			case 0x14: // ROTATION
				processMovement(buf, false, true, timestampMs);
				break;
			case 0x15: // ON_GROUND
				processMovement(buf, false, false, timestampMs);
				break;
			case 0x2C: // ANIMATION
				writeAnimation(timestampMs, ByteUtils.readVarInt(buf) == 0 ? 0 : 3);
				break;
//			case 0x1B: // PLAYER DIGGING
//				{
//					int status = ByteUtils.readVarInt(buf);
//					long position = buf.getLong();
//					byte face = buf.get();
//					if(status == 2) {
////						writeBlockBreakAnimation(timestampMs, position, 0);
//						writeEffect(timestampMs, 2001, position, 5, false);
//					}
//				}
//				break;
		}
//		System.out.println("Serverbound: " + packet.type);
	}

	public void hintPacket(StoredPacketContainer packet) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(packet.buf);
		int timestampMs = (int) (packet.timestamp / 1000000);
		byte type = buf.get();
		switch(type) {
			case 1: // EFFECT
				buf.put(0, (byte) 0x21);
				System.out.println("HINT EFFECT===");
				break;
			case 2: // SET SLOT
				buf.put(0, (byte) 0x15);
				System.out.println("HINT SET SLOT===");
				break;
			case 3: // ENTITY EQUIPMENT
				buf.put(0, (byte) 0x47);
				System.out.println("HINT ENTITY EQUIPMENT====");
				break;
		}
		buf.position(buf.capacity());
		writer.writePacket(timestampMs, buf);
		System.out.println(Arrays.toString(packet.buf));
	}

	private void writeEffect(int timestampMs, int effectId, long position, int data, boolean disableRelativeVolume) throws IOException {
		byte[] packet = new byte[18];
		ByteBuffer buf = ByteBuffer.wrap(packet);
		buf.put((byte) 0x21); // EFFECT
		buf.putInt(effectId);
		buf.putLong(position - 1);
		buf.putInt(data);
		buf.put((byte) (disableRelativeVolume ? 1 : 0));
		writer.writePacket(timestampMs, buf);
	}

	private void writeBlockBreakAnimation(int timestampMs, long position, int stage) throws IOException {
		byte[] packet = new byte[14];
		ByteBuffer buf = ByteBuffer.wrap(packet);
		buf.put((byte) 0x08); // BLOCK BREAK ANIMATION
		ByteUtils.writeVarInt(buf, 0);
		buf.putLong(position);
		buf.put((byte) stage);
		writer.writePacket(timestampMs, buf);
	}

	private void writeAnimation(int timestampMs, int animation) throws IOException {
		byte[] packet = new byte[6];
		ByteBuffer buf = ByteBuffer.wrap(packet);
		buf.put((byte) 0x05); // ENTITY ANIMATION
		ByteUtils.writeVarInt(buf, playerEntityId);
		buf.put((byte) animation);
		writer.writePacket(timestampMs, buf);
	}

	private void writeSpawnPlayerPacket(int timestampMs) throws IOException {
		byte[] packet = new byte[48];
		ByteBuffer buf = ByteBuffer.wrap(packet);
		buf.put((byte) 0x04); // SPAWN PLAYER
		ByteUtils.writeVarInt(buf, playerEntityId);
		ByteUtils.writeUUID(buf, reader.playerUUID);
		buf.putDouble(playerX).putDouble(playerY).putDouble(playerZ);
		buf.put((byte) Math.floor(playerYaw / 360.0 * 256.0));
		buf.put((byte) Math.floor(playerPitch / 360.0 * 256.0));
		writer.writePacket(timestampMs, buf);
	}

	private void writePlayerMovement(int timestampMs, boolean forceTeleport) throws IOException {
		byte[] packet = new byte[32];
		ByteBuffer buf = ByteBuffer.wrap(packet);

//		if(forceTeleport) {
			buf.put((byte) 0x56); // ENTITY TELEPORT
			ByteUtils.writeVarInt(buf, playerEntityId);
			buf.putDouble(playerX).putDouble(playerY).putDouble(playerZ);
			buf.put((byte) Math.floor(playerYaw / 360.0 * 256.0));
			buf.put((byte) Math.floor(playerPitch / 360.0 * 256.0));
			buf.put((byte) (playerOnGround ? 1 : 0));
			writer.writePacket(timestampMs, buf);

			buf.position(0);

			buf.put((byte) 0x3a); // ENTITY HEAD LOOK
			ByteUtils.writeVarInt(buf, playerEntityId);
			buf.put((byte) Math.floor(playerYaw / 360.0 * 256.0));
			writer.writePacket(timestampMs, buf);
//		}

		prevX = playerX;
		prevY = playerY;
		prevZ = playerZ;
		prevYaw = playerYaw;
		prevPitch = playerPitch;
	}

	private void processMovement(ByteBuffer buf, boolean hasPosition, boolean hasRotation, int timestampMs) throws IOException {
		if(hasPosition) {
			playerX = buf.getDouble();
			playerY = buf.getDouble();
			playerZ = buf.getDouble();
		}
		if(hasRotation) {
			playerYaw = buf.getFloat();
			playerPitch = buf.getFloat();
		}
		playerOnGround = buf.get() == 1;
		writePlayerMovement(timestampMs, false);
	}

	private void processTeleport(ByteBuffer buf, int timestampMs) throws IOException {
		double x = buf.getDouble();
		double y = buf.getDouble();
		double z = buf.getDouble();
		float yaw = buf.getFloat();
		float pitch = buf.getFloat();
		byte flags = buf.get();
		if((flags & 0x1) != 0) {
			x += playerX;
		}
		if((flags & 0x2) != 0) {
			y += playerY;
		}
		if((flags & 0x4) != 0) {
			z += playerZ;
		}
		if((flags & 0x8) != 0) {
			yaw += playerYaw;
		}
		if((flags & 0x10) != 0) {
			pitch += playerPitch;
		}
		playerX = x;
		playerY = y;
		playerZ = z;
		playerYaw = yaw;
		playerPitch = pitch;
		writePlayerMovement(timestampMs, true);
//		System.out.println("TELEPORT TO (" + playerX + ", " + playerY + ", " + playerZ + "), (" + playerYaw + ", " + playerPitch + ")");
	}
}
