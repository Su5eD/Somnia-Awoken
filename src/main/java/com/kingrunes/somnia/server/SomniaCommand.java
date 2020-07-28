package com.kingrunes.somnia.server;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.capability.CapabilityFatigue;
import com.kingrunes.somnia.common.capability.IFatigue;
import com.kingrunes.somnia.common.util.ListUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class SomniaCommand extends CommandBase
{
	private static final String	COMMAND_NAME 			= "somnia",
								COMMAND_USAGE			= "[override [add <player>|remove <player>|list]] [fatigue [set <player>]]",
								COMMAND_USAGE_CONSOLE	= "[override [add [player]|remove [player]|list]] [fatigue [set [player]]]",
								COMMAND_USAGE_FORMAT	= "/%s %s";
	
	@Override
	public String getName()
	{
		return COMMAND_NAME;
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return String.format(COMMAND_USAGE_FORMAT, COMMAND_NAME, COMMAND_USAGE);
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 3;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length < 2)
			throw new WrongUsageException(getUsage(sender));
		
		EntityPlayerMP player;
		if (args[0].equalsIgnoreCase("override"))
		{
			if (args.length > 2)
				player = sender.getServer().getPlayerList().getPlayerByUsername(args[2]);
			else
			{
				if (sender instanceof EntityPlayerMP)
					player = (EntityPlayerMP)sender;
				else
					throw new WrongUsageException(String.format(COMMAND_USAGE_FORMAT, COMMAND_NAME, COMMAND_USAGE_CONSOLE));
			}
			
			if (args[1].equalsIgnoreCase("add")) {
				if (ListUtils.containsRef(player, Somnia.instance.ignoreList))
					sender.sendMessage(new TextComponentString("Override already exists"));
				else Somnia.instance.ignoreList.add(new WeakReference<>(player));
			}
			else if (args[1].equalsIgnoreCase("remove"))
				Somnia.instance.ignoreList.remove(ListUtils.getWeakRef(player, Somnia.instance.ignoreList));
			else if (args[1].equalsIgnoreCase("list"))
			{
				List<EntityPlayerMP> players = ListUtils.extractRefs(Somnia.instance.ignoreList);
				String[] astring = ListUtils.playersToStringArray(players);
				ITextComponent chatComponent = new TextComponentString(astring.length > 0 ? joinNiceString(astring) : "Nothing to see here...");
				sender.sendMessage(chatComponent);
			}
			else
				throw new WrongUsageException(getUsage(sender));
		}
		else if (args[0].equalsIgnoreCase("fatigue"))
		{
			if (args.length > 3)
				player = sender.getServer().getPlayerList().getPlayerByUsername(args[3]);
			else
			{
				if (sender instanceof EntityPlayerMP)
					player = (EntityPlayerMP)sender;
				else
					throw new WrongUsageException(String.format(COMMAND_USAGE_FORMAT, COMMAND_NAME, COMMAND_USAGE_CONSOLE));
			}
			
			if (args[1].equalsIgnoreCase("set"))
			{
				IFatigue props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
				if (props != null)
				{
					try
					{
						props.setFatigue(Double.parseDouble(args[2]));
						Somnia.eventChannel.sendTo(PacketHandler.buildPropUpdatePacket(0x01, 0x00, props.getFatigue()), player);
					}
					catch (NumberFormatException nfe)
					{
						sender.sendMessage(new TextComponentString("Invalid double!"));
					}
				}
				else
					sender.sendMessage(new TextComponentString("props = null! Weird..."));
			}
			else
				throw new WrongUsageException(getUsage(sender));
		}
	}

	@Nonnull
	private String getSecondLastWord(String[] arr) {
		if (arr.length <= 1) return "";
		return arr[arr.length - 2];
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		String arg = getSecondLastWord(args);
		if ("fatigue".equals(arg))
			return Collections.singletonList("set");
		else if ("override".equals(arg)) return getListOfStringsMatchingLastWord(args, "add", "remove", "list");
		else if (args.length == 3 && !arg.equals("list")) return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());

		return args.length < 2 ? getListOfStringsMatchingLastWord(args, "fatigue", "override") : Collections.emptyList();
	}
}