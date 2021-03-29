package com.github.nastygamer.statusdisplay;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.WriterConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

	public ArrayList<Status> getAll() {
		return statuses;
	}

	public ImmutablePair<Boolean, String> validateStatus(Status status) {
		final String name = status.getName();
		final String color = status.getColor();
		final String prefix = status.getPrefix();
		if (!validateName(name)) {
			return new ImmutablePair<>(false, String.format("Invalid Name %s", name));
		}
		if (!validateColor(color)) {
			return new ImmutablePair<>(false, String.format("Invalid Color %s", color));
		}
		if (!validatePrefix(prefix)) {
			return new ImmutablePair<>(false, String.format("Invalid Prefix %s", prefix));
		}
		return new ImmutablePair<>(true, null);
	}

	public void remove(Status status) {
		int index = 0;
		for (int i = 0; i < rootArray.size(); i++) {
			if (rootArray.get(i).asObject().get("name").asString().equals(status.getName())) {
				index = i;
				break;
			}
		}
		rootArray.remove(index);
		try {
			writeJson();
			loadJson();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean validateName(String name) {
		return StringUtils.isNotBlank(name) && statuses.stream().noneMatch(status -> status.getName().equals(name));
	}

	private boolean validateColor(String color) {
		return ColorConverter.colors.contains(color);
	}

	private boolean validatePrefix(String prefix) {
		return StringUtils.isNotBlank(prefix);
	}
}
