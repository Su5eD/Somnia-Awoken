package com.kingrunes.somnia.server;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.capability.CapabilityFatigue;
import com.kingrunes.somnia.common.capability.IFatigue;
import com.kingrunes.somnia.common.util.ListUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.lang.ref.WeakReference;
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
			
			if (args[1].equalsIgnoreCase("add"))
				Somnia.instance.ignoreList.add(new WeakReference<EntityPlayerMP>(player));
			else if (args[1].equalsIgnoreCase("remove"))
				Somnia.instance.ignoreList.remove(ListUtils.<EntityPlayerMP>getWeakRef(player, Somnia.instance.ignoreList));
			else if (args[1].equalsIgnoreCase("list"))
			{
				List<EntityPlayerMP> players = ListUtils.<EntityPlayerMP>extractRefs(Somnia.instance.ignoreList);
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
}