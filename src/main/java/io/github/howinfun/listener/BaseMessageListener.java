package io.github.howinfun.listener;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
 **/
@Slf4j
public abstract class BaseMessageListener implements MessageListener<String> {

    private Executor executor;

    /***
     * 初始化Consumer线程池
     * @author winfun
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大核心线程数
     * @param keepAliveTime 线程保活时长，单位分钟
     * @param maxQueueLength 最大等待队列长度
     * @return {@link Void }
     **/
    public void initThreadPool(Integer corePoolSize,Integer maximumPoolSize,Integer keepAliveTime,Integer maxQueueLength,String threadPoolName){
        if (Objects.isNull(this.executor) && Boolean.TRUE.equals(this.enableAsync())){
            this.executor = new ThreadPoolExecutor(
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    TimeUnit.MINUTES,
                    new LinkedBlockingDeque<>(maxQueueLength),
                    new ThreadFactoryBuilder().setNameFormat(threadPoolName+"-%d").get(),
                    // 使用CallerRunsPolicy拒绝策略，让当前线程执行，避免消息丢失
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
            log.info("[Pulsar] Consumer消费线程池初始化成功！");
        }
    }

    @Override
    public void received(Consumer<String> consumer, Message<String> msg) {
        if (Objects.nonNull(this.executor) && Boolean.TRUE.equals(this.enableAsync())) {
            this.executor.execute(() -> this.doReceived(consumer, msg));
        }else {
            this.doReceived(consumer,msg);
        }
    }

    /**
     * 消费消息
     * 自定义监听器实现方法
     * 消息如何响应由开发者决定：
     *      Consumer#acknowledge
     *      Consumer#reconsumeLater
     *      Consumer#negativeAcknowledge
     * @param consumer 消费者
     * @param msg 消息
     */
    protected abstract void doReceived(Consumer<String> consumer, Message<String> msg);

    /***
     * 是否开启异步消费
     * @return {@link Boolean }
     **/
    public Boolean enableAsync(){
        return Boolean.TRUE;
    }
}
