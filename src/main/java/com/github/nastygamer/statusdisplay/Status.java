package com.github.nastygamer.statusdisplay;

public abstract class Status {

	public abstract String getColor();

	public abstract String getPrefix();

	public abstract String getName();

	@Override
	public String toString() {
		return String.format("%s  |  %s  |  %s  |", getName(), getColor(), getPrefix());
	}
}
