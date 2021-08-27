package com.github.howinfun.listener;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import jodd.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;

/**
 * 基础MessageListener
 * 配合@PulsarListener注解使用，集成BaseMessageListener后，需主动注入IOC容器中
 * 自动配置会根据@PulsarListener的信息进行consumer的初始化
 * 支持线程池异步消费消息
 * @author winfun
 * @date 2021/8/20 4:42 下午
 **/
@Slf4j
public abstract class BaseMessageListener implements MessageListener<String> {

    private Executor executor;

    /**
     * 初始化consumer线程池->消费消息
     * 自定义线程池参数 TODO
     */
    @PostConstruct
    private void init(){
        this.executor = new ThreadPoolExecutor(
                3,
                3,
                60,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(100),
                new ThreadFactoryBuilder().setNameFormat("base-message-listener-execute-%d").get(),
                // 使用CallerRunsPolicy拒绝策略，让当前线程执行，避免消息丢失
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        log.info("[Pulsar] consumer线程池初始化成功");
    }

    @Override
    public void received(Consumer<String> consumer, Message<String> msg) {
        this.executor.execute(()-> {
            try {
                this.doReceived(consumer, msg);
                consumer.acknowledge(msg);
            } catch (Exception e) {
                log.error("[Pulsar] consumer消费消息失败",e);
                consumer.negativeAcknowledge(msg);
            }
        });
    }

    /**
     * 消费消息
     * 自定义监听器实现方法
     * @param consumer 消费者
     * @param msg 消息
     */
    protected abstract void doReceived(Consumer<String> consumer, Message<String> msg);
}
