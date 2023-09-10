package org.example;

import java.time.LocalDate;

public class NoPosition extends Position {

    private static final Position INSTANCE = new NoPosition();

    private NoPosition() {
        super(TradeActions.NONE, LocalDate.now(), Double.NaN, Double.NaN);
    }

    public static Position GetInstance() {
        return INSTANCE;
    }

    @Override
    public Position updateStopLoss(double stop) {
        throw new IllegalStateException("No position to update stop loss");
    }
}
