package org.example;

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
