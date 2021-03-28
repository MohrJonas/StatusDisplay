package com.github.nastygamer.statusdisplay;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class StatusDisplay implements ModInitializer {

	private final ImmutableList<Status> statusList = ImmutableList.of(new AfkStatus(), new BusyStatus(), new OnlineStatus());

	@Override
	public void onInitialize() {
		System.out.println("Hello from StatusDisplay");
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(
					literal("status")
							.then(argument("name", word()).executes(context -> {
								if (statusList.stream().noneMatch(status -> status.getName().equals(context.getArgument("name", String.class))))
									return 0;
								moveIntoTeam(statusList.stream().filter(status -> status.getName().equals(context.getArgument("name", String.class))).findFirst().get(), context.getSource().getPlayer(), context);
								return 1;
							}))
			);
		});
	}

	private void moveIntoTeam(Status status, ServerPlayerEntity player, CommandContext<ServerCommandSource> context) {
		final String teamCreateCommand = String.format("team add %s", status.getName());
		final String teamColorCommand = String.format("team modify %s color %s", status.getName(), status.getColor());
		final String teamPrefixCommand = String.format("team modify %s prefix {\"text\": \"%s \"}", status.getName(), status.getPrefix());
		final String teamJoinCommand = String.format("team join %s %s", status.getName(), player.getName().asString());
		context.getSource().getMinecraftServer().getCommandManager().execute(context.getSource(), teamCreateCommand);
		context.getSource().getMinecraftServer().getCommandManager().execute(context.getSource(), teamColorCommand);
		context.getSource().getMinecraftServer().getCommandManager().execute(context.getSource(), teamPrefixCommand);
		context.getSource().getMinecraftServer().getCommandManager().execute(context.getSource(), teamJoinCommand);
	}
}