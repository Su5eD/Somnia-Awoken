import codes.som.koffee.insns.jvm.*
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode

transformers {
    method(
        "net.minecraft.server.level.ServerPlayer",
        mapMethodName("m_7720_"), // startSleepInBed
        constructMethodDescriptor("com/mojang/datafixers/util/Either", "net/minecraft/core/BlockPos"),
        ::transformStartSleepInBed
    )
}

fun transformStartSleepInBed(method: MethodNode) {
    val monstersCheck = method.findTarget {
        invokeinterface("java/util/List", "isEmpty", boolean)
        instructions.add(InsnNode(IFNE))
        getstatic("net/minecraft/world/entity/player/Player\$BedSleepingProblem", "NOT_SAFE", "net/minecraft/world/entity/player/Player\$BedSleepingProblem")
        invokestatic("com/mojang/datafixers/util/Either", "left", "com/mojang/datafixers/util/Either", "java/lang/Object")
        areturn
    }
    
    monstersCheck.insert(1) {
        logger.info("Overriding monsters check")
        
        getstatic("dev/su5ed/somnia/core/SomniaConfig", "COMMON", "dev/su5ed/somnia/core/SomniaConfig\$CommonConfig")
        getfield("dev/su5ed/somnia/core/SomniaConfig\$CommonConfig", "ignoreMonsters", "net/minecraftforge/common/ForgeConfigSpec\$BooleanValue")
        invokevirtual("net/minecraftforge/common/ForgeConfigSpec\$BooleanValue", "get", returnType = "java/lang/Object")
        checkcast("java/lang/Boolean")
        invokevirtual("java/lang/Boolean", "booleanValue", boolean)
        ifne((target as JumpInsnNode).label)
                            
        aload_0
        invokevirtual("net/minecraft/world/entity/player/Player", mapMethodName("m_7500_"), boolean) // isCreative
        ifne((target as JumpInsnNode).label)
    }
}