package com.cupshe.gateway.constant;

/**
 * Ordered
 *
 * @author zxy
 */
public interface Ordered {

    /**
     * Useful constant for the highest precedence value.
     *
     * @see org.springframework.core.Ordered#HIGHEST_PRECEDENCE
     */
    int HIGHEST = Integer.MIN_VALUE;

    int NEXT_HIGHEST = Integer.MIN_VALUE + 1;

    /**
     * Useful constant for the lowest precedence value.
     *
     * @see org.springframework.core.Ordered#LOWEST_PRECEDENCE
     */
    int LOWEST = Integer.MAX_VALUE;

    int PRE_LOWEST = Integer.MAX_VALUE - 1;

    int PRE_FIRST = -1;

    int FIRST = 0;

    int SECOND = 1;

    int THIRD = 2;

    int FOURTH = 3;

    int FIFTH = 4;
}
