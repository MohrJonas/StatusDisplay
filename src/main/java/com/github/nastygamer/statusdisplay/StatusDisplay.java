package com.github.nastygamer.statusdisplay;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import static com.github.nastygamer.statusdisplay.ColorConverter.format;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class StatusDisplay implements ModInitializer {

	private static final String VERSION = "1.0.0";
	private final StatusRegistry registry = new StatusRegistry();

	@Override
	public void onInitialize() {
		System.out.printf("Hello from StatusDisplay V.%s%n", VERSION);
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(
					literal("status")
							.then(argument("name", word())
									.executes(context -> {
										final Status status = registry.getStatus(context.getArgument("name", String.class));
										if (status != null) {
											moveIntoTeam(status, context.getSource().getPlayer(), context);
											sendMessage(format("green", String.format("Status changed to %s.", context.getArgument("name", String.class))), context.getSource().getPlayer(), context);
										} else
											sendMessage(format("red", String.format("Status %s not found.", context.getArgument("name", String.class))), context.getSource().getPlayer(), context);
										return 0;
									}))
			);
			//noinspection SpellCheckingInspection
			dispatcher.register(
					literal("statusnew")
							.then(argument("name", word())
									.then(argument("color", word())
											.then(argument("prefix", word())
													.executes(
															context -> {
																if (!hasPermission(context)) return 1;
																final Status status = registry.getStatus(context.getArgument("name", String.class));
																if (status == null) {
																	final Status newStatus = new Status() {
																		@Override
																		public String getColor() {
																			return context.getArgument("color", String.class);
																		}

																		@Override
																		public String getPrefix() {
																			return context.getArgument("prefix", String.class);
																		}

																		@Override
																		public String getName() {
																			return context.getArgument("name", String.class);
																		}
																	};
																	final ImmutablePair<Boolean, String> validateOutput = registry.validateStatus(newStatus);
																	if (validateOutput.getLeft()) {
																		registry.addStatus(newStatus);
																	} else {
																		sendMessage(format("red", validateOutput.getRight()), context.getSource().getPlayer(), context);
																	}
																} else
																	sendMessage(format("red", String.format("Status %s already defined.", context.getArgument("name", String.class))), context.getSource().getPlayer(), context);
																return 0;
															}
													)))));
			//noinspection SpellCheckingInspection
			dispatcher.register(literal("statuslist").executes(context -> {
				registry.getAll().forEach(status -> {
					try {
						sendMessage(format(status.getColor(), status.toString()), context.getSource().getPlayer(), context);
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
					}
				});
				return 0;
			}));
			//noinspection SpellCheckingInspection
			dispatcher.register(literal("statusremove").then(argument("name", word()).executes(context -> {
				if (!hasPermission(context)) return 1;
				final Status toRemove = registry.getStatus(context.getArgument("name", String.class));
				if (toRemove == null)
					sendMessage(format("red", String.format("No status with name %s found.", context.getArgument("name", String.class))), context.getSource().getPlayer(), context);
				else {
					registry.remove(toRemove);
					sendMessage(format("green", String.format("Successfully removed status %s.", context.getArgument("name", String.class))), context.getSource().getPlayer(), context);
				}
				return 0;
			})));
			//noinspection SpellCheckingInspection
			dispatcher.register(literal("statuschange").then(argument("name", word()).then(argument("type", word()).then(argument("value", word()).executes(
					context -> {
						if (!hasPermission(context)) return 1;
						if (registry.getStatus(context.getArgument("name", String.class)) == null) {
							sendMessage(format("red", String.format("No status with name %s found.", context.getArgument("name", String.class))), context.getSource().getPlayer(), context);
							return 0;
						}
						final String type = context.getArgument("type", String.class);
						if (!(type.equals("color") || type.equals("name") || type.equals("prefix"))) {
							sendMessage(format("red", String.format("Can't change %s. You can change: color, name, prefix", type)), context.getSource().getPlayer(), context);
							return 0;
						}
						if (type.equals("name")) {
							final String name = context.getArgument("value", String.class);
							if (StringUtils.isBlank(name)) {
								sendMessage(format("red", String.format("Name can't be %s.", name)), context.getSource().getPlayer(), context);
							} else {
								registry.modify(registry.getStatus(context.getArgument("name", String.class)), name, null, null);
								sendMessage(format("green", String.format("Changed name to %s.", name)), context.getSource().getPlayer(), context);
							}
						}
						if (type.equals("color")) {
							final String color = context.getArgument("value", String.class);
							if (!ColorConverter.colors.contains(color))
								sendMessage(format("red", String.format("Can't change color to %s.", color)), context.getSource().getPlayer(), context);
							else {
								registry.modify(registry.getStatus(context.getArgument("name", String.class)), null, color, null);
								sendMessage(format("green", String.format("Changed color to %s.", color)), context.getSource().getPlayer(), context);
							}
						}
						if (type.equals("prefix")) {
							final String prefix = context.getArgument("value", String.class);
							if (StringUtils.isBlank(prefix))
								sendMessage(format("red", String.format("Prefix %s can't be blank.", prefix)), context.getSource().getPlayer(), context);
							else {
								registry.modify(registry.getStatus(context.getArgument("name", String.class)), null, null, prefix);
								sendMessage(format("green", String.format("Changed prefix to %s.", prefix)), context.getSource().getPlayer(), context);
							}
						}
						return 0;
					}
			)))));
			dispatcher.register(literal("colors").executes(context -> {
						ColorConverter.colors.forEach(s -> {
							try {
								sendMessage(format(s, s), context.getSource().getPlayer(), context);
							} catch (CommandSyntaxException e) {
								e.printStackTrace();
							}
						});
						return 1;
					}
			));
			dispatcher.register(literal("players").executes(context -> {
						context.getSource().getMinecraftServer().getPlayerManager().getPlayerList().forEach(pe -> {
							final String statusName = pe.getScoreboardTeam() != null ? pe.getScoreboardTeam().getName() : null;
							if (statusName == null) {
								sendMessage(String.format("%s: None", pe.getName().asString()), pe, context);
								return;
							}
							final Status status = registry.getStatus(statusName);
							assert status != null;
							sendMessage(format(status.getColor(), String.format("%s: %s", pe.getName().asString(), status.getName())), pe, context);
						});
						return 1;
					}
			));
		});
	}

	private void sendMessage(String message, ServerPlayerEntity playerEntity, CommandContext<ServerCommandSource> context) {
		context.getSource().getMinecraftServer().getCommandManager().execute(context.getSource().getMinecraftServer().getCommandSource(), String.format("msg %s %s", playerEntity.getName().asString(), message));
	}

	private void moveIntoTeam(Status status, ServerPlayerEntity player, CommandContext<ServerCommandSource> context) {
		final String teamCreateCommand = String.format("team add %s", status.getName());
		final String teamColorCommand = String.format("team modify %s color %s", status.getName(), status.getColor());
		final String teamPrefixCommand = String.format("team modify %s prefix {\"text\": \"%s \"}", status.getName(), status.getPrefix());
		final String teamJoinCommand = String.format("team join %s %s", status.getName(), player.getName().asString());
		context.getSource().getMinecraftServer().getCommandManager().execute(context.getSource().getMinecraftServer().getCommandSource(), teamCreateCommand);
		context.getSource().getMinecraftServer().getCommandManager().execute(context.getSource().getMinecraftServer().getCommandSource(), teamColorCommand);
		context.getSource().getMinecraftServer().getCommandManager().execute(context.getSource().getMinecraftServer().getCommandSource(), teamPrefixCommand);
		context.getSource().getMinecraftServer().getCommandManager().execute(context.getSource().getMinecraftServer().getCommandSource(), teamJoinCommand);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean hasPermission(CommandContext<ServerCommandSource> context) {
		try {
			if (context.getSource().getMinecraftServer().getPlayerManager().isOperator(context.getSource().getPlayer().getGameProfile())) {
				return true;
			} else {
				sendMessage(format("red", "You have to OP to use that command."), context.getSource().getPlayer(), context);
				return false;
			}
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			return false;
		}
	}


}