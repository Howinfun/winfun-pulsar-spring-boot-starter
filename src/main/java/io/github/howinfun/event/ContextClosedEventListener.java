package io.github.howinfun.event;

import io.github.howinfun.properties.PulsarProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * 监听IOC容器
 * @author: winfun
 **/
@Slf4j
@Component
public class ContextClosedEventListener implements ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        // 关闭Pulsar客户端，释放所有相关资源
        PulsarClient pulsarClient = this.applicationContext.getBean(PulsarClient.class);
        try {
            pulsarClient.close();
            log.info("[Pulsar] 客户端关闭成功");
        } catch (PulsarClientException e) {
            log.error("[Pulsar] 客户端关闭失败",e);
            e.printStackTrace();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
