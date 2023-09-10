package org.example;

import java.time.LocalDate;

public class SellPosition extends Position {
    public SellPosition(LocalDate date, double close, double stop) {
        super(TradeActions.SELL, date, close, stop);
    }

    @Override
    public Position updateStopLoss(double stop) {
        if (this.stop > stop) {
            return new SellPosition(this.date, this.price, stop);
        }
        return this;
    }
}
