//package org.example;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//import java.util.*;
//
//public class PriceMoveTracker {
//
//
////    void newHigh() {
////        Price[] historicPrice = of(1, 3, 2, 4, 1, 5);
////        PriceTracker priceTracker = new PriceTracker();
////        for (int i = 0; i < historicPrice.length; i++) {
////            priceTracker.track(historicPrice[i]);
////        }
////        var highs = priceTracker.getHighs();
////
////        Assertions.assertArrayEquals(new Double[]{1d, 3d, 3d, 3d, 4d, 4d}, highs.values().stream().toList().reversed().toArray(new Double[0]));
////    }
//
//    private Price[] of(double... price) {
//        double[] p = Arrays.stream(price).toArray();
//        Price[] returnPrices = new Price[p.length];
//        for (int i = 0; i < p.length; i++) {
//            returnPrices[i] = Price.of(LocalDate.now().minusDays(p.length - 1 - i), p[i]);
//        }
//
//        return returnPrices;
//    }
//
//    @Test
//    void newLow() {
//
//    }
//
//
//    public record Price(LocalDate date, double price) {
//        public static Price of(LocalDate date, double price) {
//            return new Price(date, price);
//        }
//
//        @Override
//        public String toString() {
//            return "Price{" +
//                    "date=" + date +
//                    ", price=" + price +
//                    '}';
//        }
//
//        @Override
//        public LocalDate date() {
//            return date;
//        }
//
//        @Override
//        public double price() {
//            return price;
//        }
//    }
//
//    public static class PriceTracker {
//
//        private final Map<LocalDate, Double> prices = new Hashtable<>();
//
//        public Map<LocalDate, Double> getHighs() {
//            return highs;
//        }
//
//        private final Map<LocalDate, Double> highs = new Hashtable<>();
//        private static final int NOT_DEFINED = -1;
//
//        public double getNewHigh() {
//            return newHigh;
//        }
//
//        /**
//         * changes when price moves down and turn back up
//         */
//        private double newHigh = NOT_DEFINED;
//
//        public void track(Price price) {
//            prices.putIfAbsent(price.date(), price.price());
//            double tp = price.price();
//            double yp = getYesterdaysPrice(price.date());
//            double dfyp = getDayBeforeYesterdaysPrice(price.date());
//
//            if (NOT_DEFINED == yp) {
//                newHigh = tp;
//            } else if (NOT_DEFINED == dfyp && tp > yp) {
//                newHigh = tp;
//            } else if (tp < yp && yp > dfyp) {
//                newHigh = yp;
//            }
//
//            highs.putIfAbsent(price.date(), newHigh);
//        }
//
//        private double getDayBeforeYesterdaysPrice(LocalDate date) {
//            return getYesterdaysPrice(getYesterday(date));
//        }
//
//        private double getYesterdaysPrice(LocalDate date) {
//            Double p = prices.get(getYesterday(date));
//            return null == p ? NOT_DEFINED : p;
//        }
//
//        private static LocalDate getYesterday(LocalDate date) {
//            return date.minusDays(1);
//        }
//    }
//}
