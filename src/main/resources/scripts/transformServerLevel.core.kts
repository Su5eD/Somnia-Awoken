import codes.som.koffee.insns.jvm.*
import codes.som.koffee.types.int
import codes.som.koffee.types.void
import wtf.gofancy.koremods.dsl.invokestatic

transformers {
    method(
        "net.minecraft.server.level.ServerLevel",
        "m_8714_", // tickChunk
        constructMethodDescriptor(int, "net/minecraft/world/level/chunk/LevelChunk"),
        ::transformTickChunk
    )
    
    method(
        "net.minecraft.server.level.ServerChunkCache",
        "m_8490_", // tickChunks
        constructMethodDescriptor(void),
        ::transformTickChunks
    )
}

fun transformTickChunk(method: MethodNode) {
    val target = method.findTarget {
        invokevirtual("net/minecraft/server/level/ServerLevel", mapMethodName("m_46469_"), returnType = "net/minecraft/world/level/GameRules") // getGameRules
        getstatic("net/minecraft/world/level/GameRules", mapFieldName("f_46134_"), "net/minecraft/world/level/GameRules\$Key") // RULE_DOMOBSPAWNING
        invokevirtual("net/minecraft/world/level/GameRules", mapMethodName("m_46207_"), boolean, "net/minecraft/world/level/GameRules\$Key") // getBoolean
    }
    
    target.insertBefore {
        goto(L["skip"])
    }
    target.insertAfter {
        +L["skip"]
        invokestatic("dev/su5ed/somnia/util/InjectHooks", "doMobSpawning", boolean, "net/minecraft/server/level/ServerLevel")
    }
}

fun transformTickChunks(method: MethodNode) {
    val target = method.findTarget {
        aload_0
        getfield("net/minecraft/server/level/ServerChunkCache", mapFieldName("f_8329_"), "net/minecraft/server/level/ServerLevel") // level
        invokevirtual("net/minecraft/server/level/ServerLevel", mapMethodName("m_46469_"), returnType = "net/minecraft/world/level/GameRules") // getGameRules
        getstatic("net/minecraft/world/level/GameRules", mapFieldName("f_46134_"), "net/minecraft/world/level/GameRules\$Key") // RULE_DOMOBSPAWNING
        invokevirtual("net/minecraft/world/level/GameRules", mapMethodName("m_46207_"), boolean, "net/minecraft/world/level/GameRules\$Key") // getBoolean
    }
    
    target.insert {
        goto(L["skip"])
    }
    target.insertAfter { 
        +L["skip"]
        getfield("net/minecraft/server/level/ServerChunkCache", mapFieldName("f_8329_"), "net/minecraft/server/level/ServerLevel") // level
        invokestatic("dev/su5ed/somnia/util/InjectHooks", "doMobSpawning", boolean, "net/minecraft/server/level/ServerLevel")
    }
}