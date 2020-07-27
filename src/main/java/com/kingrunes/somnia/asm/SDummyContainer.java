package com.kingrunes.somnia.asm;

import com.google.common.eventbus.EventBus;
import com.kingrunes.somnia.SomniaVersion;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.Arrays;

public class SDummyContainer extends DummyModContainer
{
	public SDummyContainer()
	{
		super(new ModMetadata());
		ModMetadata meta = super.getMetadata();
		meta.modId = "somnia_core";
		meta.name = "SomniaCore";
		meta.version = SomniaVersion.getCoreVersionString();
		meta.authorList = Arrays.asList("Kingrunes");
		meta.description = "This mod modifies Minecraft to allow Somnia to hook in";
		meta.url = "";
		meta.updateUrl = "";
		meta.screenshots = new String[0];
		meta.logoFile = "";
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		bus.register(this);
		return true;
	}
}