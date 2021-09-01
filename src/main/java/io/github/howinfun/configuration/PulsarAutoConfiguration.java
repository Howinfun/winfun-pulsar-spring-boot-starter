package io.github.howinfun.configuration;

import io.github.howinfun.client.MultiPulsarClient;
import io.github.howinfun.ececption.PulsarAutoConfigException;
import io.github.howinfun.properties.MultiPulsarProperties;
import io.github.howinfun.properties.PulsarProperties;
import io.github.howinfun.template.PulsarTemplate;
import lombok.extern.slf4j.Slf4j;
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
     * 注入多数据源Pulsar客户端
     * @param multiPulsarProperties 多数据源pulsar自定义配置
     * @return 客户端
     */
    @Bean
    public MultiPulsarClient multiPulsarClient(MultiPulsarProperties multiPulsarProperties){
        return new MultiPulsarClient(multiPulsarProperties);
    }

    /**
     * 注入Pulsar Producer模版类
     * @param multiPulsarClient 多数据源Pulsar客户端
     * @param multiPulsarProperties pulsar自定义配置
     * @return 模版类
     */
    @Bean
    public PulsarTemplate pulsarTemplate(MultiPulsarClient multiPulsarClient, MultiPulsarProperties multiPulsarProperties){
        return new PulsarTemplate(multiPulsarClient,multiPulsarProperties);
    }

    /***
     * 注入Pulsar Consumer自动配置类
     * @param multiPulsarClient multiPulsarClient
     * @param multiPulsarProperties multiPulsarProperties
     * @return Pulsar Consumer 自动配置类
     **/
    @Bean
    public PulsarConsumerAutoConfigure pulsarConsumerAutoConfigure(MultiPulsarClient multiPulsarClient, MultiPulsarProperties multiPulsarProperties){
        return new PulsarConsumerAutoConfigure(multiPulsarClient,multiPulsarProperties);
    }
}
