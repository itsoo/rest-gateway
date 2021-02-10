package com.cupshe.gateway.core;

import com.cupshe.gateway.core.lb.LoadBalanceType;
import lombok.Data;

import java.util.List;

/**
 * Router
 *
 * @author zxy
 */
@Data
public class Router {

    /*** 路由名称 */
    private String name;

    /*** 匹配请求路径的前缀 */
    private String prefix;

    /*** 可用状态 */
    private boolean status;

    /*** service 列表 */
    private List<String> services;

    /*** 负载均衡策略（round-robin=RR, random=R） */
    private LoadBalanceType lbType;

    public Router() {
        this.status = true;
        this.lbType = LoadBalanceType.RR;
    }
}
