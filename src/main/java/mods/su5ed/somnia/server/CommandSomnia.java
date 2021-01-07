package mods.su5ed.somnia.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.api.capability.FatigueCapability;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketUpdateFatigue;
import mods.su5ed.somnia.util.ListUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

public class CommandSomnia
{
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("somnia")
				.requires(src -> src.hasPermissionLevel(3))
				.then(Commands.literal("fatigue")
                    .then(Commands.literal("set")
						.then(Commands.argument("amount", DoubleArgumentType.doubleArg())
							.executes(ctx -> CommandSomnia.setFatigue(ctx, DoubleArgumentType.getDouble(ctx, "amount"), null))
								.then(Commands.argument("target", EntityArgument.players())
									.executes(ctx -> CommandSomnia.setFatigue(ctx, DoubleArgumentType.getDouble(ctx, "amount"), EntityArgument.getPlayer(ctx, "targets")))))))
				.then(Commands.literal("override")
					.then(Commands.literal("add")
						.then(Commands.argument("target", EntityArgument.players())
							.executes(ctx -> addOverride(EntityArgument.getPlayer(ctx, "target")))))
					.then(Commands.literal("remove")
						.then(Commands.argument("target", EntityArgument.players())
							.executes(ctx -> removeOverride(EntityArgument.getPlayer(ctx, "target")))))
					.then(Commands.literal("list")
						.executes(CommandSomnia::listOverrides))));

	}

	private static int setFatigue(CommandContext<CommandSource> ctx, double amount, @Nullable ServerPlayerEntity player) throws CommandSyntaxException {
		ServerPlayerEntity target = player != null ? player : ctx.getSource().asPlayer();
		target.getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			props.setFatigue(amount);
			NetworkHandler.sendToClient(new PacketUpdateFatigue(props.getFatigue()), target);
		});
		return Command.SINGLE_SUCCESS;
	}

	private static int addOverride(ServerPlayerEntity target) {
		if (ListUtils.containsRef(target, Somnia.instance.ignoreList))
			target.sendMessage(new StringTextComponent("Override already exists"), UUID.randomUUID());
		else Somnia.instance.ignoreList.add(new WeakReference<>(target));

		return Command.SINGLE_SUCCESS;
	}

	private static int removeOverride(ServerPlayerEntity target) {
		Somnia.instance.ignoreList.remove(ListUtils.getWeakRef(target, Somnia.instance.ignoreList));
		return Command.SINGLE_SUCCESS;
	}

	private static int listOverrides(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity sender = ctx.getSource().asPlayer();
		List<ServerPlayerEntity> players = ListUtils.extractRefs(Somnia.instance.ignoreList);
		String[] aString = ListUtils.playersToStringArray(players);

		ITextComponent chatComponent = new StringTextComponent(aString.length > 0 ? String.join(", ", aString) : "Nothing to see here...");
		sender.sendMessage(chatComponent, UUID.randomUUID());
		return Command.SINGLE_SUCCESS;
	}
}