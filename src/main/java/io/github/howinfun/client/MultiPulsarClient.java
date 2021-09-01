package io.github.howinfun.client;

import cn.hutool.core.collection.CollUtil;
import io.github.howinfun.ececption.PulsarAutoConfigException;
import io.github.howinfun.properties.MultiPulsarProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.shade.org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.DisposableBean;

/**
 * 多数据源Pulsar客户端
 * @author winfun
 * @date 2021/9/1 9:17 上午
 **/
@Slf4j
public class MultiPulsarClient extends HashMap<String,PulsarClient> implements DisposableBean {

    public MultiPulsarClient(MultiPulsarProperties multiPulsarProperties){
        Map<String,String> serviceUrlMap = multiPulsarProperties.getServiceUrl();
        if (null != serviceUrlMap && !serviceUrlMap.isEmpty()){
            for (Entry<String, String> entry : serviceUrlMap.entrySet()) {
                String sourceName = entry.getKey();
                String serviceUrl = entry.getValue();
                if (StringUtils.isNotBlank(serviceUrl)){
                    try {
                        ClientBuilder clientBuilder = PulsarClient.builder().serviceUrl(serviceUrl);
                        if (CollUtil.isNotEmpty(multiPulsarProperties.getEnableTcpNoDelay())){
                            Boolean enableTcpNoDelay = multiPulsarProperties.getEnableTcpNoDelay().getOrDefault(sourceName,multiPulsarProperties.getDefaultEnableTcpNoDelay());
                            clientBuilder.enableTcpNoDelay(enableTcpNoDelay);
                        }
                        if (CollUtil.isNotEmpty(multiPulsarProperties.getIoThreads())){
                            Integer ioThreads = multiPulsarProperties.getIoThreads().getOrDefault(sourceName,multiPulsarProperties.getDefaultIoThreads());
                            clientBuilder.ioThreads(ioThreads);
                        }
                        if (CollUtil.isNotEmpty(multiPulsarProperties.getListenerThreads())){
                            Integer listenerThreads = multiPulsarProperties.getListenerThreads().getOrDefault(sourceName,multiPulsarProperties.getDefaultListenerThreads());
                            clientBuilder.listenerThreads(listenerThreads);
                        }
                        if (CollUtil.isNotEmpty(multiPulsarProperties.getOperationTimeout())){
                            Integer operationTimeout = multiPulsarProperties.getOperationTimeout().getOrDefault(sourceName,multiPulsarProperties.getDefaultOperationTimeout());
                            clientBuilder.operationTimeout(operationTimeout,TimeUnit.SECONDS);
                        }
                        PulsarClient client = clientBuilder.build();
                        log.info("[Pulsar] Client实例化成功");
                        this.put(sourceName,client);
                    } catch (PulsarClientException e) {
                        log.error("[Pulsar] Client实例化失败！");
                        throw new PulsarAutoConfigException("[Pulsar] Client实例化失败！", e);
                    }
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        this.values().forEach(pulsarClient -> {
            try {
                pulsarClient.close();
                log.info("[Pulsar] 客户端关闭成功");
            } catch (PulsarClientException e) {
                log.error("[Pulsar] 客户端关闭失败",e);
            }
        });
    }
}
