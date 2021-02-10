package com.cupshe.gateway.core.lb;

/**
 * LoadBalance
 * <ul>
 *   <li>{@link LoadBalanceType#RR} (default)</li>
 *   <li>{@link LoadBalanceType#R}</li>
 * </ul>
 */
public enum LoadBalanceType {

    /*** round-robin */
    RR,

    /*** random */
    R
}
