package io.github.howinfun.configuration;

import cn.hutool.core.util.RandomUtil;
import io.github.howinfun.ececption.PulsarAutoConfigException;
import io.github.howinfun.listener.BaseMessageListener;
import io.github.howinfun.listener.PulsarListener;
import io.github.howinfun.properties.PulsarProperties;
import io.github.howinfun.utils.TopicUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.shade.org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;

/**
 * 消费者自动初始化
 * @author winfun
 **/
@Slf4j
public class PulsarConsumerAutoConfigure implements CommandLineRunner {

    /**
     * 自定义消费者监听器列表，可为空
     */
    @Autowired(required = false)
    private List<BaseMessageListener> listeners;
    private final PulsarClient pulsarClient;
    private final PulsarProperties pulsarProperties;

    public PulsarConsumerAutoConfigure(PulsarClient pulsarClient, PulsarProperties pulsarProperties){
        this.pulsarClient = pulsarClient;
        this.pulsarProperties = pulsarProperties;
    }

    /**
     * 注入消费者到IOC容器
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        if (!CollectionUtils.isEmpty(this.listeners)){
            this.listeners.forEach(baseMessageListener -> {
                // 获取当前监听器的@PulsarListener信息
                PulsarListener pulsarListener = AnnotationUtils.findAnnotation(baseMessageListener.getClass(), PulsarListener.class);
                if (Objects.nonNull(pulsarListener)){
                    try {
                        ConsumerBuilder<String> consumerBuilder = this.pulsarClient.newConsumer(Schema.STRING).receiverQueueSize(pulsarListener.receiverQueueSize());
                        if (pulsarListener.topics().length > 0){
                            List<String> topics = new ArrayList<>(pulsarListener.topics().length);
                            String tenant = StringUtils.isBlank(pulsarListener.tenant())?this.pulsarProperties.getTenant():pulsarListener.tenant();
                            String namespace = StringUtils.isBlank(pulsarListener.namespace())?this.pulsarProperties.getNamespace():pulsarListener.namespace();
                            Boolean persistent = Objects.nonNull(pulsarListener.persistent())?pulsarListener.persistent():Boolean.TRUE;
                            /**
                             * 处理topics
                             */
                            for (String topic : pulsarListener.topics()) {
                                topics.add(TopicUtil.generateTopic(persistent, tenant, namespace, topic));
                            }
                            consumerBuilder.topics(topics);
                            /**
                             * 处理订阅名称
                             */
                            String subscriptionName = StringUtils.isBlank(pulsarListener.subscriptionName())?"subscription_"+ RandomUtil.randomString(3):pulsarListener.subscriptionName();
                            consumerBuilder.subscriptionName(subscriptionName);
                            consumerBuilder.ackTimeout(Long.parseLong(pulsarListener.ackTimeout()), TimeUnit.MILLISECONDS);
                            consumerBuilder.subscriptionType(pulsarListener.subscriptionType());
                            /**
                             * 处理死信策略
                             */
                            if (Boolean.TRUE.equals(pulsarListener.enableRetry())){
                                DeadLetterPolicy deadLetterPolicy = DeadLetterPolicy.builder()
                                        .maxRedeliverCount(pulsarListener.maxRedeliverCount())
                                        .build();
                                if (StringUtils.isNotBlank(pulsarListener.retryLetterTopic())){
                                    deadLetterPolicy.setRetryLetterTopic(pulsarListener.retryLetterTopic());
                                }
                                if (StringUtils.isNotBlank(pulsarListener.deadLetterTopic())){
                                    deadLetterPolicy.setDeadLetterTopic(pulsarListener.deadLetterTopic());
                                }
                                consumerBuilder.enableRetry(pulsarListener.enableRetry()).deadLetterPolicy(deadLetterPolicy);
                            }else {
                                if (StringUtils.isNotBlank(pulsarListener.deadLetterTopic())){
                                    if (SubscriptionType.Exclusive.equals(pulsarListener.subscriptionType())){
                                        throw new PulsarAutoConfigException("消费端仅支持在Shared/Key_Shared模式下单独使用死信队列");
                                    }
                                    DeadLetterPolicy deadLetterPolicy = DeadLetterPolicy.builder()
                                            .maxRedeliverCount(pulsarListener.maxRedeliverCount())
                                            .deadLetterTopic(pulsarListener.deadLetterTopic())
                                            .build();
                                    consumerBuilder.deadLetterPolicy(deadLetterPolicy);
                                }
                            }
                            consumerBuilder.messageListener(baseMessageListener);
                            Consumer<String> consumer = consumerBuilder.subscribe();
                            log.info("[Pulsar] 消费者初始化完毕,topic is {},",consumer.getTopic());
                        }
                    } catch (PulsarClientException e) {
                        throw new PulsarAutoConfigException("[Pulsar] consumer初始化异常",e);
                    }
                }
            });
        }else {
            log.warn("[Pulsar] 未发现有消费者");
        }
    }
}
