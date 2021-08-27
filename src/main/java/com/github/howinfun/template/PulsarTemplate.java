package com.github.howinfun.template;

import com.github.howinfun.ececption.PulsarBusinessException;
import com.github.howinfun.properties.PulsarProperties;
import com.github.howinfun.utils.TopicUtil;
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
 * @date 2021/8/25 1:49 下午
 **/
@Slf4j
public class PulsarTemplate {

    /**
     * producer 缓存
     * key：topic，value：producer
     */
    private final ConcurrentHashMap<String,Producer<String>> producerCaches = new ConcurrentHashMap<>(64);
    private final PulsarClient client;
    private final PulsarProperties properties;

    public PulsarTemplate(PulsarClient client,PulsarProperties pulsarProperties){
        this.client = client;
        this.properties = pulsarProperties;
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
                return this.sendAsync(msg).get();
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
            try {
                Producer<String> producer = PulsarTemplate.this.producerCaches.getOrDefault(finalTopic,null);
                if (Objects.isNull(producer)){
                    producer = PulsarTemplate.this.client.newProducer(Schema.STRING).topic(finalTopic).create();
                    PulsarTemplate.this.producerCaches.put(finalTopic,producer);
                }
                return producer.sendAsync(msg);
            } catch (PulsarClientException e) {
                log.error("[Pulsar] Producer初始化失败，topic is {}",finalTopic);
                throw e;
            }
        }

        /**
         * 拼接topic
         * @return 完整topic路径
         */
        private String generateTopic(){
            if (StringUtils.isBlank(this.topic)){
                throw new PulsarBusinessException("topic不能为空");
            }
            String finalTenant = StringUtils.isNotBlank(this.tenant)?this.tenant:PulsarTemplate.this.properties.getTenant();
            String finalNamespace = StringUtils.isNotBlank(this.namespace)?this.namespace:PulsarTemplate.this.properties.getNamespace();
            Boolean finalPersistent = Objects.nonNull(this.persistent)?this.persistent:Boolean.TRUE;
            return TopicUtil.generateTopic(finalPersistent,finalTenant,finalNamespace,this.topic);
        }

    }

}
