package org.example;

import java.time.LocalDate;

record Trade(LocalDate tradeDate, Position position, double entry, double exit) {
    public double getPnL() {
        return Position.LONG == position ? exit - entry : entry - exit;
    }
}
