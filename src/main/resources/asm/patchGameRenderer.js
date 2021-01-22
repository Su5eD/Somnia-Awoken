function initializeCoreMod() {
    return {
        'transformGameRenderer': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.GameRenderer',
                'methodName': 'func_195458_a', // updateCameraAndRender
                'methodDesc': '(FJZ)V'
            },
            'transformer': function(method) {
                var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

                ASM.log("DEBUG", "Patching GameRenderer#updateCameraAndRender");

                for (var i = 0; i < method.instructions.size(); i++) {
                    var insn = method.instructions.get(i);
                    if (insn instanceof MethodInsnNode
                        && insn.owner === "net/minecraft/client/renderer/GameRenderer"
                        && insn.name === "renderWorld"
                        && insn.desc === "(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V"
                        && insn.getOpcode() === Opcodes.INVOKEVIRTUAL) {
                        insn.setOpcode(Opcodes.INVOKESTATIC);
                        insn.name = "renderWorld";
                        insn.owner = "mods/su5ed/somnia/common/util/ASMHooks";

                        method.instructions.remove(method.instructions.get(i - 6));
                    }
                }

                return method;
            }
        }
    }
}