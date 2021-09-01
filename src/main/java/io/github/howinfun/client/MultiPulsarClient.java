package io.github.howinfun.client;

import io.github.howinfun.properties.MultiPulsarProperties;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
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
                String tenant = multiPulsarProperties.getTenant().getOrDefault(sourceName,"");
                String namespace = multiPulsarProperties.getNamespace().getOrDefault(sourceName,"");
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
