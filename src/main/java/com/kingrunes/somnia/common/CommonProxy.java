package com.kingrunes.somnia.common;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.capability.CapabilityFatigue;
import com.kingrunes.somnia.common.capability.IFatigue;
import com.kingrunes.somnia.common.util.ClassUtils;
import com.kingrunes.somnia.common.util.TimePeriod;
import com.kingrunes.somnia.server.ForgeEventHandler;
import com.kingrunes.somnia.server.ServerTickHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CommonProxy
{
	private static final int CONFIG_VERSION = 2;
	
	public TimePeriod 	enterSleepPeriod;
	public TimePeriod 	validSleepPeriod;
	
	public double 		fatigueRate,
						fatigueReplenishRate,
						minimumFatigueToSleep,
						baseMultiplier,
						multiplierCap;
	
	public boolean 		fatigueSideEffects,
						tpsGraph,
						secondsOnGraph,
						sleepWithArmor,
						vanillaBugFixes,
						fading,
						somniaGui,
						muteSoundWhenSleeping,
						ignoreMonsters,
						disableCreatureSpawning,
						disableRendering,
						disableMoodSoundAndLightCheck;
	
	public String		displayFatigue;
	
	public static ForgeEventHandler forgeEventHandler;
	
	public void configure(File file)
	{
		Configuration config = new Configuration(file);
		config.load();
		Property property = config.get(Configuration.CATEGORY_GENERAL, "configVersion", 0);
		if (property.getInt() != CONFIG_VERSION)
			file.delete();
		config = new Configuration(file);
		config.load();
		
		config.get(Configuration.CATEGORY_GENERAL, "configVersion", CONFIG_VERSION);

		/*
		 * Timings
		 */
		enterSleepPeriod =
				new TimePeriod(
						config.get("timings", "enterSleepStart", 0).getInt(),
						config.get("timings", "enterSleepEnd", 24000).getInt()
						);
		validSleepPeriod =
				new TimePeriod(
						config.get("timings", "validSleepStart", 0).getInt(),
						config.get("timings", "validSleepEnd", 24000).getInt()
						);
		
		/*
		 * Fatigue
		 */
		fatigueSideEffects = config.get("fatigue", "fatigueSideEffects", true).getBoolean(true);
		displayFatigue = config.get("fatigue", "displayFatigue", "br").getString();
		fatigueRate = config.get("fatigue", "fatigueRate", 0.00208d).getDouble(0.00208d);
		fatigueReplenishRate = config.get("fatigue", "fatigueReplenishRate", 0.00833d).getDouble(0.00833d);
		minimumFatigueToSleep = config.get("fatigue", "minimumFatigueToSleep", 20.0d).getDouble(20.0d);
		
		/*
		 * Logic
		 */
		baseMultiplier = config.get("logic", "baseMultiplier", 1.0d).getDouble(1.0d);
		multiplierCap = config.get("logic", "multiplierCap", 100.0d).getDouble(100.0d);
		
		/*
		 * Profiling (Not implemented)
		secondsOnGraph = config.get("profiling", "secondsOnGraph", 30).getInt();
		tpsGraph = config.get("profiling", "tpsGraph", false).getBoolean(false);
		*/
		
		/*
		 * Options
		 */
		sleepWithArmor = config.get("options", "sleepWithArmor", false).getBoolean(false);
		vanillaBugFixes = config.get("options", "vanillaBugFixes", true).getBoolean(true);
		fading = config.get("options", "fading", true).getBoolean(true);
		somniaGui = config.get("options", "somniaGui", true).getBoolean(true);
		muteSoundWhenSleeping = config.get("options", "muteSoundWhenSleeping", false).getBoolean(false);
		ignoreMonsters = config.get("options", "ignoreMonsters", false).getBoolean(false);

		/*
		 * Performance
		 */
		disableCreatureSpawning = config.get("performance", "disableCreatureSpawning", false).getBoolean(false);
		disableRendering = config.get("performance", "disableRendering", false).getBoolean(false);
		disableMoodSoundAndLightCheck = config.get("performance", "disableMoodSoundAndLightCheck", false).getBoolean(false);
		
		config.save();
	}
	
	public void register()
	{
		MinecraftForge.EVENT_BUS.register(this);
		
		FMLCommonHandler.instance().bus().register(new PlayerSleepTickHandler());
		
		forgeEventHandler = new ForgeEventHandler();
		MinecraftForge.EVENT_BUS.register(forgeEventHandler);
		FMLCommonHandler.instance().bus().register(forgeEventHandler);
		CapabilityFatigue.register();
	}
	
	@SubscribeEvent
	public void worldLoadHook(WorldEvent.Load event)
	{
		if (event.getWorld() instanceof WorldServer)
		{
			WorldServer worldServer = (WorldServer)event.getWorld();
			Somnia.instance.tickHandlers.add(new ServerTickHandler(worldServer));
			System.out.println("[Somnia] Registering tick handler for loading world!"); //TODO: Get a logger
		}
	}
	
	@SubscribeEvent
	public void worldUnloadHook(WorldEvent.Unload event)
	{
		if (event.getWorld() instanceof WorldServer)
		{
			WorldServer worldServer = (WorldServer)event.getWorld();
			Iterator<ServerTickHandler> iter = Somnia.instance.tickHandlers.iterator();
			ServerTickHandler serverTickHandler;
			while (iter.hasNext())
			{
				serverTickHandler = iter.next();
				if (serverTickHandler.worldServer == worldServer)
				{
					System.out.println("[Somnia] Removing tick handler for unloading world!");
					iter.remove();
					break;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerDamage(LivingHurtEvent event)
	{
		if (event.getEntityLiving() instanceof EntityPlayerMP)
		{
			if (!(event.getEntityLiving()).isPlayerSleeping())
				return;
			
	        Somnia.eventChannel.sendTo(PacketHandler.buildGUIClosePacket(), (EntityPlayerMP) event.getEntityLiving());
		}
	}
	
	@SubscribeEvent
	public void sleepHook(PlayerSleepInBedEvent event)
	{
		onSleep(event);
	}
	
	/*
	 * This method reimplements the entire sleep checking logic, look away
	 */
	public void onSleep(PlayerSleepInBedEvent event)
	{
		if (event.getResultStatus() != null && event.getResultStatus() != EntityPlayer.SleepResult.OK)
			return;

		BlockPos pos = event.getPos();
		EntityPlayer player = event.getEntityPlayer();

		if (!player.world.isRemote)
        {
			IFatigue props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
			if (props != null && props.getFatigue() < minimumFatigueToSleep)
			{
				event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
				player.sendMessage(new TextComponentTranslation("somnia.status.cooldown"));
				return;
			}
			
			if (!enterSleepPeriod.isTimeWithin(player.world.getWorldTime() % 24000))
			{
				event.setResult(EntityPlayer.SleepResult.NOT_POSSIBLE_NOW);
				return;
			}
			
			if (!sleepWithArmor && Somnia.doesPlayHaveAnyArmor(player))
			{
				event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
				player.sendMessage(new TextComponentTranslation("somnia.status.armor"));
				return;
			}
			
            if (player.isPlayerSleeping() || !player.isEntityAlive())
            {
            	event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
            	return;
            }

            if (!player.world.provider.isSurfaceWorld())
            {
            	event.setResult(EntityPlayer.SleepResult.NOT_POSSIBLE_HERE);
                return;
            }

            if (Math.abs(player.posX - (double)pos.getX()) > 3.0d || Math.abs(player.posY - (double)pos.getY()) > 2.0d || Math.abs(player.posZ - (double)pos.getZ()) > 3.0d)
            {
                event.setResult(EntityPlayer.SleepResult.TOO_FAR_AWAY);
                return;
            }

            if (!this.ignoreMonsters)
            {
	            double d0 = 8.0D;
	            double d1 = 5.0D;
	            
				List<?> list = player.world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double)pos.getX() - d0, (double)pos.getY() - d1, (double)pos.getZ() - d0, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d0));
	
	            if (!list.isEmpty())
	            {
	                event.setResult(EntityPlayer.SleepResult.NOT_SAFE);
	                return;
	            }
            }
        }

        if (player.isRiding())
        {
        	player.dismountRidingEntity();
        }

        ClassUtils.setSize(player, 0.2F, 0.2F);
//        event.entityPlayer.yOffset = 0.2F;

        if (player.world.isBlockLoaded(pos))
        {
			IBlockState state = player.world.getBlockState(pos);
        	EnumFacing enumFacing = state.getBlock().getBedDirection(state, player.world, pos);
            float f1 = 0.5F;
            float f = 0.5F;
            player.renderOffsetX = 0.0F;
            player.renderOffsetZ = 0.0F;

            switch (enumFacing) // FACING_LOOKUP isn't visible.. lets do this our own way
            {
                case SOUTH:
                    f1 = 0.9F;
                    player.renderOffsetX = -1.8F;
                    break;
                case NORTH:
                    f = 0.1F;
                    player.renderOffsetX = 1.8F;
                    break;
                case WEST:
                    f1 = 0.1F;
                    player.renderOffsetX = 1.8F;
                    break;
                case EAST:
                    f = 0.9F;
                    player.renderOffsetX = -1.8F;
                    break;
                default:
                	break;
            }

//            ClassUtils.call_func_71013_b(event.entityPlayer, l);
            player.setPosition(((float)pos.getX() + f), ((float)pos.getY() + 0.9375F), ((float)pos.getZ() + f1));
        }
        else
        	player.setPosition(((float)pos.getX() + 0.5F), ((float)pos.getY() + 0.9375F), ((float)pos.getZ() + 0.5F));

        ClassUtils.setSleeping(player, true);
        ClassUtils.setSleepTimer(player, 0);
        player.setPosition(pos.getX(), pos.getY(), pos.getZ());
        player.motionX = player.motionZ = player.motionY = 0.0D;

        if (!player.world.isRemote)
        {
            player.world.updateAllPlayersSleepingFlag();
        }
		
        event.setResult(EntityPlayer.SleepResult.OK);
        
        if (player.world.isRemote)
	        return;
        
        Somnia.eventChannel.sendTo(PacketHandler.buildGUIOpenPacket(), (EntityPlayerMP) player);
	}
	
	/*
	 * The following methods are implemented client-side only
	 */
	
	public void handleGUIOpenPacket() throws IOException
	{}

	public void handlePropUpdatePacket(DataInputStream in) throws IOException
	{}

	public void handleGUIClosePacket(EntityPlayerMP player)
	{}
}