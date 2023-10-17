package org.example;

import java.text.MessageFormat;
import java.time.LocalDate;

class CsvPrinter {
    private final TradeRepository repository;

    public CsvPrinter(TradeRepository repository) {
        this.repository = repository;
    }

    @Override
    public String toString() {
        var trades = repository.getTrades();
        StringBuilder buffer = new StringBuilder();
        buffer.append("TRADE DATE, POSITION, ENTRY, EXIT, P/N\n");
        for (int i = 0; i < trades.size(); i++) {
            Trade trade = trades.get(i);
            LocalDate tradeDate = trade.tradeDate();
            Position position = trade.position();
            double entry = trade.entry();
            double exit = trade.exit();
            double pnl = trade.getPnL();
            buffer.append(MessageFormat.format("{0}, {1}, {2,number,###0.##}, {3,number,###0.##}, {4,number,###0.##}\n",
                    tradeDate, position, entry, exit, pnl));
        }

        return buffer.toString();
    }
}
