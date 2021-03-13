package mods.su5ed.somnia.util;

import mods.su5ed.somnia.config.SomniaConfig;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class SideEffectStage {
    private static SideEffectStage[] stages;

    public final int minFatigue;
    public final int maxFatigue;
    public final int potionID;
    public final int duration;
    public final int amplifier;

    public SideEffectStage(int minFatigue, int maxFatigue, int potionID, int duration, int amplifier) {
        this.minFatigue = minFatigue;
        this.maxFatigue = maxFatigue;
        this.potionID = potionID;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public static SideEffectStage[] getSideEffectStages() {
        if (stages == null) {
            stages = new SideEffectStage[SomniaConfig.sideEffectStages.size()];
            for (int i = 0; i < stages.length; i++) {
                stages[i] = parseStage(SomniaConfig.sideEffectStages.get(i));
            }
        }

        return stages;
    }

    private static SideEffectStage parseStage(List<Integer> stage) {
        return new SideEffectStage(stage.get(0), stage.get(1), stage.get(2), stage.get(3), stage.get(4));
    }

    public static String getSideEffectStageDescription(double fatigue) {
        int stage = getForFatigue(fatigue);
        float ratio = SomniaConfig.sideEffectStages.size() / 4F;
        int desc = Math.round(stage / ratio);
        return I18n.format("somnia.side_effect."+desc);
    }

    public static int getForFatigue(double fatigue) {
        for (int i = 0; i < SomniaConfig.sideEffectStages.size(); i++) {
            SideEffectStage stage = SideEffectStage.getSideEffectStages()[i];
            if (fatigue >= stage.minFatigue && fatigue <= stage.maxFatigue && (!(stage.duration < 0) || i == SomniaConfig.sideEffectStages.size() - 1)) return i + 1;
        }

        return 0;
    }
}
