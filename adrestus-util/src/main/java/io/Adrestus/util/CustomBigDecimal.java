package io.Adrestus.util;

import io.Adrestus.config.RewardConfiguration;

import java.math.BigDecimal;

public class CustomBigDecimal {


    public static BigDecimal valueOf(BigDecimal value) {
        value = value.setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING);
        return value;
    }

    public static BigDecimal valueOf(Double value) {
        return BigDecimal.valueOf(value).setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING);
    }

    public static BigDecimal valueOf(int value) {
        return BigDecimal.valueOf(value).setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING);
    }
}
