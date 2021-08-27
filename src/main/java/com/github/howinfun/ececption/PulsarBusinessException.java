package com.github.howinfun.ececption;

/**
 * 自定义Pulsar异常
 * @author winfun
 **/
public class PulsarBusinessException extends RuntimeException{

    public PulsarBusinessException(String msg){
        super(msg);
    }

    public PulsarBusinessException(String msg,Throwable e){
        super(msg,e);
    }
}
