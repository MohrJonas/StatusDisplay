package com.github.nastygamer.statusdisplay;

public class BusyStatus extends Status {

	@Override
	public String getColor() {
		return "red";
	}

	@Override
	public String getPrefix() {
		return "[busy]";
	}

	@Override
	public String getName() {
		return "busy";
	}
}
