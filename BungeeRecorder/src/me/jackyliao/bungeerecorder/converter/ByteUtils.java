package me.jackyliao.bungeerecorder.converter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ByteUtils {
	public static int readVarInt(ByteBuffer buf) {
		int result = 0;
		int shift = 0;
		byte read;
		do {
			read = buf.get();
			result |= (read & 0x7f) << shift;
			shift += 7;

			if (shift > 32) {
				throw new RuntimeException("VarInt is too big");
			}
		} while ((read & 0x80) != 0);

		return result;
	}

	public static void writeVarInt(ByteBuffer buf, int val) {
		do {
			byte temp = (byte)(val & 0x7f);
			val >>>= 7;
			if (val != 0) {
				temp |= 0x80;
			}
			buf.put(temp);
		} while (val != 0);
	}

	public static UUID readUUID(ByteBuffer buf) {
		return new UUID(buf.getLong(), buf.getLong());
	}

	public static void writeUUID(ByteBuffer buf, UUID uuid) {
		buf.putLong(uuid.getMostSignificantBits());
		buf.putLong(uuid.getLeastSignificantBits());
	}

	public static String readString(ByteBuffer buf) {
		int len = readVarInt(buf);
		byte[] b = new byte[len];
		buf.get(b);
		return new String(b, StandardCharsets.UTF_8);
	}

	public static void skipNBT(ByteBuffer buf) {
		byte tag = buf.get();
		int size;
		switch(tag) {
			case 0: // TAG_END
				return;
			case 1: // TAG_BYTE
				buf.get();
				break;
			case 2: // TAG_SHORT
				buf.getShort();
				break;
			case 3: // TAG_INT
				buf.getInt();
				break;
			case 4: // TAG_LONG
				buf.getLong();
				break;
			case 5: // TAG_FLOAT
				buf.getFloat();
				break;
			case 6: // TAG_DOUBLE
				buf.getDouble();
				break;
			case 7: // TAG_BYTE_ARRAY
				size = buf.getInt();
				buf.position(buf.position() + size);
				break;
			case 8: // TAG_STRING
				size = buf.getShort();
				buf.position(buf.position() + size);
				break;
			case 11: // TAG_INT_ARRAY
				size = buf.getInt();
				buf.position(buf.position() + size * 4);
				break;
			case 12: // TAG_LONG_ARRAY
				size = buf.getInt();
				buf.position(buf.position() + size * 8);
				break;
			case 9: // TAG_LIST

				break;
			case 10: // TAG_COMPOUND

				break;
		}
	}
}
