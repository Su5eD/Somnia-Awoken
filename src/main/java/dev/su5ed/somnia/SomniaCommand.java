package dev.su5ed.somnia;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.client.FatigueUpdatePacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class SomniaCommand {
    public static final Set<UUID> OVERRIDES = new HashSet<>();
    public static final int PERMISSION_LEVEL = 3;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("somnia")
            .requires(src -> src.hasPermission(PERMISSION_LEVEL))
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

    private static int setFatigue(double amount, ServerPlayer player) {
        player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> {
            props.setFatigue(amount);
            SomniaNetwork.sendToClient(new FatigueUpdatePacket(props.getFatigue()), player);
        });
        return Command.SINGLE_SUCCESS;
    }

    private static int addOverride(ServerPlayer player) {
        if (!OVERRIDES.add(player.getUUID())) player.displayClientMessage(Component.literal("Override already exists"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int removeOverride(ServerPlayer target) {
        OVERRIDES.remove(target.getUUID());
        return Command.SINGLE_SUCCESS;
    }

    private static int listOverrides(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer sender = ctx.getSource().getPlayerOrException();
        List<String> overrides = OVERRIDES.stream()
            .map(sender.level()::getPlayerByUUID)
            .filter(Objects::nonNull)
            .map(player -> player.getName().getString())
            .toList();

        Component chatComponent = Component.literal(!overrides.isEmpty() ? String.join(", ", overrides) : "Nothing to see here...");
        sender.displayClientMessage(chatComponent, false);
        return Command.SINGLE_SUCCESS;
    }

    private SomniaCommand() {}
}