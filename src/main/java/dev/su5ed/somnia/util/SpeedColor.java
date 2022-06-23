package dev.su5ed.somnia.util;

import net.minecraft.ChatFormatting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum SpeedColor {
    WHITE(ChatFormatting.WHITE, 8),
    DARK_RED(ChatFormatting.DARK_RED, 20),
    RED(ChatFormatting.RED, 30),
    GOLD(ChatFormatting.GOLD, 100);

    private static final List<SpeedColor> VALUES = Arrays.stream(values())
        .sorted(Comparator.comparing(color -> color.range))
        .toList();

    public final ChatFormatting color;
    public final double range;

    SpeedColor(ChatFormatting color, double range) {
        this.color = color;
        this.range = range;
    }

    public static SpeedColor getColorForSpeed(double speed) {
        return VALUES.stream()
            .filter(color -> speed <= color.range)
            .findFirst()
            .orElse(SpeedColor.WHITE);
    }
}
