package dev.su5ed.somnia.util;

import dev.su5ed.somnia.Somnia;
import dev.su5ed.somnia.SomniaConfig;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public record SideEffectStage(int minFatigue, int maxFatigue, ResourceLocation effect, int duration, int amplifier) {
    private static SideEffectStage[] stages;

    public static SideEffectStage[] getSideEffectStages() {
        if (stages == null) {
            List<? extends List<Object>> sideEffectStages = SomniaConfig.COMMON.sideEffectStages.get();
            List<? extends List<Object>> migrated = migrateStages(sideEffectStages);
            if (!sideEffectStages.equals(migrated)) {
                sideEffectStages = migrated;
                SomniaConfig.COMMON.sideEffectStages.set(sideEffectStages);
                SomniaConfig.COMMON.sideEffectStages.save();
            }
            
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
    
    private static List<? extends List<Object>> migrateStages(List<? extends List<Object>> stages) {
        List<List<Object>> migratedStages = new ArrayList<>();
        for (List<Object> stage : stages) {
            List<Object> migratedStage = new ArrayList<>();
            for (int i = 0; i < stage.size(); i++) {
                Object obj = stage.get(i);
                if (i == 2 && obj instanceof Integer) {
                    MobEffect value = Registry.MOB_EFFECT.byId((int) obj);
                    ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(value);
                    if (key == null) {
                        Somnia.LOGGER.error("Potion with ID " + obj + " not found. Please migrate sideEffectStages manually or reset it.");
                        throw new RuntimeException("Error migrating sideEffectStages config effect IDs");
                    }
                    obj = key.toString();
                }
                migratedStage.add(obj);
            }
            migratedStages.add(migratedStage);
        }
        return migratedStages;
    }
    
    public MobEffect getEffect() {
        return ForgeRegistries.MOB_EFFECTS.getValue(this.effect);
    }
}
