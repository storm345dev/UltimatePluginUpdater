package net.stormdev.bukkitmods.ultimatepluginupdater.generalDownload;

import java.util.Arrays;

public class FileComparator {
	public static boolean areSame(byte[] one, byte[] two){
		return Arrays.equals(one, two);
	}
}
