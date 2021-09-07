# winfun-pulsar-spring-boot-starter


# 里程碑


版本 | 功能点 | 作者 | 完成 
---|---|---|---
1.0.0 | 支持PulsarTemplate发送消息&支持自定义注解实例化Consumer监听消息 | howinfun | ✅
1.1.0 | 支持动态开启/关闭Consumer消费线程池、支持自定义配置Consuemr消费线程池参数 | howinfun | ✅
1.2.0 | 支持多Pulsar数据源 | howinfun | ✅
1.2.0 | 支持Spring容器停止时，释放Pulsar所有相关资源 | howinfun | ✅

# 一、背景

Pulsar 作为新生代云原生消息队列，越来越受到开发者的热爱；而我们现在基本上的项目都是基于 SpringBoot 上开发的，但是我们可以发现，至今都没有比较大众和成熟的关于 Pulsar 的 Starter，所以我们需要自己整一个，从而避免常规使用 Pulsar API 时产生大量的重复代码。

# 二、设计思路

由于是第一版的设计，所以我们是从简单开始，不会一开始就设计得很复杂，尽量保留 Pulsar API 原生的功能。

## 2.1、PulsarClient

我们都知道，不管是 Producer 还是 Consumer，都是由 PulsarClient 创建的。

当然了，PulsarClient 可以根据业务需要自定义很多参数，但是第一版的设计只会支持比较常用的参数。

我们这个组件支持下面功能点：
- 支持 PulsarClient 参数配置外部化，参数可配置在 applicatin.properties 中。
- 支持 applicatin.properties 提供配置提示信息。
- 读取外部配置文件，根据参数实例化 PulsarClient，并注入到 IOC 容器中。


## 2.2、Producer

Producer是发送消息的组件。

- 这里我们提供一个模版类，可以根据需求创建对应的 Producer 实例。
- 支持将 Topic<->Producer 关系缓存起来，避免重复创建 Producer 实例。
- 支持同步/异步发送消息。

## 2.3、Consumer

Consumer是消费消息的组件。

- 这里我们提供一个抽象类，开发者只需要集成此实现类并实现 doReceive 方法即可，即消费消息的逻辑方法。
- 接着还提供一个自定义注解，自定义注解支持自定义 Consmuer 配置，例如Topic、Tenant、Namespace等。
- 实现类加入上述自定义注解后，组件将会自动识别并且生成对应的 Consumer 实例。
- 支持同步/线程池异步消费。

# 三、使用例子


## 3.1、winfun-pulsar-spring-boot-starter:1.1.0 版本

第一个版本，不支持多数据源。

### 3.1.1、引入依赖

```xml
<dependency>
    <groupId>io.github.howinfun</groupId>
    <artifactId>winfun-pulsar-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 3.1.2、加入配置

```properties
pulsar.service-url=pulsar://127.0.0.1:6650
pulsar.tenant=winfun
pulsar.namespace=study
pulsar.operation-timeout=30
pulsar.io-threads=10
pulsar.listener-threads=10
```

### 3.1.3、发送消息

```java
/**
 * 发送消息
 * @author: winfun
 **/
@RestController
@RequestMapping("msg")
public class MessageController {

    @Autowired
    private PulsarTemplate pulsarTemplate;
    @Autowired
    private PulsarProperties pulsarProperties;

    /***
     * 往指定topic发送消息
     * @author winfun
     * @param topic topic
     * @param msg msg
     * @return {@link String }
     **/
    @GetMapping("/{topic}/{msg}")
    public String send(@PathVariable("topic") String topic,@PathVariable("msg") String msg) throws Exception {
        this.pulsarTemplate.createBuilder().persistent(Boolean.TRUE)
                .tenant(this.pulsarProperties.getTenant())
                .namespace(this.pulsarProperties.getNamespace())
                .topic(topic)
                .send(msg);
        return "success";
    }
}
```

### 3.1.4、消费消息

```java
/**
 * @author: winfun
 **/
