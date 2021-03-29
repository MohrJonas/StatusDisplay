package com.github.nastygamer.statusdisplay;

public class OnlineStatus extends Status {

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
