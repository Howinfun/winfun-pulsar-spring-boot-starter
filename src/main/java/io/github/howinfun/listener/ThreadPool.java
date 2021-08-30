package io.github.howinfun.listener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解
 * Consumer线程池参数
 * @author winfun
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ThreadPool {

    /**
     * 线程名称
     * @return
     */
    String threadPoolName() default "base-message-listener-execute";
    /**
     * 核心线程数，默认1
     */
    int coreThreads() default 1;
    /**
     * 核心线程数，默认1
     */
    int maxCoreThreads() default 1;
    /**
     * 线程活跃时长，单位分钟，默认10
     */
    int keepAliveTime() default 10;
    /**
     * 最大等待队列长度，默认100
     */
    int maxQueueLength() default 100;
}
