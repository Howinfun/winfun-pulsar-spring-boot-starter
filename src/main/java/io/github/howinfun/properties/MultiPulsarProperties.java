package io.github.howinfun.properties;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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

    /**
     * 是否开启TCP不延迟
     */
    private Boolean defaultEnableTcpNoDelay=true;
    /**
     * 操作超时，单位秒
     */
    private Integer defaultOperationTimeout=30;
    /**
     * 消费者监听线程数
     */
    private Integer defaultListenerThreads=1;
    /**
     * IO线程数
     */
    private Integer defaultIoThreads=1;

    /**
     * 根据数据源名称获取 pulsar服务地址
     * @param sourceName 数据源名称
     * @return pulsar服务地址
     */
    public String getServiceUrlBySourceName(String sourceName){
        if (CollectionUtils.isEmpty(this.serviceUrl)){
            return null;
        }
        return this.serviceUrl.getOrDefault(sourceName,"");
    }

    /**
     * 根据数据源名称获取 租户
     * @param sourceName 数据源名称
     * @return 租户
     */
    public String getTenantBySourceName(String sourceName){
        if (CollectionUtils.isEmpty(this.tenant)){
            return null;
        }
        return this.tenant.getOrDefault(sourceName,"");
    }

    /**
     * 根据数据源名称获取 命名空间
     * @param sourceName 数据源名称
     * @return 命名空间
     */
    public String getNamespaceBySourceName(String sourceName){
        if (CollectionUtils.isEmpty(this.namespace)){
            return null;
        }
        return this.namespace.getOrDefault(sourceName,"");
    }

    /**
     * 根据数据源名称获取 enableTcpNoDelay 开关
     * @param sourceName 数据源名称
     * @return 开关
     */
    public Boolean getEnableTcpNoDelayBySourceName(String sourceName){
        if (CollectionUtils.isEmpty(this.enableTcpNoDelay)){
            return this.defaultEnableTcpNoDelay;
        }
        return this.enableTcpNoDelay.getOrDefault(sourceName,this.defaultEnableTcpNoDelay);
    }

    /**
     * 根据数据源名称获取 操作超时时长
     * @param sourceName 数据源名称
     * @return 超时时长
     */
    public Integer getOperationTimeoutBySourceName(String sourceName){
        if (CollectionUtils.isEmpty(this.operationTimeout)){
            return this.defaultOperationTimeout;
        }
        return this.operationTimeout.getOrDefault(sourceName,this.defaultOperationTimeout);
    }

    /**
     * 根据数据源名称获取 消费者监听线程数
     * @param sourceName 数据源名称
     * @return 消费者监听线程数
     */
    public Integer getListenerThreadsBySourceName(String sourceName){
        if (CollectionUtils.isEmpty(this.listenerThreads)){
            return this.defaultListenerThreads;
        }
        return this.listenerThreads.getOrDefault(sourceName,this.defaultListenerThreads);
    }

    /**
     * 根据数据源名称获取 IO线程数
     * @param sourceName 数据源名称
     * @return IO线程数
     */
    public Integer getIoThreadsBySourceName(String sourceName){
        if (CollectionUtils.isEmpty(this.ioThreads)){
            return this.defaultIoThreads;
        }
        return this.ioThreads.getOrDefault(sourceName,this.defaultIoThreads);
    }
}
