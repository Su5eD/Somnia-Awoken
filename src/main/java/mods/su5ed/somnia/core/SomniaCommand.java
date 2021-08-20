package mods.su5ed.somnia.core;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketUpdateFatigue;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.*;
import java.util.stream.Collectors;

public class SomniaCommand {
	public static final Set<UUID> OVERRIDES = new HashSet<>();

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("somnia")
				.requires(src -> src.hasPermission(3))
				.then(Commands.literal("fatigue")
                    .then(Commands.literal("set")
						.then(Commands.argument("amount", DoubleArgumentType.doubleArg())
							.executes(ctx -> SomniaCommand.setFatigue(DoubleArgumentType.getDouble(ctx, "amount"), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("target", EntityArgument.players())
									.executes(ctx -> SomniaCommand.setFatigue(DoubleArgumentType.getDouble(ctx, "amount"), EntityArgument.getPlayer(ctx, "target")))))))
				.then(Commands.literal("override")
					.then(Commands.literal("add")
						.then(Commands.argument("target", EntityArgument.players())
							.executes(ctx -> addOverride(EntityArgument.getPlayer(ctx, "target")))))
					.then(Commands.literal("remove")
						.then(Commands.argument("target", EntityArgument.players())
							.executes(ctx -> removeOverride(EntityArgument.getPlayer(ctx, "target")))))
					.then(Commands.literal("list")
						.executes(SomniaCommand::listOverrides))));

	}

	private static int setFatigue(double amount, ServerPlayerEntity player) {
		player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> {
			props.setFatigue(amount);
			NetworkHandler.sendToClient(new PacketUpdateFatigue(props.getFatigue()), player);
		});
		return Command.SINGLE_SUCCESS;
	}

	private static int addOverride(ServerPlayerEntity player) {
		if (!OVERRIDES.add(player.getUUID())) player.displayClientMessage(new StringTextComponent("Override already exists"), true);

		return Command.SINGLE_SUCCESS;
	}

	private static int removeOverride(ServerPlayerEntity target) {
		OVERRIDES.remove(target.getUUID());
		return Command.SINGLE_SUCCESS;
	}

	private static int listOverrides(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity sender = ctx.getSource().getPlayerOrException();
		List<String> overrides = OVERRIDES.stream()
				.map(sender.level::getPlayerByUUID)
				.filter(Objects::nonNull)
				.map(player -> player.getName().getContents())
				.collect(Collectors.toList());

		ITextComponent chatComponent = new StringTextComponent(!overrides.isEmpty() ? String.join(", ", overrides) : "Nothing to see here...");
		sender.displayClientMessage(chatComponent, false);
		return Command.SINGLE_SUCCESS;
	}
}