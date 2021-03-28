package com.github.nastygamer.statusdisplay;

import net.minecraft.stat.Stat;
import net.minecraft.text.TextColor;

public class AfkStatus implements Status {

	@Override
	public String getColor() {
		return "gold";
	}

	@Override
	public String getPrefix() {
		return "[afk]";
	}

	@Override
	public String getName() {
		return "afk";
	}
}
