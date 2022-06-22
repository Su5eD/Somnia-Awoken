import codes.som.koffee.insns.jvm.*
import codes.som.koffee.types.float
import codes.som.koffee.types.long
import codes.som.koffee.types.void

transformers {
    method(
        "net.minecraft.client.renderer.GameRenderer",
        mapMethodName("m_109089_"), // renderLevel
        constructMethodDescriptor(void, float, long, "com/mojang/blaze3d/vertex/PoseStack"),
        ::transformRenderLevel
    )
}

fun transformRenderLevel(method: MethodNode) {
    method.insert { 
        fload_1
        lload_2
        aload(4)
        invokestatic(
            "dev/su5ed/somnia/util/ClientInjectHooks",
            "skipRenderWorld",
            constructMethodDescriptor(boolean, float, long, "com/mojang/blaze3d/vertex/PoseStack")
        )
        ifeq(L["continue"])
        `return`
        +L["continue"]
    }
}