package io.github.howinfun.configuration;

import io.github.howinfun.ececption.PulsarAutoConfigException;
import io.github.howinfun.properties.MultiPulsarProperties;
import io.github.howinfun.properties.PulsarProperties;
import io.github.howinfun.template.PulsarTemplate;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * pulsar 自动配置
 * @author winfun
 **/
@Slf4j
@Configuration
@ComponentScan("io.github.howinfun")
@ConditionalOnExpression("!'${pulsar.serviceUrl}'.isEmpty()")
@EnableConfigurationProperties({PulsarProperties.class})
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
            PulsarClient client = PulsarClient.builder().serviceUrl(pulsarProperties.getServiceUrl())
                    .ioThreads(pulsarProperties.getIoThreads())
                    .listenerThreads(pulsarProperties.getListenerThreads())
                    .enableTcpNoDelay(pulsarProperties.getEnableTcpNoDelay())
                    .operationTimeout(pulsarProperties.getOperationTimeout(), TimeUnit.SECONDS)
                    .build();
            log.info("[Pulsar] Client实例化成功");
            return client;
        } catch (PulsarClientException e) {
            log.error("[Pulsar] Client实例化失败！");
            throw new PulsarAutoConfigException("[Pulsar] Client实例化失败！", e);
        }
    }

    /**
     * 注入Pulsar Producer模版类
     * @param pulsarClient pulsar客户端
     * @param multiPulsarProperties pulsar自定义配置
     * @return 模版类
     */
    @Bean
    public PulsarTemplate pulsarTemplate(PulsarClient pulsarClient, MultiPulsarProperties multiPulsarProperties){
        return new PulsarTemplate(pulsarClient,multiPulsarProperties);
    }

    /***
     * 注入Pulsar Consumer自动配置类
     * @param pulsarClient pulsarClient
     * @param multiPulsarProperties multiPulsarProperties
     * @return Pulsar Consumer 自动配置类
     **/
    @Bean
    public PulsarConsumerAutoConfigure pulsarConsumerAutoConfigure(PulsarClient pulsarClient, MultiPulsarProperties multiPulsarProperties){
        return new PulsarConsumerAutoConfigure(pulsarClient,multiPulsarProperties);
    }
}
