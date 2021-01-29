package com.cupshe.gateway.core;

import lombok.Data;

import java.util.List;

/**
 * Router
 *
 * @author zxy
 */
@Data
public class Router {

    /*** router name */
    private String name;

    /*** 负载均衡策略（round-robin=RR, random=R） */
    private String prefix;

    /*** 匹配请求路径的前缀 */
    private List<String> services;

    /*** service 列表 */
    private LoadBalance lbType;

    public Router() {
        lbType = LoadBalance.RR;
    }

    /**
     * LoadBalance
     * <ul>
     *   <li>{@link LoadBalance#RR} (default)</li>
     *   <li>{@link LoadBalance#R}</li>
     * </ul>
     */
    public enum LoadBalance {

        /*** round-robin */
        RR,

        /*** random */
        R
    }
}
