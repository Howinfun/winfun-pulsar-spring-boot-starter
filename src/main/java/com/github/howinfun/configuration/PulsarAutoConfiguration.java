package com.github.howinfun.configuration;

import com.github.howinfun.ececption.PulsarAutoConfigException;
import com.github.howinfun.properties.PulsarProperties;
import com.github.howinfun.template.PulsarTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * pulsar 自动配置
 * @author winfun
 * @date 2021/8/20 4:36 下午
 **/
@Slf4j
@Configuration
@ComponentScan("com.github.howinfun")
@ConditionalOnExpression("!'${pulsar.serviceUrl}'.isEmpty()")
public class PulsarAutoConfiguration {

    /**
     * 注入Pulsar客户端
     * @param pulsarProperties pulsar自定义配置
     * @return 客户端
     */
    @Bean
    public PulsarClient pulsarClient(PulsarProperties pulsarProperties){

        try {
            /**
             * listenerThreads为所有Consumer的MessageListener共享
             */
            return PulsarClient.builder().serviceUrl(pulsarProperties.getServiceUrl()).listenerThreads(pulsarProperties.getListenerThreads()).build();
        } catch (PulsarClientException e) {
            log.error("[Pulsar] 客户端实例化失败！");
            throw new PulsarAutoConfigException("[Pulsar] 客户端实例化失败！",e);
        }
    }

    /**
     * 注入Pulsar Producer模版类
     * @param pulsarClient pulsar客户端
     * @param pulsarProperties pulsar自定义配置
     * @return 模版类
     */
    @Bean
    public PulsarTemplate pulsarTemplate(PulsarClient pulsarClient, PulsarProperties pulsarProperties){
        return new PulsarTemplate(pulsarClient,pulsarProperties);
    }

    /**
     * consumer 自动化配置
     * @return PulsarConsumerAutoConfigure
     */
    @Bean
    public PulsarConsumerAutoConfigure pulsarConsumerAutoConfigure(PulsarClient pulsarClient, PulsarProperties pulsarProperties){
        return new PulsarConsumerAutoConfigure(pulsarClient,pulsarProperties);
    }
}
