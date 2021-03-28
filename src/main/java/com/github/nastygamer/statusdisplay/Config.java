package com.github.nastygamer.statusdisplay;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class Config {

	public Config() {
		final File jsonFile = new File(FabricLoader.getInstance().getConfigDirectory(), "status.json");
		if (!jsonFile.exists()) ;
	}

}
