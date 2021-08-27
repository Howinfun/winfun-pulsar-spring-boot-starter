package com.github.howinfun.ececption;

/**
 * 自定义Pulsar异常
 * @author winfun
 * @date 2021/8/24 7:15 下午
 **/
public class PulsarAutoConfigException extends RuntimeException{

    public PulsarAutoConfigException(String msg){
        super(msg);
    }

    public PulsarAutoConfigException(String msg,Throwable e){
        super(msg,e);
    }
}
