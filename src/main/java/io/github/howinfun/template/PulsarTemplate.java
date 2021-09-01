package io.github.howinfun.template;

import io.github.howinfun.client.MultiPulsarClient;
import io.github.howinfun.ececption.PulsarBusinessException;
import io.github.howinfun.properties.MultiPulsarProperties;
import io.github.howinfun.utils.TopicUtil;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.shade.org.apache.commons.lang.StringUtils;

/**
 * Pulsar Producer Template
 * @author winfun
 **/
@Slf4j
public class PulsarTemplate {

    /**
     * producer 缓存
     * key：topic，value：producer
     */
    private final ConcurrentHashMap<String,Producer<String>> producerCaches = new ConcurrentHashMap<>(64);
    /**
     * 多数据源Pulsar客户端
     */
    private final MultiPulsarClient multiPulsarClient;
    /**
     * 多数据源Pulsar自定义配置
     */
    private final MultiPulsarProperties multiPulsarProperties;

    public PulsarTemplate(MultiPulsarClient multiPulsarClient, MultiPulsarProperties multiPulsarProperties){
        this.multiPulsarClient = multiPulsarClient;
        this.multiPulsarProperties = multiPulsarProperties;
    }

    /**
     * 创建Builder
     * @return Builder
     */
    public Builder createBuilder(){
        return new Builder();
    }

    /**
     * Builder模式
     * 开发者可自行指定租户/命名空间，如果不指定，则使用配置文件
     */
    public class Builder {

        /**
         * 数据源名称
         * 默认值：{@link MultiPulsarProperties#DEFAULT_SOURCE_NAME}
         */
        private String sourceName;
        /**
         * 是否持久化
         */
        private Boolean persistent;
        /**
         * 租户
         */
        private String tenant;
        /**
         * 命名空间
         */
        private String namespace;
        /**
         * 主题
         */
        private String topic;

        public Builder sourceName(String sourceName){
            this.sourceName = sourceName;
            return this;
        }

        public Builder persistent(Boolean persistent){
            this.persistent = persistent;
            return this;
        }

        public Builder tenant(String tenant){
            this.tenant = tenant;
            return this;
        }

        public Builder namespace(String namespace){
            this.namespace = namespace;
            return this;
        }

        public Builder topic(String topic){
            this.topic = topic;
            return this;
        }

        /**
         * 同步发送消息
         * @param msg 消息
         * @return 消息ID
         */
        public MessageId send(String msg) throws Exception{
            try {
                MessageId messageId = this.sendAsync(msg).get();
                log.info("[Pulsar] Producer同步发送消息成功，msg is {}",msg);
                return messageId;
            } catch (InterruptedException | ExecutionException e) {
                log.error("[Pulsar] Producer同步发送消息失败，msg is {}",msg);
                throw e;
            }
        }

        /**
         * 异步发送消息
         * @param msg 消息
         * @return CompletableFuture
         */
        public CompletableFuture<MessageId> sendAsync(String msg) throws PulsarClientException{

            String finalTopic = this.generateTopic();
            String sourceName = StringUtils.isNotBlank(this.sourceName) ? this.sourceName : MultiPulsarProperties.DEFAULT_SOURCE_NAME;
            try {
                Producer<String> producer = PulsarTemplate.this.producerCaches.getOrDefault(finalTopic,null);
                if (Objects.isNull(producer)){
                    PulsarClient client = PulsarTemplate.this.multiPulsarClient.getOrDefault(sourceName,null);
                    if (Objects.isNull(client)){
                        log.error("[Pulsar] 数据源对应PulsarClient不存在，sourceName is {}",sourceName);
                        throw new PulsarBusinessException("[Pulsar] 数据源对应PulsarClient不存在！");
                    }
                    producer = client.newProducer(Schema.STRING).topic(finalTopic).create();
                    PulsarTemplate.this.producerCaches.put(finalTopic,producer);
                    log.info("[Pulsar] Producer实例化成功，sourceName is {}, topic is {}",sourceName,finalTopic);
                }
                return producer.sendAsync(msg);
            } catch (Exception e) {
                log.error("[Pulsar] Producer实例化失败，topic is {}",finalTopic);
                throw e;
            }
        }

        /**
         * 拼接topic
         * @return 完整topic路径
         */
        private String generateTopic(){
            if (StringUtils.isBlank(this.topic)){
                log.error("[Pulsar] Topic 为空，无法发送消息, topic is {}",this.topic);
                throw new PulsarBusinessException("Topic不能为空");
            }
            String finalTenant = StringUtils.isNotBlank(this.tenant)?this.tenant:PulsarTemplate.this.multiPulsarProperties.getTenantBySourceName(this.sourceName);
            String finalNamespace = StringUtils.isNotBlank(this.namespace)?this.namespace:PulsarTemplate.this.multiPulsarProperties.getNamespaceBySourceName(this.sourceName);
            if (StringUtils.isBlank(finalTenant) || StringUtils.isBlank(finalNamespace)){
                log.error("[Pulsar] 租户||命名空间为空，无法创建发送消息, tenant is {}, namespace is {}",finalTenant,finalNamespace);
                throw new PulsarBusinessException("租户||命名空间为空，无法发送消息");
            }
            Boolean finalPersistent = Objects.nonNull(this.persistent)?this.persistent:Boolean.TRUE;
            return TopicUtil.generateTopic(finalPersistent, finalTenant, finalNamespace, this.topic);
        }

    }

}
