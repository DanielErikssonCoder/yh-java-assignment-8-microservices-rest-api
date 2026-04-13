package com.danielerikssoncoder.cinema_project.service;

import com.aspman.currency.CurrencyConverter;
import com.aspman.currency.Currency;
import org.springframework.stereotype.Service;

/**
 * Currency conversion from SEK to USD.
 * <p>
 * The team's shared library (com.aspman:gruppe-shared-lib).
 */
@Service
public class CurrencyService {

    /**
     * Converts a SEK amount to USD, rounded to 2 decimal places.
     * <p>
     * Math.round(x * 100.0) / 100.0 is a standard trick for rounding doubles
     * to 2 decimals without BigDecimal. Precision is sufficient for price display.
     *
     * @param amountSek  Amount in Swedish kronor
     * @return           Equivalent amount in USD, rounded to 2 decimals
     */
    public double convertSekToUsd(double amountSek) {
        return Math.round(CurrencyConverter.convert(amountSek, Currency.USD) * 100.0) / 100.0;
    }
}