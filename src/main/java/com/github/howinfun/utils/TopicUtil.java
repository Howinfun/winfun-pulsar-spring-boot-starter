package com.github.howinfun.utils;

import java.util.StringJoiner;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.github.howinfun.constant.PulsarConstant.NON_PERSISTENT;
import static com.github.howinfun.constant.PulsarConstant.PATH_SPLIT;
import static com.github.howinfun.constant.PulsarConstant.PERSISTENT;

/**
 * 主题工具类
 * @author winfun
 * @date 2021/8/27 10:58 上午
 **/
public final class TopicUtil {

    private TopicUtil(){}

    /**
     * 拼接topic
     * @return 完整topic路径
     */
    public static String generateTopic(@NotNull Boolean persistent, @NotBlank String tenant, @NotBlank String namespace, @NotBlank String topic){

        StringJoiner stringJoiner = new StringJoiner(PATH_SPLIT);
        stringJoiner.add(tenant).add(namespace).add(topic);
        if (Boolean.TRUE.equals(persistent)){
            return PERSISTENT + stringJoiner.toString();
        }else {
            return NON_PERSISTENT + stringJoiner.toString();
        }
    }
}
