package org.example;

public enum InstrumentType implements InstrumentConfiguration {
    EQUITY(0.03), INDEX(0.02);
    private final double maxStopLose;

    InstrumentType(double maxStopLose) {
        this.maxStopLose = maxStopLose;
    }

    public double stopLosePercentage() {
        return maxStopLose;
    }
}
