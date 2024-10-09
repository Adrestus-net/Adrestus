package io.Adrestus.config;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RewardConfiguration {
    public static final int TRANSACTION_REWARD_PER_BLOCK = 7;
    public static final int BLOCK_REWARD_HEIGHT = 3;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    public static final int DECIMAL_PRECISION = 6;
    public static final int LEADER_BLOCK_REWARD_VALUE = 10;
    public static final int VDF_REWARD_VALUE = 20;
    public static final int VRF_REWARD_VALUE = 15;
    public static final BigDecimal TRANSACTION_LEADER_BLOCK_REWARD = BigDecimal.valueOf(LEADER_BLOCK_REWARD_VALUE).divide(BigDecimal.valueOf(100), DECIMAL_PRECISION, ROUNDING);
    public static final BigDecimal VDF_REWARD = BigDecimal.valueOf(VDF_REWARD_VALUE).divide(BigDecimal.valueOf(100), DECIMAL_PRECISION, ROUNDING);
    ;
    public static final BigDecimal VRF_REWARD = BigDecimal.valueOf(VRF_REWARD_VALUE).divide(BigDecimal.valueOf(100), DECIMAL_PRECISION, ROUNDING);
    ;
}
