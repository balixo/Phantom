package org.example;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class BackTestBankNifty {

    public static void main(String[] args) throws URISyntaxException, IOException {
        TradeRepository tradeRepository = new TradeRepository();
        CsvPrinter csvPrinter = new CsvPrinter(tradeRepository);

        var prices = InvestingDotComHistoricPriceReader.parse(
                Paths.get(BackTestBankNifty.class.getClassLoader().getResource("bank-nifty-futures-2017-2023.csv").toURI()));

        BackTester topDownBackTester = new TopBottomBackTester(prices, tradeRepository);
        topDownBackTester.test();

        System.out.println(csvPrinter);
    }
}