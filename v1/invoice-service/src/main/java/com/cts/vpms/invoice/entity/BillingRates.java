package com.cts.vpms.invoice.entity;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class BillingRates {

    public final BigDecimal TWO_WHEELER_BASE      = new BigDecimal("10.00");
    public final BigDecimal TWO_WHEELER_HOURLY    = new BigDecimal("5.00");
    public final BigDecimal TWO_WHEELER_DAILY_CAP = new BigDecimal("80.00");

    public final BigDecimal FOUR_WHEELER_BASE      = new BigDecimal("20.00");
    public final BigDecimal FOUR_WHEELER_HOURLY    = new BigDecimal("10.00");
    public final BigDecimal FOUR_WHEELER_DAILY_CAP = new BigDecimal("200.00");

}