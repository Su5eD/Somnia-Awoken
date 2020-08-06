package com.kingrunes.somnia.asm;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.kingrunes.somnia.SomniaVersion;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import scala.actors.threadpool.Arrays;

import java.util.Collections;

public class SDummyContainer extends DummyModContainer
{
	public SDummyContainer()
	{
		super(new ModMetadata());
		ModMetadata meta = super.getMetadata();
		meta.modId = "somniacore";
		meta.name = "SomniaCore";
		meta.version = SomniaVersion.getCoreVersionString();
		meta.authorList = Lists.newArrayList("Kingrunes", "Su5eD");
		meta.description = "This mod modifies Minecraft to allow Somnia to hook in";
		meta.screenshots = new String[0];
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		bus.register(this);
		return true;
	}
}