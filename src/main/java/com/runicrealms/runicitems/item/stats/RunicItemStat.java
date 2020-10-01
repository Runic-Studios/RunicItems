package com.runicrealms.runicitems.item.stats;

import java.util.Random;

public class RunicItemStat {

    private RunicItemStatRange range;
    private float rawRoll;
    private int roll;

    public RunicItemStat(RunicItemStatRange range, float rawRoll) {
        this.range = range;
        this.rawRoll = rawRoll;
        this.roll = this.range.getMin() + (int) Math.floor(this.rawRoll * (this.range.getMax() - this.range.getMin()));
    }

    public RunicItemStat(RunicItemStatRange range, float rawRoll, int roll) {
        this.range = range;
        this.rawRoll = rawRoll;
        this.roll = roll;
    }

    public RunicItemStat(RunicItemStatRange range) {
        this.range = range;
        this.rawRoll = new Random().nextFloat();
    }

    public RunicItemStatRange getRange() {
        return this.range;
    }

    public float getRawRoll() {
        return this.rawRoll;
    }

    public int getRoll() {
        return this.roll;
    }

}
