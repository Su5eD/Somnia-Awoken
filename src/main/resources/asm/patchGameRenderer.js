function initializeCoreMod() {
    return {
        'transformGameRenderer': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.GameRenderer',
                'methodName': 'func_228378_a_', // renderWorld
                'methodDesc': '(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V'
            },
            'transformer': function(method) {
                var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

                ASM.log("DEBUG", "Patching GameRenderer#renderWorld");

                var initialLabel = method.instructions.get(0);
                var list = new InsnList();
                list.add(new VarInsnNode(Opcodes.FLOAD, 1));
                list.add(new VarInsnNode(Opcodes.LLOAD, 2));
                list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/su5ed/somnia/util/ASMHooks", "skipRenderWorld", "(FJLcom/mojang/blaze3d/matrix/MatrixStack;)Z"));
                list.add(new JumpInsnNode(Opcodes.IFEQ, initialLabel));
                list.add(new InsnNode(Opcodes.RETURN));
                method.instructions.insertBefore(initialLabel, list);

                return method;
            }
        }
    }
}