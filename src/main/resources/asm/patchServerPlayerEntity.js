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
                var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');

                print("hi every55 im new!")

                for(var i=0; i<3; i++) method.instructions.remove(method.instructions.get(90));

                var insnList = new InsnList();
                insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, "mods/su5ed/somnia/setup/ServerProxy", "enterSleepPeriod", "Lmods/su5ed/somnia/common/util/TimePeriod;"));
                insnList.add(new LdcInsnNode(24000));
                insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "mods/su5ed/somnia/common/util/TimePeriod", "isTimeWithin", "(I)Z", false));

                method.instructions.insert(method.instructions.get(89), insnList);

                return method;
            }
        }
    }
}

function printInsnNode(index, printTgt) {
    print(index + " " + printTgt+"|"+printTgt.opcode
        +"|"+printTgt.desc+"|"+printTgt.owner+"|"+printTgt.name+"|"+printTgt["var"]+"|"+printTgt.line)
}