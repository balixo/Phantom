package org.example;

import java.time.LocalDate;

record DailyPrice(double open, double high, double low, double close, LocalDate date) {
}
