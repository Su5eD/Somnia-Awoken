package com.kingrunes.somnia.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import java.util.Map;

@MCVersion("1.12.2")
@TransformerExclusions("com.kingrunes.somnia.asm")
public class SFMLLoadingPlugin implements IFMLLoadingPlugin
{
	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] { SClassTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass()
	{
		return SDummyContainer.class.getName();
	}

	@Override
	public void injectData(Map<String, Object> data)
	{

	}
	
	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}