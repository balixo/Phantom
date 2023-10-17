package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoField.*;

class InvestingDotComHistoricPriceReader {

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
