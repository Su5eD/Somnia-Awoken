function initializeCoreMod() {
    return {
        'transformServerWorld': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerWorld',
                'methodName': 'func_217441_a', // tickEnvironment
                'methodDesc': '(Lnet/minecraft/world/chunk/Chunk;I)V'
            },
            'transformer': function(method) {
                var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

                print("hi every22 im new!")
                for(var h = 0; h < 3; h++) {
                    method.instructions.remove(method.instructions.get(68));
                }

                method.instructions.insert(method.instructions.get(67), new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/su5ed/somnia/Somnia", "doMobSpawning", "(Lnet/minecraft/world/server/ServerWorld;)Z", false));
                return method;
            }
        },
        'transformServerChunkProvider': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerChunkProvider',
                'methodName': 'func_217220_m', // tickChunks
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');

                print("hi every33 im new!")
                for(var h = 0; h < 4; h++) {
                    method.instructions.remove(method.instructions.get(33));
                }
                var insnList = new InsnList();
                insnList.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/server/ServerChunkProvider", "world", "Lnet/minecraft/world/server/ServerWorld;"))
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/su5ed/somnia/Somnia", "doMobSpawning", "(Lnet/minecraft/world/server/ServerWorld;)Z", false));

                method.instructions.insert(method.instructions.get(32), insnList);

                /*for(var i = 0; i < method.instructions.size(); i++) {
                    printInsnNode(i, method.instructions.get(i));
                }*/
                return method;
            }
        }
    }
}

function printInsnNode(index, printTgt) {
    print(index + " " + printTgt+"|"+printTgt.opcode
        +"|"+printTgt.desc+"|"+printTgt.owner+"|"+printTgt.name+"|"+printTgt["var"]+"|"+printTgt.line)
}
