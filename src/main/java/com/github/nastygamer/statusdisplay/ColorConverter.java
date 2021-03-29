package com.github.nastygamer.statusdisplay;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class ColorConverter {

	public static final ImmutableList<String> colors = ImmutableList.of(
			"aqua",
			"black",
			"blue",
			"dark_aqua",
			"dark_blue",
			"dark_gray",
			"dark_green",
			"dark_purple",
			"dark_red",
			"gold",
			"gray",
			"green",
			"light_purple",
			"red",
			"white",
			"yellow"
	);

	private static final ImmutableMap<String, String> colorMap = new ImmutableMap.Builder<String, String>()
			.put("black", "§0")
			.put("dark_blue", "§1")
			.put("dark_green", "§2")
			.put("dark_aqua", "§3")
			.put("dark_red", "§4")
			.put("dark_purple", "§5")
			.put("gold", "§6")
			.put("gray", "§7")
			.put("dark_gray", "§8")
			.put("blue", "§9")
			.put("green", "§a")
			.put("aqua", "§b")
			.put("red", "§c")
			.put("light_purple", "§d")
			.put("yellow", "§e")
			.put("white", "§f")
			.build();

	public static String format(String color, String text) {
		return String.format("%s%s§r", colorMap.get(color), text);
	}
}
