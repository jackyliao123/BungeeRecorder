package me.jackyliao.bungeerecorder;

import java.lang.reflect.Field;

public class ReflectionHelper {
	public static Class classInitialHandler;
	public static Field fieldInitialHandler_ch;
	public static Class classChannelWrapper;
	public static Field fieldChannelWrapper_ch;

	public static void initialize() throws ClassNotFoundException {
		try {
			classInitialHandler = Class.forName("net.md_5.bungee.connection.InitialHandler");
			fieldInitialHandler_ch = classInitialHandler.getDeclaredField("ch");
			fieldInitialHandler_ch.setAccessible(true);
			classChannelWrapper = fieldInitialHandler_ch.getType();
			fieldChannelWrapper_ch = classChannelWrapper.getDeclaredField("ch");
			fieldChannelWrapper_ch.setAccessible(true);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
