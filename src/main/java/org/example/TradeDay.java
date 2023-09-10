package org.example;

import java.time.LocalDate;

public record TradeDay(LocalDate date, double closePrice) {
}
