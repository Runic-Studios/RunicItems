package com.runicrealms.runicitems.item.stats;

import java.util.Random;

public class RunicItemStat {

    private final RunicItemStatRange range;
    private final float rollPercentage;
    private final int roll;

    public RunicItemStat(RunicItemStatRange range, float rollPercentage) {
        this(range, rollPercentage, range.getMin() + (int) Math.floor(rollPercentage * (range.getMax() - range.getMin())));
    }

    public RunicItemStat(RunicItemStatRange range, float rollPercentage, int roll) {
        this.range = range;
        this.rollPercentage = rollPercentage;
        this.roll = roll;
    }

    public RunicItemStat(RunicItemStatRange range) {
        this(range, new Random().nextFloat());
    }

    public RunicItemStatRange getRange() {
        return this.range;
    }

    public float getRollPercentage() {
        return this.rollPercentage;
    }

    public int getRoll() {
        return this.roll;
    }

}
