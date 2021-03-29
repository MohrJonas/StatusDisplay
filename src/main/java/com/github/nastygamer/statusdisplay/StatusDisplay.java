package com.github.nastygamer.statusdisplay;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

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
										}
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
																final Status status = registry.getStatus(context.getArgument("name", String.class));
																if (status == null) {
																	registry.addStatus(new Status() {
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
																	});
																}
																return 0;
															}
													)))));
			//noinspection SpellCheckingInspection
			dispatcher.register(literal("statuslist").executes(context -> {
				registry.getAll().forEach(status -> {
					try {
						context.getSource().getMinecraftServer().getCommandManager().execute(context.getSource().getMinecraftServer().getCommandSource(), String.format("msg %s %s", context.getSource().getPlayer().getName().asString(), status.toString()));
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
					}
				});
				return 0;
			}));
		});
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
}