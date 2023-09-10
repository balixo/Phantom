package org.example;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Position {
    protected final TradeActions position;
    protected final LocalDate date;
    protected final double price;
    protected final double stop;

    public Position(TradeActions position, LocalDate date, double price, double stop) {
        this.position = position;
        this.date = date;
        this.price = price;
        this.stop = stop;
    }

    public TradeActions getPosition() {
        return position;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position position1)) return false;
        return Double.compare(getPrice(), position1.getPrice()) == 0
                && Double.compare(getStopLoss(), position1.getStopLoss()) == 0
                && getPosition() == position1.getPosition()
                && Objects.equals(getDate(), position1.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition(), getDate(), getPrice(), getStopLoss());
    }

    @Override
    public String toString() {
        return "Position{" +
                "position=" + position +
                ", date=" + date +
                ", price=" + price +
                ", stop=" + stop +
                '}';
    }

    public abstract Position updateStopLoss(double stop);

    public double getStopLoss() {
        return stop;
    }
}
