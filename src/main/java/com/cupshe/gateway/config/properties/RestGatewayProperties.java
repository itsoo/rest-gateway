package com.cupshe.gateway.config.properties;

import com.cupshe.gateway.core.Router;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;

/**
 * RestGatewayProperties
 *
 * @author zxy
 */
@Data
@ConfigurationProperties(prefix = "rest-gateway")
public class RestGatewayProperties {

    /*** 请求流速限制 */
    private double rateLimiter;

    /*** 触发熔断的失败速率 */
    private double rateFailure;

    /*** 黑名单开关 */
    private boolean blackEnable;

    /*** 黑名单列表（支持 IP 段配置） */
    private List<String> blackList;

    //---------------------
    // NETWORK TIMEOUT
    //---------------------

    /*** 最大连接数 */
    private int maxConnections;

    /*** 最大空闲时间（毫秒） */
    private int maxIdleTime;

    /*** 取得连接的最大等待时间（毫秒） */
    private long acquireTimeout;

    /*** 请求超时时间（毫秒） */
    private long readTimeout;

    /*** 响应超时时间（毫秒） */
    private long writeTimeout;

    /*** 连接超时时间（毫秒） */
    private int connectTimeout;

    /*** tcp 无延迟 */
    private boolean tcpNodelay;

    /*** keep-alive */
    private boolean soKeepAlive;

    /*** 开启压缩 */
    private boolean compress;

    /*** 过滤掉的请求头 */
    private Set<String> filterHeaders;

    /*** 不需要鉴权的 routers（router name） */
    private Set<String> nonAuth;

    /*** 路由的配置信息 */
    private List<Router> routers;

    public RestGatewayProperties() {
        this.rateLimiter = 2_000.0;
        this.rateFailure = 3.0;
        this.blackEnable = false;
        this.blackList = Lists.newLinkedList();
        this.maxConnections = 2_000;
        this.maxIdleTime = 40_000;
        this.acquireTimeout = 6_000L;
        this.readTimeout = 1_000L;
        this.writeTimeout = 1_000L;
        this.connectTimeout = 1_000;
        this.tcpNodelay = true;
        this.soKeepAlive = true;
        this.compress = false;
        this.filterHeaders = Sets.newLinkedHashSet();
        this.nonAuth = Sets.newLinkedHashSet();
        this.routers = Lists.newLinkedList();
    }
}
