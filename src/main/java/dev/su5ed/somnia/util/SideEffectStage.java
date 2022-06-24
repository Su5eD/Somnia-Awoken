package dev.su5ed.somnia.util;

import dev.su5ed.somnia.SomniaConfig;
import net.minecraft.client.resources.language.I18n;

import java.util.List;

public record SideEffectStage(int minFatigue, int maxFatigue, int potionID, int duration, int amplifier) {
    private static SideEffectStage[] stages;

    public static SideEffectStage[] getSideEffectStages() {
        if (stages == null) {
            List<? extends List<Integer>> sideEffectStages = SomniaConfig.COMMON.sideEffectStages.get();
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
        List<? extends List<Integer>> sideEffectStages = SomniaConfig.COMMON.sideEffectStages.get();
        for (int i = 0; i < sideEffectStages.size(); i++) {
            SideEffectStage stage = SideEffectStage.getSideEffectStages()[i];
            if (fatigue >= stage.minFatigue && fatigue <= stage.maxFatigue && (stage.duration >= 0 || i == sideEffectStages.size() - 1)) {
                return i + 1;
            }
        }
        return 0;
    }

    private static SideEffectStage parseStage(List<Integer> stage) {
        return new SideEffectStage(stage.get(0), stage.get(1), stage.get(2), stage.get(3), stage.get(4));
    }
}
