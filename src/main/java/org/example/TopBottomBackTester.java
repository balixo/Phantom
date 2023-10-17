package org.example;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.Month;

class TopBottomBackTester implements BackTester {
    private final DailyPrice[] historicPrice;
    private final TradeRepository tradeRepo;

    public TopBottomBackTester(DailyPrice[] historicPrice, TradeRepository tradeRepo) {
        this.historicPrice = historicPrice;
        this.tradeRepo = tradeRepo;
    }

    @Override
    public void test() {
        InstrumentType instrumentType = InstrumentType.EQUITY;
        Position position = Position.LONG;
        double stopLose = 18034;
        double tradePrice = 18266;
        double top1 = 18226;
        double top2 = 18072;
        double low = Double.NaN;
        boolean isAdjusted = false;
        LocalDate tradeDate = LocalDate.of(2016, Month.DECEMBER, 29);

        for (int i = 2; i < historicPrice.length; i++) {
            double dayBeforeYesterdayClose = historicPrice[i - 2].close();
            double yesterdaysClose = historicPrice[i - 1].close();
            double todayClose = historicPrice[i].close();

            LocalDate today = historicPrice[i].date();

            if (!isAdjusted) {
                var previousStop = stopLose;
                stopLose = adjustStopLose(position, stopLose, tradePrice, todayClose);
                isAdjusted = true;

                if (previousStop != stopLose) {
                    System.out.println(MessageFormat.format("{0} adjusting top lose for in the money price from {1} to {2}",
                            today, previousStop, stopLose));
                }

            }
            //up trend
            if (yesterdaysClose < dayBeforeYesterdayClose && yesterdaysClose < todayClose) {
                low = yesterdaysClose;
            } else if (yesterdaysClose > dayBeforeYesterdayClose && yesterdaysClose > todayClose) {
                if (Double.isNaN(top1)) {
                    top1 = yesterdaysClose;
                } else {
                    System.out.println(MessageFormat.format("{0} updating top from {1} to {2}, previous top1 at {3} ", today, top2, yesterdaysClose, top1));
                    top2 = yesterdaysClose;
                }
            }
            //double top
            if (top2 > top1) {
                if (Position.LONG == position) {
                    System.out.println(MessageFormat.format("{0} moving stop lose on {1} position from : {2} to {3} (Top2 : {4} > Top1 :{5})",
                            today, position, stopLose, low, top2, top1));
                    stopLose = low;
                }
            }
            //double bottom
            else if (top2 < top1) {
                if (Position.SHORT == position) {
                    System.out.println(MessageFormat.format("{0} moving stop lose on {1} position from : {2} to {3} (Top2 : {3} < Top1 :{4})",
                            today, position, stopLose, top2, top1));
                    stopLose = top2;
                }
            }
            //
            //Check stop loose
            if (Position.LONG == position && stopLose >= todayClose) {

                System.out.println(MessageFormat.format("{0} closing position {1} entry {2} exit {3}"
                        , tradeDate, position, tradePrice, stopLose));

                tradeRepo.insert(tradeDate, Position.LONG, tradePrice, stopLose);
                tradeDate = today;
                tradePrice = stopLose;
                position = Position.SHORT;
                stopLose = getSpotLose(instrumentType, position, top2, tradePrice); //top2
                top1 = top2;
                top2 = Double.NaN;
                isAdjusted = false;
                System.out.println(MessageFormat.format("{0} entered into new position {1} entry {2} exit {3}"
                        , today, Position.SHORT, tradePrice, stopLose));

            } else if (Position.SHORT == position && stopLose <= todayClose) {
                System.out.println(MessageFormat.format("{0} closing position {1} entry {2} exit {3}"
                        , tradeDate, position, tradePrice, stopLose));
                tradeRepo.insert(tradeDate, Position.SHORT, tradePrice, stopLose);
                tradeDate = today;
                tradePrice = stopLose;
                position = Position.LONG;
                stopLose = getSpotLose(instrumentType, position, low, tradePrice); //low
                top1 = top2;
                top2 = Double.NaN;
                isAdjusted = false;
                System.out.println(MessageFormat.format("{0} entered into new position {1} position entry {2} exit {3}"
                        , today, position, tradePrice, stopLose));
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

    private static double getSpotLose(InstrumentConfiguration configuration, Position position, double proposedStopLose, double tradePrice) {
        double possibleStopLose = tradePrice * configuration.stopLosePercentage();
        if (Position.SHORT == position) {
//            return Math.min(proposedStopLose, tradePrice + possibleStopLose);
            return proposedStopLose;
        } else {
//            return Math.max(proposedStopLose, tradePrice - possibleStopLose);
            return proposedStopLose;
        }
    }
}
