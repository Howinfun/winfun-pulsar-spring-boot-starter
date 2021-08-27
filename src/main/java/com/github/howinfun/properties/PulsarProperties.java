package com.github.howinfun.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Pulsar配置类
 * @author winfun
 * @date 2021/8/20 4:55 下午
 **/
@Data
@Component
@ConfigurationProperties(prefix = "pulsar")
@ConditionalOnExpression("!'${pulsar.serviceUrl}'.isEmpty()")
public class PulsarProperties {

    /**
     * pulsar服务地址
     */
    private String serviceUrl;
    /**
     * 租户
     */
    private String tenant;
    /**
     * 命名空间
     */
    private String namespace;
    /**
     * 消费者监听线程数
     */
    private Integer listenerThreads=10;
}
