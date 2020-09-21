package com.runicrealms.runicitems.item.stats;

public class RunicItemStatRange {

    private Integer min;
    private Integer max;

    public RunicItemStatRange(Integer one, Integer two) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public int getRandomValue() {
        if (this.min == this.max) {
            return this.min;
        }
        return (int) (Math.floor(Math.random() * (this.max - this.min)) + this.min);
    }

    public Integer getMin() {
        return this.min;
    }

    public Integer getMax() {
        return this.max;
    }

}
