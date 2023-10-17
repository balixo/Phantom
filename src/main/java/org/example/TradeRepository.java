package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class TradeRepository {
    private final List<Trade> trades = new ArrayList<>();

    public void insert(LocalDate tradeDate, Position position, double entry, double exit) {
        trades.add(new Trade(tradeDate, position, entry, exit));
    }

    public List<Trade> getTrades() {
        return new ArrayList<>(trades);
    }
}
