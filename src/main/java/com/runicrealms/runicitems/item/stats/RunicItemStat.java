package com.runicrealms.runicitems.item.stats;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RunicItemStat {

    private final RunicItemStatRange range;
    private final double rollPercentage;
    private final int value;

    public RunicItemStat(RunicItemStatRange range, double rollPercentage) {
        this.range = range;
        this.rollPercentage = rollPercentage;
        this.value = ThreadLocalRandom.current().nextInt(range.getMin(), range.getMax() + 1);
        // this(range, rollPercentage, (int) (range.getMin() + (rollPercentage * (range.getMax() - range.getMin()))));
    }

//    public RunicItemStat(RunicItemStatRange range, double rollPercentage, int roll) {
//        this.range = range;
//        this.rollPercentage = rollPercentage;
//        this.value = roll;
//    }

    public RunicItemStat(RunicItemStatRange range) {
        this(range, new Random().nextFloat());
    }

    public RunicItemStatRange getRange() {
        return this.range;
    }

    public double getRollPercentage() {
        return this.rollPercentage;
    }

    public int getValue() {
        return this.value;
    }

}
