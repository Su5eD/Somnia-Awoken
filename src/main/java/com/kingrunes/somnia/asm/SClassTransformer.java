package com.kingrunes.somnia.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.*;

public class SClassTransformer implements IClassTransformer
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (name.equalsIgnoreCase("net.minecraft.client.renderer.EntityRenderer"))
			return patchEntityRenderer(bytes, false);
		else if (name.equalsIgnoreCase("buo"))
			return patchEntityRenderer(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.world.WorldServer"))
			return patchWorldServer(bytes, false);
		else if (name.equalsIgnoreCase("om"))
			return patchWorldServer(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.world.chunk.Chunk"))
			return patchChunk(bytes, false);
		else if (name.equalsIgnoreCase("axu"))
			return patchChunk(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.server.MinecraftServer"))
			return patchMinecraftServer(bytes);
		else if (name.equalsIgnoreCase("net.minecraft.item.ItemClock") || name.equalsIgnoreCase("ahj"))
			return patchItemClock(bytes);
		else if (name.equalsIgnoreCase("net.minecraft.entity.player.EntityPlayer"))
			return patchEntityPlayer(bytes, false);
		else if (name.equalsIgnoreCase("aeb"))
			return patchEntityPlayer(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.block.BlockBed"))
			return patchBlockBed(bytes, false);
		else if (name.equalsIgnoreCase("aos"))
			return patchBlockBed(bytes, true);
		return bytes;
	}

	private byte[] patchBlockBed(byte[] bytes, boolean obf) {
		String 	methodOnBlockActivated = obf ? "a" : "onBlockActivated";

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for (MethodNode m : classNode.methods) {
			if (m.name.equals(methodOnBlockActivated) && m.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Z")) {
				//Add wake time calculation
				InsnList insnList = new InsnList();
				insnList.add(new FrameNode(Opcodes.F_APPEND, 1, new Object[]{"net/minecraft/item/ItemStack"}, 0, null));
				insnList.add(new VarInsnNode(ALOAD, 1));
				insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", "getTotalWorldTime", "()J", false));
				insnList.add(new VarInsnNode(LSTORE, 10));
				m.localVariables.add(new LocalVariableNode("totalWorldTime", "J", null, (LabelNode) m.instructions.get(5), (LabelNode) m.instructions.get(9), 10));
				LabelNode label24 = new LabelNode();
				insnList.add(label24);
				insnList.add(new VarInsnNode(LLOAD, 10));
				insnList.add(new VarInsnNode(LLOAD, 10));
				insnList.add(new LdcInsnNode(24000L));
				insnList.add(new InsnNode(LREM));
				insnList.add(new LdcInsnNode(12000L));
				insnList.add(new InsnNode(LCMP));
				LabelNode label25 = new LabelNode();
				insnList.add(new JumpInsnNode(IFLE, label25));
				insnList.add(new InsnNode(ICONST_0));
				LabelNode label26 = new LabelNode();
				insnList.add(new JumpInsnNode(GOTO, label26));
				insnList.add(label25);
				insnList.add(new IntInsnNode(SIPUSH, 12000));
				insnList.add(label26);
				insnList.add(new MethodInsnNode(INVOKESTATIC, "com/kingrunes/somnia/Somnia", "calculateWakeTime", "(JI)J", false));
				insnList.add(new FieldInsnNode(PUTSTATIC, "com/kingrunes/somnia/Somnia", "clientAutoWakeTime", "J"));
				m.instructions.insert(m.instructions.get(6), insnList);

				//for (AbstractInsnNode insn : m.instructions.toArray()) System.out.println("ins: " + insn + "   " + m.instructions.indexOf(insn) + "   " + (insn instanceof LineNumberNode ? ((LineNumberNode) insn).line : ""));
				break;
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		return cw.toByteArray();
	}

	private byte[] patchEntityPlayer(byte[] bytes, boolean obf) {
		String 	methodSleep = obf ? "a" : "trySleep";

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		Iterator<MethodNode> methods = classNode.methods.iterator();
		AbstractInsnNode ain;

		Label l115 = new Label();

		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if (m.name.equals(methodSleep) && m.desc.equals("(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/entity/player/EntityPlayer$SleepResult;"))
			{
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				while (iter.hasNext())
				{
					ain = iter.next();
					if (ain instanceof VarInsnNode && m.instructions.indexOf(ain) == 5) //IFatique variable
					{
						InsnList insnList = new InsnList();
						insnList.add(new VarInsnNode(ALOAD, 0));
						insnList.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/common/capability/CapabilityFatigue", "FATIGUE_CAPABILITY", "Lnet/minecraftforge/common/capabilities/Capability;"));
						insnList.add(new InsnNode(ACONST_NULL));
						insnList.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/entity/EntityLivingBase", "getCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", false));
						insnList.add(new TypeInsnNode(CHECKCAST, "com/kingrunes/somnia/common/capability/IFatigue"));
						insnList.add(new VarInsnNode(ASTORE, 11));
						insnList.add(new LabelNode(l115));

						m.instructions.insert(ain, insnList);
						m.localVariables.add(new LocalVariableNode("fatigue", "Lcom/kingrunes/somnia/common/capability/IFatigue;", null, new LabelNode(l115), (LabelNode) m.instructions.get(128), 11));
						m.maxLocals = 12;
					}

					if (ain instanceof JumpInsnNode && m.instructions.indexOf(ain) == 115) {
						LabelNode label18 = ((JumpInsnNode) ain).label;
						for (byte i = 0; i < 4; i++) m.instructions.remove(m.instructions.get(112)); //Remove sleep time check

						InsnList insnList = new InsnList(); //Change sleep time check
						insnList.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/common/CommonProxy", "enterSleepPeriod", "Lcom/kingrunes/somnia/common/util/TimePeriod;"));
						insnList.add(new VarInsnNode(ALOAD, 0));
						insnList.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/player/EntityPlayer", "world", "Lnet/minecraft/world/World;"));
						insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", "getWorldTime", "()J", false));
						insnList.add(new LdcInsnNode(24000L));
						insnList.add(new InsnNode(LREM));
						insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "com/kingrunes/somnia/common/util/TimePeriod", "isTimeWithin", "(J)Z", false));
						LabelNode label175 = new LabelNode();
						insnList.add(new JumpInsnNode(IFNE, label175));
						m.instructions.insert(m.instructions.get(111), insnList);

						InsnList insnList2 = new InsnList();
						insnList2.add(label175);
						insnList2.add(new VarInsnNode(ALOAD, 11));
						insnList2.add(new JumpInsnNode(IFNULL, label18));
						insnList2.add(new VarInsnNode(ALOAD, 11));
						insnList2.add(new MethodInsnNode(INVOKEINTERFACE, "com/kingrunes/somnia/common/capability/IFatigue", "getFatigue", "()D", true));
						insnList2.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/common/CommonProxy", "minimumFatigueToSleep", "D"));
						insnList2.add(new InsnNode(DCMPG));
						insnList2.add(new JumpInsnNode(IFGE, label18));
						LabelNode label176 = new LabelNode();
						insnList2.add(label176);
						insnList2.add(new VarInsnNode(ALOAD, 0));
						insnList2.add(new TypeInsnNode(NEW, "net/minecraft/util/text/TextComponentTranslation"));
						insnList2.add(new InsnNode(DUP));
						insnList2.add(new LdcInsnNode("somnia.status.cooldown"));
						insnList2.add(new InsnNode(ICONST_0));
						insnList2.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
						insnList2.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/util/text/TextComponentTranslation", "<init>", "(Ljava/lang/String;[Ljava/lang/Object;)V", false));
						insnList2.add(new InsnNode(ICONST_1));
						insnList2.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", "sendStatusMessage", "(Lnet/minecraft/util/text/ITextComponent;Z)V", false));
						LabelNode label177 = new LabelNode();
						insnList2.add(label177);
						insnList2.add(new FieldInsnNode(GETSTATIC, "net/minecraft/entity/player/EntityPlayer$SleepResult", "OTHER_PROBLEM", "Lnet/minecraft/entity/player/EntityPlayer$SleepResult;"));
						insnList2.add(new InsnNode(ARETURN));
						m.instructions.insert(m.instructions.get(123), insnList2);

						InsnList insnList3 = new InsnList(); //Add a ignoremonsters check to existing if statement
						insnList3.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/common/CommonProxy", "ignoreMonsters", "Z"));
						JumpInsnNode ainsnode = (JumpInsnNode) m.instructions.get(215);
						insnList3.add(new JumpInsnNode(IFNE, ainsnode.label));
						m.instructions.insert(ainsnode, insnList3);

						InsnList insnList4 = new InsnList(); //Armor check
						LabelNode label20 = (LabelNode) m.instructions.get(157);
						LabelNode label195 = new LabelNode();
						m.instructions.insert(m.instructions.get(152), new JumpInsnNode(IFNE, label195));
						m.instructions.remove(m.instructions.get(152));
						insnList4.add(label195);
						insnList4.add(new FrameNode(Opcodes.F_APPEND, 2, new Object[]{"net/minecraft/util/math/BlockPos", "net/minecraft/entity/player/EntityPlayer"}, 0, null));
						insnList4.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/common/CommonProxy", "sleepWithArmor", "Z"));
						insnList4.add(new JumpInsnNode(IFNE, label20));
						insnList4.add(new VarInsnNode(ALOAD, 0));
						insnList4.add(new MethodInsnNode(INVOKESTATIC, "com/kingrunes/somnia/Somnia", "doesPlayHaveAnyArmor", "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
						insnList4.add(new JumpInsnNode(IFEQ, label20));
						LabelNode label148 = new LabelNode();
						insnList4.add(label148);
						insnList4.add(new VarInsnNode(ALOAD, 0)); //Send armor status to player
						insnList4.add(new TypeInsnNode(NEW, "net/minecraft/util/text/TextComponentTranslation"));
						insnList4.add(new InsnNode(DUP));
						insnList4.add(new LdcInsnNode("somnia.status.armor"));
						insnList4.add(new InsnNode(ICONST_0));
						insnList4.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
						insnList4.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/util/text/TextComponentTranslation", "<init>", "(Ljava/lang/String;[Ljava/lang/Object;)V", false));
						insnList4.add(new InsnNode(ICONST_1));
						insnList4.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", "sendStatusMessage", "(Lnet/minecraft/util/text/ITextComponent;Z)V", false));

						LabelNode label149 = new LabelNode(); //Return
						insnList4.add(label149);
						insnList4.add(new FieldInsnNode(GETSTATIC, "net/minecraft/entity/player/EntityPlayer$SleepResult", "OTHER_PROBLEM", "Lnet/minecraft/entity/player/EntityPlayer$SleepResult;"));
						insnList4.add(new InsnNode(ARETURN));
						m.instructions.insert(m.instructions.get(156), insnList4);

						//Send GuiOpen packet
						InsnList insnList5 = new InsnList();
						LabelNode label425 = new LabelNode();
						insnList5.add(label425);
						insnList5.add(new VarInsnNode(ALOAD, 0));
						insnList5.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/player/EntityPlayer", "world", "Lnet/minecraft/world/World;"));
						insnList5.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "isRemote", "Z"));
						insnList5.add(new JumpInsnNode(IFNE, (LabelNode)m.instructions.get(382)));
						LabelNode label426 = new LabelNode();
						insnList5.add(label426);
						insnList5.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/Somnia", "eventChannel", "Lnet/minecraftforge/fml/common/network/FMLEventChannel;"));
						insnList5.add(new MethodInsnNode(INVOKESTATIC, "com/kingrunes/somnia/common/PacketHandler", "buildGUIOpenPacket", "()Lnet/minecraftforge/fml/common/network/internal/FMLProxyPacket;", false));
						insnList5.add(new VarInsnNode(ALOAD, 0));
						insnList5.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/player/EntityPlayerMP"));
						insnList5.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/fml/common/network/FMLEventChannel", "sendTo", "(Lnet/minecraftforge/fml/common/network/internal/FMLProxyPacket;Lnet/minecraft/entity/player/EntityPlayerMP;)V", false));
						LabelNode label427 = new LabelNode();
						insnList5.add(label427);
						m.instructions.insert(m.instructions.get(381), insnList5);
					}
				}
				break;
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		return cw.toByteArray();
	}

	private byte[] patchEntityRenderer(byte[] bytes, boolean obf)
	{
		String methodName = obf ? "a" : "updateCameraAndRender";
		String methodName2 = obf ? "b" : "renderWorld";
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		boolean f = true;

		for (MethodNode m : classNode.methods) {
			if (m.name.equals(methodName) && m.desc.equals("(F)V")) {
				AbstractInsnNode ain;
				MethodInsnNode min;
				VarInsnNode vin;
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				while (iter.hasNext()) {
					ain = iter.next();
					if (ain instanceof MethodInsnNode) {
						min = (MethodInsnNode) ain;
						if (min.name.equals(methodName2) && min.desc.equalsIgnoreCase("(FJ)V") && min.getOpcode() == Opcodes.INVOKEVIRTUAL) {
							min.setOpcode(Opcodes.INVOKESTATIC);
							min.name = "renderWorld";
							min.owner = "com/kingrunes/somnia/Somnia";

							vin = (VarInsnNode) m.instructions.get(m.instructions.indexOf(min) - (f ? 9 : 3));
							m.instructions.remove(vin);

							f = false;
						}
					}
				}
				break;
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		return cw.toByteArray();
	}


	private byte[] patchWorldServer(byte[] bytes, boolean obf)
	{
		String 	methodTick = obf ? "d" : "tick",
				methodGetGameRule = obf ? "b" : "getBoolean";

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		Iterator<MethodNode> methods = classNode.methods.iterator();
		AbstractInsnNode ain;
		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if (m.name.equals(methodTick) && m.desc.equals("()V"))
			{
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				MethodInsnNode min;
				while (iter.hasNext())
				{
					ain = iter.next();
					if (ain instanceof MethodInsnNode)
					{
						min = (MethodInsnNode)ain;
						if (min.name.equals(methodGetGameRule) && min.desc.equals("(Ljava/lang/String;)Z"))
						{
							int index = m.instructions.indexOf(min);

							LdcInsnNode lin = (LdcInsnNode)m.instructions.get(index-1);
							if (lin.cst.equals("doMobSpawning"))
							{
								min.setOpcode(Opcodes.INVOKESTATIC);
								min.desc = "(Lnet/minecraft/world/WorldServer;)Z";
								min.name = "doMobSpawning";
								min.owner = "com/kingrunes/somnia/Somnia";

								m.instructions.remove(lin);
								m.instructions.remove(m.instructions.get(index-2));
								break;
							}
						}
					}
				}
				break;
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		return cw.toByteArray();
	}

	private byte[] patchChunk(byte[] bytes, boolean obf)
	{
		String methodName = obf ? "b" : "onTick";
		String methodName2 = obf ? "o" : "checkLight";

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		Iterator<MethodNode> methods = classNode.methods.iterator();
		AbstractInsnNode ain;
		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if (m.name.equals(methodName))
			{
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				while (iter.hasNext())
				{
					ain = iter.next();
					if (ain instanceof MethodInsnNode)
					{
						MethodInsnNode min = (MethodInsnNode)ain;
						if (min.name.equals(methodName2))
						{
							min.setOpcode(Opcodes.INVOKESTATIC);
							min.desc = "(Lnet/minecraft/world/chunk/Chunk;)V";
							min.name = "chunkLightCheck";
							min.owner = "com/kingrunes/somnia/Somnia";
						}
					}
				}
				break;
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		return cw.toByteArray();
	}

	private byte[] patchMinecraftServer(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		Iterator<MethodNode> methods = classNode.methods.iterator();
		AbstractInsnNode ain;
		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if ((m.name.equals("c") || m.name.equals("tick")) && m.desc.equals("()V"))
			{
				AbstractInsnNode lrin = null;
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				while (iter.hasNext())
				{
					ain = iter.next();
					if (ain instanceof InsnNode && (ain).getOpcode() == Opcodes.RETURN)
						lrin = ain;
				}

				if (lrin != null)
				{
					InsnList toInject = new InsnList();
					toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/kingrunes/somnia/Somnia", "tick", "()V", false));

					m.instructions.insertBefore(lrin, toInject);
				}
				break;
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		return cw.toByteArray();
	}

	private byte[] patchItemClock(byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, "onItemUseFirst", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFFLnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;", null, null);
		methodNode.visitCode();
		methodNode.visitVarInsn(ALOAD, 1);
		methodNode.visitVarInsn(ALOAD, 2);
		methodNode.visitVarInsn(ALOAD, 3);
		methodNode.visitVarInsn(ALOAD, 4);
		methodNode.visitVarInsn(FLOAD, 5);
		methodNode.visitVarInsn(FLOAD, 6);
		methodNode.visitVarInsn(FLOAD, 7);
		methodNode.visitVarInsn(ALOAD, 8);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "com/kingrunes/somnia/Somnia", "onItemUseFirst", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFFLnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;", false);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitEnd();

		classNode.methods.add(methodNode);

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		return cw.toByteArray();
	}
}