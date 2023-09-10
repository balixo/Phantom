package org.example;

public class RestingOrder {
    private final Position position;
    private final double stopLoss;


    public RestingOrder(Position position, double stopLoss) {
        this.position = position;
        this.stopLoss = stopLoss;
    }


    public Position getPosition() {
        return position;
    }

    public double getStopLoss() {
        return stopLoss;
    }

    public RestingOrder UpdateStopLoss(double stopLoss) {
        return new RestingOrder(position, stopLoss);
    }
}
