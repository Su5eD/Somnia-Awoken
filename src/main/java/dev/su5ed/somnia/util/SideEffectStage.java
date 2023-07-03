package dev.su5ed.somnia.util;

import dev.su5ed.somnia.SomniaConfig;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public record SideEffectStage(int minFatigue, int maxFatigue, ResourceLocation effect, int duration, int amplifier) {
    private static SideEffectStage[] stages;

    public static SideEffectStage[] getSideEffectStages() {
        if (stages == null) {
            List<? extends List<Object>> sideEffectStages = SomniaConfig.COMMON.sideEffectStages.get();
            stages = new SideEffectStage[sideEffectStages.size()];
            for (int i = 0; i < stages.length; i++) {
                stages[i] = parseStage(sideEffectStages.get(i));
            }
        }

        return stages;
    }

    public static String getSideEffectStageDescription(double fatigue) {
        int stage = getForFatigue(fatigue);
        float ratio = SomniaConfig.COMMON.sideEffectStages.get().size() / 4F;
        int desc = Math.round(stage / ratio);
        return I18n.get("somnia.side_effect." + desc);
    }

    private static int getForFatigue(double fatigue) {
        List<? extends List<Object>> sideEffectStages = SomniaConfig.COMMON.sideEffectStages.get();
        for (int i = 0; i < sideEffectStages.size(); i++) {
            SideEffectStage stage = SideEffectStage.getSideEffectStages()[i];
            if (fatigue >= stage.minFatigue && fatigue <= stage.maxFatigue && (stage.duration >= 0 || i == sideEffectStages.size() - 1)) {
                return i + 1;
            }
        }
        return 0;
    }

    private static SideEffectStage parseStage(List<Object> stage) {
        return new SideEffectStage((int) stage.get(0), (int) stage.get(1), new ResourceLocation((String) stage.get(2)), (int) stage.get(3), (int) stage.get(4));
    }

    public MobEffect getEffect() {
        return ForgeRegistries.MOB_EFFECTS.getValue(this.effect);
    }
}
