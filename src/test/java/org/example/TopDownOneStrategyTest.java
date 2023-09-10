package org.example;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TopDownOneStrategyTest {

    @Test
    void firstBuyPositionInUpTrend() {
        double[] close = new double[]{1d, 2d, 3d, 2d, 3d, 4d};
        TopDownOneStrategy topDownOneStrategy = new TopDownOneStrategy(buildHistoricDates(close));

        topDownOneStrategy.process();

        TopDownOneStrategy.TradingDay tradingDay = topDownOneStrategy.getLastTradingDay();

        Position processPosition = tradingDay.getPosition();

        assertEquals(TradeActions.BUY, processPosition.getPosition());
        assertEquals(LocalDate.now().minusDays(1), processPosition.getDate());
        assertEquals(processPosition.getPrice(), 3d);

        assertEquals(Trend.UP, tradingDay.getTrend());
        assertEquals(2d, processPosition.getStopLoss());
        assertEquals(4d, tradingDay.getPreviousHigh());
    }


    @Test
    void moveStopLoseOnBuyPosition() {
        double[] close = new double[]{1d, 2d, 3d, 2d, 3d, 4d, 3d, 4d, 5d, 6d};
        TopDownOneStrategy topDownOneStrategy = new TopDownOneStrategy(buildHistoricDates(close));

        topDownOneStrategy.process();

        TopDownOneStrategy.TradingDay tradingDay = topDownOneStrategy.getLastTradingDay();

        Position processPosition = tradingDay.getPosition();

        assertEquals(TradeActions.BUY, processPosition.getPosition());
        assertEquals(LocalDate.now().minusDays(5), processPosition.getDate());
        assertEquals(3d, processPosition.getPrice());

        assertEquals(Trend.UP, tradingDay.getTrend());
        assertEquals(3d, processPosition.getStopLoss());
        assertEquals(6d, tradingDay.getPreviousHigh());
    }

    @Test
    void moveFromBuyToSellPosition() {
        double[] close = new double[]{1d, 2d, 3d, 2d, 3d, 4d, 3d, 4d, 2d};
        TopDownOneStrategy topDownOneStrategy = new TopDownOneStrategy(buildHistoricDates(close));
        topDownOneStrategy.process();

        TopDownOneStrategy.TradingDay tradingDay = topDownOneStrategy.getLastTradingDay();

        Position processPosition = tradingDay.getPosition();

        assertEquals(TradeActions.SELL, processPosition.getPosition());
        assertEquals(LocalDate.now(), processPosition.getDate());
        assertEquals(3d, processPosition.getPrice());

        assertEquals(Trend.DOWN, tradingDay.getTrend());
        assertEquals(4d, processPosition.getStopLoss());
        assertEquals(4d, tradingDay.getPreviousHigh());

    }


    @Test
    void randomWalk() {
        double[] close = new double[]{1, 2, 3, 2, 3, 4, 3, 4, 5, 4, 3, 4, 3, 2, 3, 2, 1};
        TopDownOneStrategy topDownOneStrategy = new TopDownOneStrategy(buildHistoricDates(close));
        topDownOneStrategy.process();


    }

    private TradeDay[] buildHistoricDates(double[] close) {
        TradeDay[] historicData = new TradeDay[close.length];
        LocalDate today = LocalDate.now().minusDays(close.length - 1);

        for (int i = 0; i < close.length; i++) {
            historicData[i] = new TradeDay(today.plusDays(i), close[i]);
        }
        return historicData;
    }
}
