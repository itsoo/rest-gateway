# rest-gateway

#### 介绍

基于 Web Flux 开发的高性能自建站 API 网关系统，作为服务层对外提供通信和集群的公共基础功能，网关采用 Filters 链分别封装职责和逻辑（责任链模式），当请求经过过滤器链后，满足请求的有效性验证，此时将完成请求转发的相关逻辑（在此之前还需要通过负载均衡算法、请求上下文和 Path 共同组成目标 URL），调用失败的服务达到设置的频率阈值后会触发熔断（请求超时的流速器限制），熔断后的服务将在队列中等待尝试唤醒


#### 网关实现功能

1. 维护服务列表（配置文件维护或接入 Nacos）
2. 服务心跳检测（每 10 秒检测服务是否超时，通过队列和流速器实现，好处是单线程节省资源开销）
3. 熔断（请求的速率及失败率达到阈值），熔断后的服务会在队列中等待尝试唤醒
4. 服务降级，可通过更新配置中心的设置来使路由上下文（一般对应到系统）是否对外提供服务能力
5. 路由机制（按注册的应用名称划分对应的 routing key）
6. 内置负载均衡（支持轮询 RR、随机 R）
7. 登录鉴权，校验请求头是否携带有效 token（可自定义其他头信息等）
8. 生成 traceId 并设置到头信息（X-Trace-ID）
9. 服务响应超时（默认 1 秒）走兜底逻辑
10. 鉴权、直接放行列表项配置
11. 防火墙过滤器，将拦截黑名单中出现的 IP 访问，通过 Bloom Filter 实现


#### 参考配置项

```yaml
rest-gateway:
  # 请求流速限制
  rate-limiter: 2000.0
  # 触发熔断的失败速率
  rate-failure: 3.0
  # 黑名单开关
  black-enable: true
  # 黑名单列表（支持 IP 段配置）
  black-list:
    - unknown
    - 127.0.0.9/9
    - 9.9.9.9/9
  # 网络超时相关配置
  max-connections: 2000        # 最大连接数
  max-idle-time: 40000         # 最大空闲时间（毫秒）
  acquire-timeout: 6000        # 取得连接的最大等待时间（毫秒）
  read-timeout: 1000           # 请求超时时间（毫秒）
  write-timeout: 1000          # 响应超时时间（毫秒）
  connect-timeout: 1000        # 连接超时时间（毫秒）
  tcp-nodelay: true            # tcp 无延迟
  so-keep-alive: true          # keep-alive
  compress: false              # 开启压缩
  # 过滤掉的请求头
  filter-headers:
    - host
    - via
    - cookie
  # 不需要鉴权的 routers（router name）
  non-auth:
    - comment
  # 路由的配置信息
  routers:
    - name: comment            # router name
      lb-type: RR              # 负载均衡策略（round-robin=RR, random=R）
      prefix: /api/v1/comment  # 匹配请求路径的前缀
      services:                # service 列表
        - 127.0.0.1:8080
        - 127.0.0.1:8081
        - 127.0.0.1:8082
        - 127.0.0.1:8083
    - name: product
      prefix: /api/v1/product
      services:
        - 127.0.0.1:8090
        - 127.0.0.1:8091
```
