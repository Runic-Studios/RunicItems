package com.runicrealms.runicitems.item.stats;

public class RunicItemStatRange {

    private final Integer min;
    private final Integer max;

    public RunicItemStatRange(Integer one, Integer two) {
        this.min = Math.min(one, two);
        this.max = Math.max(one, two);
    }

    public int getRandomValue() {
        if (this.min.equals(this.max)) {
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
