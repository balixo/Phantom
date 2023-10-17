package org.example;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoField.*;

public class BackTestBankNifty {


    public static void main(String[] args) throws URISyntaxException, IOException {
        var prices = InvestingDotComHistoricPriceReader.parse(
                Paths.get(BackTestBankNifty.class.getClassLoader().getResource("bank-nifty-futures-2017-2023.csv").toURI()));

        InstrumentType instrumentType = InstrumentType.EQUITY;
        Position position = Position.LONG;
        double stopLose = 18034;
        double tradePrice = 18266;
        double top1 = 18226;
        double top2 = 18072;
        double low = Double.NaN;
        boolean isAdjusted = false;
        LocalDate tradeDate = LocalDate.of(2016, Month.DECEMBER, 29);
        System.out.println("Trade Date, Position, Entry, exit, P/N");

        for (int i = 2; i < prices.length; i++) {
            double dayBeforeYesterdayClose = prices[i - 2].close();
            double yesterdaysClose = prices[i - 1].close();
            double todayClose = prices[i].close();

            LocalDate today = prices[i].date();

            if (!isAdjusted) {
                stopLose = adjustStopLose(position, stopLose, tradePrice, todayClose);
                isAdjusted = true;
            }
            //up trend
            if (yesterdaysClose < dayBeforeYesterdayClose && yesterdaysClose < todayClose) {
                low = yesterdaysClose;
            } else if (yesterdaysClose > dayBeforeYesterdayClose && yesterdaysClose > todayClose) {
                if (Double.isNaN(top1)) {
                    top1 = yesterdaysClose;
                } else {
                    top2 = yesterdaysClose;
                }
            }
            //double top
            if (top2 > top1) {
                if (Position.LONG == position) {
                    stopLose = low;
                }
            }
            //double bottom
            else if (top2 < top1) {
                if (Position.SHORT == position) {
                    stopLose = top2;
                }
            }
            //
            //Check stop loose
            if (Position.LONG == position && stopLose >= todayClose) {
//                System.out.println(MessageFormat.format("{0} Stop lose hit at {1}, closing long position", today, stopLose));
                System.out.println(MessageFormat.format("{0}, {1}, {2,number,#}, {3,number,#}, {4,number,#}", tradeDate, Position.LONG, tradePrice, stopLose, stopLose - tradePrice));
//                System.out.println(MessageFormat.format("{0} Will enter Short position at at {1}, with stop loss top2 {2}", today, stopLose, top2));

                tradeDate = today;
                tradePrice = stopLose;
                position = Position.SHORT;
                stopLose = getSpotLose(instrumentType, position, top2, tradePrice); //top2
                top1 = top2;
                top2 = Double.NaN;
                isAdjusted = false;

            } else if (Position.SHORT == position && stopLose <= todayClose) {
//                System.out.println(MessageFormat.format("{0} Stop lose hit at {1}, closing Short position", today, stopLose));
                System.out.println(MessageFormat.format("{0}, {1}, {2,number,#}, {3,number,#}, {4,number,#}", tradeDate, Position.SHORT, tradePrice, stopLose, tradePrice - stopLose));
//                System.out.println(MessageFormat.format("{0} Will enter Long position at at {1}, with stop loss top2 {2}", today, stopLose, low));

                tradeDate = today;
                tradePrice = stopLose;
                position = Position.LONG;
                stopLose = getSpotLose(instrumentType, position, low, tradePrice); //low
                top1 = top2;
                top2 = Double.NaN;
                isAdjusted = false;
            }
        }
    }

    private static double adjustStopLose(Position position, double stopLose, double tradePrice, double eodPrice) {
        if (stopLose != tradePrice) {
            if (Position.SHORT == position && eodPrice <= tradePrice) {
//                System.out.println(MessageFormat.format("Stop lose will be adjusted from {0} to new stop lose : {1}", stopLose, tradePrice));
                return tradePrice;
            }
            if (Position.LONG == position && eodPrice >= tradePrice) {
//                System.out.println(MessageFormat.format("Stop lose will be adjusted from {0} to new stop lose : {1}", stopLose, tradePrice));
                return tradePrice;

            }
        }
        return stopLose;
    }

    static double getSpotLose(InstrumentConfiguration configuration, Position position, double proposedStopLose, double tradePrice) {
        double possibleStopLose = tradePrice * configuration.stopLosePercentage();
        if (Position.SHORT == position) {
//            return Math.min(proposedStopLose, tradePrice + possibleStopLose);
            return proposedStopLose;
        } else {
//            return Math.max(proposedStopLose, tradePrice - possibleStopLose);
            return proposedStopLose;
        }
    }

    interface InstrumentConfiguration {
        double stopLosePercentage();
    }

    enum InstrumentType implements InstrumentConfiguration {
        EQUITY(0.03), INDEX(0.02);
        private final double maxStopLose;

        InstrumentType(double maxStopLose) {
            this.maxStopLose = maxStopLose;
        }

        public double stopLosePercentage() {
            return maxStopLose;
        }


    }

    enum Position {
        LONG {
            @Override
            public String toString() {
                return "BUY";
            }
        }, SHORT {
            @Override
            public String toString() {
                return "SELL";
            }
        }


    }


    static class InvestingDotComHistoricPriceReader {

        public static final DateTimeFormatter LOCAL_DATE;
        public static final NumberFormat NUMBER_FORMAT;

        public static final int DATE = 0;
        public static final int CLOSE = 1;
        public static final int OPEN = 2;
        public static final int HIGH = 3;
        public static final int LOW = 4;

        static {
            LOCAL_DATE = new DateTimeFormatterBuilder().appendValue(MONTH_OF_YEAR, 2).appendLiteral('/').appendValue(DAY_OF_MONTH, 2).appendLiteral('/').appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD).toFormatter();
            NUMBER_FORMAT = NumberFormat.getNumberInstance(java.util.Locale.US);
        }

        public static DailyPrice[] parse(Path path) throws IOException {
            try (Stream<String> stream = Files.lines(path)) {
                List<DailyPrice> priceList = stream.skip(1).map(InvestingDotComHistoricPriceReader::parse).collect(Collectors.toList());
                Collections.reverse(priceList);
                return priceList.toArray(DailyPrice[]::new);
            }
        }

        private static DailyPrice parse(String recordLine) {
            String[] cols = recordLine.split("\",\"");

            LocalDate dt = LocalDate.parse(cols[DATE].replaceAll("\"", ""), LOCAL_DATE);
            try {
                double close = NUMBER_FORMAT.parse(cols[CLOSE].replaceAll("\"", "")).doubleValue();
                double open = NUMBER_FORMAT.parse(cols[OPEN].replaceAll("\"", "")).doubleValue();
                double high = NUMBER_FORMAT.parse(cols[HIGH].replaceAll("\"", "")).doubleValue();
                double low = NUMBER_FORMAT.parse(cols[LOW].replaceAll("\"", "")).doubleValue();
                return new DailyPrice(open, high, low, close, dt);
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    record DailyPrice(double open, double high, double low, double close, LocalDate date) {
    }

}