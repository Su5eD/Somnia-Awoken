package dev.su5ed.somnia.util;

import java.util.function.BiFunction;

public enum FatigueDisplayPosition {
    TOP_CENTER((scaledWidth, stringWidth) -> scaledWidth / 2 - stringWidth / 2, (scaledHeight, fontHeight) -> fontHeight),
    TOP_LEFT((scaledWidth, stringWidth) -> 10, (scaledHeight, fontHeight) -> fontHeight),
    TOP_RIGHT((scaledWidth, stringWidth) -> scaledWidth - stringWidth - 10, (scaledHeight, fontHeight) -> fontHeight),
    BOTTOM_CENTER((scaledWidth, stringWidth) -> scaledWidth / 2 - stringWidth / 2, (scaledHeight, fontHeight) -> scaledHeight - fontHeight - 45),
    BOTTOM_LEFT((scaledWidth, stringWidth) -> 10, (scaledHeight, fontHeight) -> scaledHeight - fontHeight - 10),
    BOTTOM_RIGHT((scaledWidth, stringWidth) -> scaledWidth - stringWidth - 10, (scaledHeight, fontHeight) -> scaledHeight - fontHeight - 10);

    private final BiFunction<Integer, Integer, Integer> x;
    private final BiFunction<Integer, Integer, Integer> y;

    FatigueDisplayPosition(BiFunction<Integer, Integer, Integer> x, BiFunction<Integer, Integer, Integer> y) {
        this.x = x;
        this.y = y;
    }

    public int getX(int scaledWidth, int stringWidth) {
        return this.x.apply(scaledWidth, stringWidth);
    }

    public int getY(int scaledHeight, int fontHeight) {
        return this.y.apply(scaledHeight, fontHeight);
    }
}