@Slf4j
@PulsarListener(topics = {"test-topic2"},
                threadPool = @ThreadPool(
                                        coreThreads = 2,
                                        maxCoreThreads = 3, 
                                        threadPoolName = "test-thread-pool"))
public class ConsumerListener extends BaseMessageListener {

    /**
     * 消费消息
     * @param consumer 消费者
     * @param msg 消息
     */
    @Override
    protected void doReceived(Consumer<String> consumer, Message<String> msg) {
        log.info("成功消费消息：{}",msg.getValue());
        try {
            consumer.acknowledge(msg);
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }

    /***
     * 是否开启异步消费
     * @return {@link Boolean }
     **/
    @Override
    public Boolean enableAsync() {
        return Boolean.TRUE;
    }
}
```

## 3.2、winfun-pulsar-spring-boot-starter:1.2.0 版本

第二个版本，支持多数据源

### 3.2.1、引入依赖

```xml
<dependency>
    <groupId>io.github.howinfun</groupId>
    <artifactId>winfun-pulsar-spring-boot-starter</artifactId>
    <version>1.2.0</version>
</dependency>
```

### 3.2.2、加入配置

```properties
pulsar.serviceUrl.default=pulsar://127.0.0.1:6650
pulsar.tenant.default=winfun
pulsar.namespace.default=study
pulsar.enableTcpNoDelay.default=true
pulsar.operationTimeout.default=3
pulsar.ioThreads.default=5
pulsar.listenerThreads.default=5


# 如果有第二个数据源，可这样配置
pulsar.serviceUrl.second=pulsar://127.0.0.1:6651
pulsar.tenant.second=winfun
pulsar.namespace.second=study
.....
```

### 3.2.3、发送消息

和上面的第一个版本不同，我们不再需要指定tenant和namespace、只需要指定sourceName即可；如果sourceName也不指定，默认使用"default"。
```java
/**
 * 发送消息
 * @author: winfun
 **/
@RestController
@RequestMapping("msg")
public class MessageController {

    @Autowired
    private PulsarTemplate pulsarTemplate;

    /***
     * 往指定topic发送消息
     * @author winfun
     * @param topic topic
     * @param msg msg
     * @return {@link String }
     **/
    @GetMapping("/{sourceName}/{topic}/{msg}")
    public String send(@PathVariable("sourceName") String sourceName,@PathVariable("topic") String topic,@PathVariable("msg") String msg) throws Exception {
        this.pulsarTemplate.createBuilder().persistent(Boolean.TRUE)
                .sourceName(sourceName)
                .topic(topic)
                .send(msg);
        return "success";
    }
}
```

### 3.2.4、消费消息

消费者在第二版中，基本和第一版一致，只是多出了一个sourceName属性，可自行指定使用哪个数据源，如果不指定，默认使用"default"。
```java
/**
 * 消费消息
 * @author: winfun
 **/
@Slf4j
@PulsarListener(sourceName = "default",
                topics = {"test-topic2"},
                threadPool = @ThreadPool(
                                        coreThreads = 2,
                                        maxCoreThreads = 3,
                                        threadPoolName = "test-thread-pool"))
public class ConsumerListener extends BaseMessageListener {

    /**
     * 消费消息
     * @param consumer 消费者
     * @param msg 消息
     */
    @Override
    protected void doReceived(Consumer<String> consumer, Message<String> msg) {
        log.info("成功消费消息：{}",msg.getValue());
        try {
            consumer.acknowledge(msg);
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }

    /***
     * 是否开启异步消费
     * @return {@link Boolean }
     **/
    @Override
    public Boolean enableAsync() {
        return Boolean.TRUE;
    }
}
```

# 四、源码

源码就不放在这里分析了，大家可到[Github](https://github.com/Howinfun/winfun-pulsar-spring-boot-starter)上看看，如果有什么代码上面的建议或意见，欢迎大家提MR。
