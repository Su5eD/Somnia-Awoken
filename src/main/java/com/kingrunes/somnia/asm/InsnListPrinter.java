package com.kingrunes.somnia.asm;

import com.kingrunes.somnia.Somnia;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Iterator;

public class InsnListPrinter
{
	public static void printInsnList(InsnList instructions)
	{
		int i = 0;
		AbstractInsnNode ain;
		Iterator<AbstractInsnNode> iter = instructions.iterator();
		while (iter.hasNext())
		{
			ain = iter.next();
			if (ain instanceof MethodInsnNode)
				Somnia.logger.debug(((MethodInsnNode)ain).name + " @ " + i);
			else
				Somnia.logger.debug(ain.toString() + " @ " + i);
			i++;
		}
	}
}