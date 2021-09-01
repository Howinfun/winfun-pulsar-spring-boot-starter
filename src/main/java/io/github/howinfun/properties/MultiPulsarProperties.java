package io.github.howinfun.properties;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Pulsar配置类
 * 支持多数据源注入：key-数据源名称、value-对应配置值
 * @author winfun
 **/
@Data
@Component
@ConfigurationProperties(prefix = "pulsar")
@ConditionalOnExpression("!'${pulsar.serviceUrl}'.isEmpty()")
public class MultiPulsarProperties {

    /**
     * 默认数据源名称：default
     */
    public static final String DEFAULT_SOURCE_NAME = "default";
    /**
     * pulsar服务地址
     */
    private Map<String,String> serviceUrl;
    /**
     * 租户
     */
    private Map<String,String> tenant;
    /**
     * 命名空间
     */
    private Map<String,String> namespace;
    /**
     * 是否开启TCP不延迟
     */
    private Map<String,Boolean> enableTcpNoDelay;
    /**
     * 操作超时，单位毫秒
     */
    private Map<String,Integer> operationTimeout;
    /**
     * 消费者监听线程数
     */
    private Map<String,Integer> listenerThreads;
    /**
     * IO线程数
     */
    private Map<String,Integer> ioThreads;
}
