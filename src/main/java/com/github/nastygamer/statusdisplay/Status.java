package com.github.nastygamer.statusdisplay;

public abstract class Status {

	public abstract String getColor();

	public abstract String getPrefix();

	public abstract String getName();

	@Override
	public String toString() {
		return String.format(">> name: %s color: %s prefix: %s <<", getName(), getColor(), getPrefix());
	}
}
