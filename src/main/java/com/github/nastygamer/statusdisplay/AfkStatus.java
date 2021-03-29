package com.github.nastygamer.statusdisplay;

public class AfkStatus extends Status {

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
