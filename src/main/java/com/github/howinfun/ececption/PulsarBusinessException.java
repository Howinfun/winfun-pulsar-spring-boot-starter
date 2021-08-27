package com.github.howinfun.ececption;

/**
 * 自定义Pulsar异常
 * @author winfun
 * @date 2021/8/24 7:15 下午
 **/
public class PulsarBusinessException extends RuntimeException{

    public PulsarBusinessException(String msg){
        super(msg);
    }

    public PulsarBusinessException(String msg,Throwable e){
        super(msg,e);
    }
}
