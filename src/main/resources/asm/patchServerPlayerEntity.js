function initializeCoreMod() {
    return {
        'transformServerPlayerEntity': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.player.ServerPlayerEntity',
                'methodName': 'func_213819_a', // trySleep
                'methodDesc': '(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;'
            },
            'transformer': function (method) {
                var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
                var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');

                ASM.log("INFO", "Patching class ServerPlayerEntity")

                for (var i = 0; i < method.instructions.size(); i++) {
                    var node = method.instructions.get(i);
                    if (node instanceof MethodInsnNode) {
                        if (node.opcode === Opcodes.INVOKEINTERFACE && node.owner === "java/util/List" && node.name === "isEmpty") {
                            ASM.log("INFO", "Overriding monsters check")
                            var jumpInsnNode = method.instructions.get(i + 1);
                            var list = new InsnList();
                            var label = new LabelNode();
                            list.add(label)
                            list.add(new FieldInsnNode(Opcodes.GETSTATIC, "mods/su5ed/somnia/common/config/SomniaConfig", "ignoreMonsters", "Z"));
                            list.add(new JumpInsnNode(Opcodes.IFNE, jumpInsnNode.label));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "isCreative", "()Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFNE, jumpInsnNode.label));
                            method.instructions.insert(jumpInsnNode, list);
                        }

                        if (node.opcode === Opcodes.INVOKEVIRTUAL && node.owner === "net/minecraft/world/server/ServerWorld" && node.name === "updateAllPlayersSleepingFlag") {
                            ASM.log("INFO", "Injecting wake time update");
                            list = new InsnList();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/su5ed/somnia/common/util/ASMHooks", "updateWakeTime", "(Lnet/minecraft/entity/player/PlayerEntity;)V", false));
                            method.instructions.insert(node, list);
                        }
                    }
                }

                return method;
            }
        }
    }
}
