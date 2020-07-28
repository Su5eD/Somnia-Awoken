package com.kingrunes.somnia.asm;

import com.kingrunes.somnia.Somnia;
import org.objectweb.asm.ClassWriter;

public class SomniaClassWriter extends ClassWriter
{
	private String precalculatedCommonSuperClass;
	
	public SomniaClassWriter(int flags, String precalculatedCommonSuperClass)
	{
		super(flags);
		this.precalculatedCommonSuperClass = precalculatedCommonSuperClass;
	}

	@Override
	protected String getCommonSuperClass(String a, String b)
	{
		Somnia.logger.debug("a = " + a + " b = " + b);
		if (precalculatedCommonSuperClass != null)
		{
			Somnia.logger.debug("Overriding common superclass with: " + precalculatedCommonSuperClass);
		}
		else
		{
			precalculatedCommonSuperClass = super.getCommonSuperClass(a, b);
			Somnia.logger.debug("a = " + a + " b = " + b + " output = " + precalculatedCommonSuperClass);
		}
		return precalculatedCommonSuperClass;
	}
}