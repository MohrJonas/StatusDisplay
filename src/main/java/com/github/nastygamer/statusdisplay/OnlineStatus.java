package com.github.nastygamer.statusdisplay;

public class OnlineStatus implements Status {

	@Override
	public String getColor() {
		return "green";
	}

	@Override
	public String getPrefix() {
		return "[online]";
	}

	@Override
	public String getName() {
		return "online";
	}
}
