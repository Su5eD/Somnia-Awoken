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
                var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');

                print("hi every66 im new!")

                method.instructions.insertBefore(method.instructions.get(252), ASM.buildMethodCall(
                    "mods/su5ed/somnia/Somnia",
                    "tick",
                    "()V",
                    ASM.MethodType.STATIC
                ));

                return method;
            }
        }
    }
}

function printInsnNode(index, printTgt) {
    print(index + " " + printTgt+"|"+printTgt.opcode
        +"|"+printTgt.desc+"|"+printTgt.owner+"|"+printTgt.name+"|"+printTgt["var"]+"|"+printTgt.line)
}