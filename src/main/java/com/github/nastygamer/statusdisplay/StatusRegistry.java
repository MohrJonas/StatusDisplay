package com.github.nastygamer.statusdisplay;

import com.eclipsesource.json.*;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

public class StatusRegistry {

	private final ArrayList<Status> statuses = new ArrayList<>();
	@SuppressWarnings("deprecation")
	private final File jsonFile = new File(FabricLoader.getInstance().getConfigDirectory(), "status.json");
	private JsonArray rootArray;

	public StatusRegistry() {
		try {
			if (!jsonFile.exists()) {
				//noinspection ResultOfMethodCallIgnored
				jsonFile.createNewFile();
				generateDefault();
				writeJson();
			}
			loadJson();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public void writeJson() throws IOException {
		final BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
		writer.write(rootArray.toString(WriterConfig.PRETTY_PRINT));
		writer.close();
	}

	private void loadJson() throws IOException {
		rootArray = Json.parse(new BufferedReader(new FileReader(jsonFile))).asArray();
		statuses.clear();
		rootArray.forEach(jsonValue -> {
			final JsonObject o = jsonValue.asObject();
			statuses.add(new Status() {
				@Override
				public String getColor() {
					return o.get("color").asString();
				}

				@Override
				public String getPrefix() {
					return o.get("prefix").asString();
				}

				@Override
				public String getName() {
					return o.get("name").asString();
				}
			});
		});
	}

	private void generateDefault() {
		rootArray = new JsonArray();
		addStatus(new AfkStatus());
		addStatus(new OnlineStatus());
		addStatus(new BusyStatus());
	}

	public void addStatus(Status status) {
		if (!isValid(status)) return;
		rootArray.add(new JsonObject() {{
			add("name", status.getName());
			add("color", status.getColor());
			add("prefix", status.getPrefix());
		}});
		try {
			writeJson();
			loadJson();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public @Nullable Status getStatus(String name) {
		return statuses.stream().filter(status -> status.getName().equals(name)).findFirst().orElse(null);
	}

	private boolean isValid(Status status) {
		for (JsonValue value : rootArray) {
			if (value.asObject().get("name").asString().equals(status.getName())) return false;
		}
		return true;
	}

	public ArrayList<Status> getAll() {
		return statuses;
	}
}
