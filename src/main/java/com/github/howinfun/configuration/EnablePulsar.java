package com.github.howinfun.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

/**
 * 开启pulsar自动配置，包括Producer和Consumer
 * @author winfun
 * @date 2021/8/20 4:36 下午
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Component
@Import({PulsarAutoConfiguration.class})
public @interface EnablePulsar {
}
