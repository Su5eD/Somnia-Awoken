import codes.som.koffee.insns.jvm.*
import codes.som.koffee.types.int
import codes.som.koffee.types.void
import wtf.gofancy.koremods.dsl.invokestatic

transformers {
    method(
        "net.minecraft.server.level.ServerLevel",
        mapMethodName("m_8714_"), // tickChunk
        constructMethodDescriptor(int, "net/minecraft/world/level/chunk/LevelChunk"),
        ::transformTickChunk
    )
    
    method(
        "net.minecraft.server.level.ServerChunkCache",
        mapMethodName("m_8490_"), // tickChunks
        constructMethodDescriptor(void),
        ::transformTickChunks
    )
}

fun transformTickChunk(method: MethodNode) {
    val target = method.findTarget {
        invokevirtual("net/minecraft/server/level/ServerLevel", "getGameRules", returnType = "net/minecraft/world/level/GameRules")
        getstatic("net/minecraft/world/level/GameRules", "RULE_DOMOBSPAWNING", "net/minecraft/world/level/GameRules\$Key")
        invokevirtual("net/minecraft/world/level/GameRules", "getBoolean", boolean, "net/minecraft/world/level/GameRules\$Key")
    }
    
    target.insertBefore {
        goto(L["skip"])
    }
    target.insertAfter {
        +L["skip"]
        invokestatic("dev/su5ed/somnia/util/ASMHooks", "doMobSpawning", boolean, "net/minecraft/server/level/ServerLevel")
    }
}

fun transformTickChunks(method: MethodNode) {
    val target = method.findTarget {
        aload_0
        getfield("net/minecraft/server/level/ServerChunkCache", "level", "net/minecraft/server/level/ServerLevel")
        invokevirtual("net/minecraft/server/level/ServerLevel", "getGameRules", returnType = "net/minecraft/world/level/GameRules")
        getstatic("net/minecraft/world/level/GameRules", "RULE_DOMOBSPAWNING", "net/minecraft/world/level/GameRules\$Key")
        invokevirtual("net/minecraft/world/level/GameRules", "getBoolean", boolean, "net/minecraft/world/level/GameRules\$Key")
    }
    
    target.insert {
        goto(L["skip"])
    }
    target.insertAfter { 
        +L["skip"]
        getfield("net/minecraft/server/level/ServerChunkCache", "level", "net/minecraft/server/level/ServerLevel")
        invokestatic("dev/su5ed/somnia/util/ASMHooks", "doMobSpawning", boolean, "net/minecraft/server/level/ServerLevel")
    }
}