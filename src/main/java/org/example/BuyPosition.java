package org.example;

import java.time.LocalDate;
import java.util.Objects;

public class BuyPosition extends Position {
    public BuyPosition(LocalDate date, double close, double stop) {
        super(TradeActions.BUY, date, close, stop);
    }


    @Override
    public Position updateStopLoss(double stop) {
        if (this.stop < stop) {
            return new BuyPosition(this.date, this.price, stop);
        }
        return this;
    }
}
