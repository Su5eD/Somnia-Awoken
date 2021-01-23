function initializeCoreMod() {
    return {
        'transformServerPlayerEntity': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.MinecraftServer',
                'methodName': 'func_71217_p', // tick
                'methodDesc': '(Ljava/util/function/BooleanSupplier;)V'
            },
            'transformer': function (method) {
                var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");

                for (var i = 0; i < method.instructions.size(); i++) {
                    var instruction = method.instructions.get(i);
                    if (instruction instanceof InsnNode && instruction.getOpcode() === Opcodes.RETURN) {
                        method.instructions.insertBefore(instruction, ASM.buildMethodCall(
                            "mods/su5ed/somnia/util/ASMHooks",
                            "tick",
                            "()V",
                            ASM.MethodType.STATIC
                        ));
                        break;
                    }
                }

                return method;
            }
        }
    }
}
