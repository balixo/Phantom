package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

/**
 * Top bottom System rules
 * 1) Buy above the previous top
 * 2) Sell below the previous bottom
 * 3) Maximum Stop -either 2% (from indices) or 3% (for stocks)
 * 4) Once ITM by either 2% (for indices) or 3% (for stocks)- stop loss is break even
 * 5) if gap up/gap down by more than 2% from the system point - exit only (no new position)
 * 6) On the day of entry follow the stop of 1% (for indices) or 1.5% (for stocks)
 */
public class TopDownOneStrategy {

    private static final Logger LOGGER = LogManager.getLogger(TopDownOneStrategy.class);

    private final TradeDay[] close;


    private final PositionTracker positionTracker = new PositionTracker();

    public TopDownOneStrategy(TradeDay[] close) {
        this.close = close;
    }

    public void process() {
        for (int i = 0; i < close.length; i++) {
            positionTracker.update(close[i].date(), close[i].closePrice());
        }
    }

    public TradingDay getLastTradingDay() {
        return positionTracker.days.get(positionTracker.days.size() - 1);
    }

    public static class TradingDay {
        public Position getPosition() {
            return position;
        }

        public double getPreviousHigh() {
            return previousHigh;
        }

        public double getPreviousLow() {
            return previousLow;
        }

        public double getYesterdayPrice() {
            return yesterdayPrice;
        }

        public double getPrice() {
            return price;
        }

        private final Position position;
        private final double previousHigh;
        private final double previousLow;
        private final double yesterdayPrice;
        private final double price;

        private TradingDay(Position position, double previousHigh, double previousLow, double yesterdayPrice, double price) {
            this.position = position;
            this.previousHigh = previousHigh;
            this.previousLow = previousLow;
            this.yesterdayPrice = yesterdayPrice;
            this.price = price;
        }


        Trend getTrend() {
            if (price > yesterdayPrice) {
                return Trend.UP;
            }
            if (price < yesterdayPrice) {
                return Trend.DOWN;
            }
            return Trend.NONE;
        }

        TradingDay updatePrice(double price) {
            return new TradingDay(position, getPreviousHigh(), getPreviousLow(), this.price, price);
        }


        TradingDay updatePreviousHigh(double price) {
            return new TradingDay(position, Math.max(previousHigh, price), this.getPreviousLow(), yesterdayPrice, price);
        }

        TradingDay updatePreviousLow(double price) {
            return new TradingDay(position, previousHigh, Math.min(this.getPreviousLow(), price), yesterdayPrice, price);
        }

        TradingDay resetPreviousLow(double price) {
            return new TradingDay(position, previousHigh, yesterdayPrice, yesterdayPrice, price);
        }

        TradingDay buyWithStop(LocalDate date, double price) {
            if (position != null) {
                if (price >= position.getStopLoss()) {
                    return sellWithStop(date, price);
                } else {
                    return moveStopLoss();
                }
            } else if (Trend.UP == getTrend() && price >= previousHigh) {
                return new TradingDay(new BuyPosition(date, previousHigh, previousLow), previousHigh, previousLow, yesterdayPrice, price);
            }
            return this;
        }

        TradingDay sellWithStop(LocalDate date, double price) {
            if (position != null) {
                if (price <= position.getStopLoss()) {
                    return new TradingDay(new SellPosition(date, previousLow, previousHigh), previousHigh, previousLow, yesterdayPrice, price);
                } else {
                    return moveStopLoss();
                }
            } else if (Trend.DOWN == getTrend() && price < previousLow) {
                return new TradingDay(new SellPosition(date, previousLow, previousHigh), previousHigh, previousLow, yesterdayPrice, price);
            }
            return this;
        }

        static TradingDay createWithPrice(double price) {
            return new TradingDay(null, Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, price);
        }

        static TradingDay cloneFrom(TradingDay tradeDay) {
            if (tradeDay == null) {
                return createNew();
            }
            return new TradingDay(tradeDay.getPosition(), tradeDay.getPreviousHigh(), tradeDay.getPreviousLow(), tradeDay.getYesterdayPrice(), tradeDay.getPrice());
        }

        private static TradingDay createNew() {
            return new TradingDay(null, Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
        }

        public TradingDay moveStopLoss() {
            return new TradingDay(position.updateStopLoss(previousLow), previousHigh, previousLow, yesterdayPrice, price);
        }

        @Override
        public String toString() {
            return "TradingDay{" +
                    "position=" + position +
                    ", previousHigh=" + previousHigh +
                    ", previousLow=" + previousLow +
                    ", yesterdayPrice=" + yesterdayPrice +
                    ", price=" + price +
                    '}';
        }
    }

    private static class PositionTracker {
        List<TradingDay> days = new LinkedList<>();

        TradingDay yesterday = null;

        void update(LocalDate date, double price) {

            /*
            How to react to current price?

            buy
                - if new high is higher than previous high
            short sell
                - if new low is lower than previous low
            is it a new high?
                - what is high if position is
                long
                    move stop loss to previous low before turn
                short
                    move stop loss to previous high, before turn
                None
                    Buy on today's close and stop loss on previous low

            is it a new low?
                - what is low if position
                long
                    move stop loss to previous low before turn
                short
                    move stop loss to previous high before turn
                None
                    how on today's close and stop loss on previous high

            does it hit stop loss?
            does it move stop loss?

             */

            TradingDay today;

            if (!days.isEmpty()) {
                yesterday = days.get(days.size() - 1);
            } else {
                yesterday = TradingDay.createNew();
            }

            today = TradingDay.cloneFrom(yesterday).updatePrice(price);


            if (price > yesterday.price) { //change
                if (Trend.UP == yesterday.getTrend()) {
                    //continue the up-trend
                    today = today.updatePreviousHigh(price);
                } else if (Trend.DOWN == yesterday.getTrend()) {
                    //change in direction
                    today = today.resetPreviousLow(price);
                    today = today.buyWithStop(date, price);
                }
            } else if (price < yesterday.price) {
                if (Trend.DOWN == yesterday.getTrend()) {
                    //continue the down-trend
                    today = today.updatePreviousLow(price);
                } else if (Trend.UP == yesterday.getTrend()) {
                    //change in direction
                    if (today.position instanceof BuyPosition && price <= today.position.getStopLoss()) {
                        today = today.sellWithStop(date, price);
                        today = today.updatePreviousLow(price);
                    } else {
                        today = today.updatePreviousLow(price);
                        today = today.sellWithStop(date, price);
                    }
                }
            }
            LOGGER.warn("Price action for {}", today);
            days.add(today);
        }

    }
}