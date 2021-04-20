## 1、解决什么问题
基于钉钉机器人的 logback-append-spring-boot-start，可以通过自定义 logback-append 然后通过钉钉机器人的API 推送到钉钉群，解决线下或者线上 针对错误场景的消息推送,方便开发者快速发现问题解决问题，最终的目的是治理日志，减少异常日志，开发环境快速发现错误。
## 2、效果图
![image.png](https://cdn.nlark.com/yuque/0/2021/png/171220/1618933321858-9ac5de6b-73da-4af6-98c7-bb521514a939.png#clientId=u89e52154-1e75-4&from=paste&height=286&id=uac5f17fb&margin=%5Bobject%20Object%5D&name=image.png&originHeight=571&originWidth=491&originalType=binary&size=72812&status=done&style=none&taskId=u5ef3995d-4536-41ef-8e74-37ccdca8605&width=245.5)![image.png](https://cdn.nlark.com/yuque/0/2021/png/171220/1618933337682-c6e3b4b8-5d53-4bc4-8017-47f94c5f07b3.png#clientId=u89e52154-1e75-4&from=paste&height=276&id=ua757968d&margin=%5Bobject%20Object%5D&name=image.png&originHeight=552&originWidth=408&originalType=binary&size=119640&status=done&style=none&taskId=uacc63e92-0820-4fbf-aa6f-22e12250631&width=204)
## 3、使用
### 3.1 依赖 
```xml
<guava-version>30.1.1-jre</guava-version>
<alibaba-dingtalk-service-sdk-version>1.0.1</alibaba-dingtalk-service-sdk-version>
<janino-version>3.1.3</janino-version>

 <!-- 限流使用 -->
<dependency>
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>
  <version>${guava-version}</version>
  <scope>provided</scope>
</dependency>

 <!-- 钉钉机器人SDK -->
<dependency>
  <groupId>com.aliyun</groupId>
  <artifactId>alibaba-dingtalk-service-sdk</artifactId>
  <version>${alibaba-dingtalk-service-sdk-version}</version>
</dependency>

 <!-- logback 表达式处理 -->
<dependency>
  <groupId>org.codehaus.janino</groupId>
  <artifactId>janino</artifactId>
  <version>${janino-version}</version>
</dependency>

.... logback的一些核心包 引入spring 基本上都有
```
### 3.2 自动装配
自动装配模式 不需要配置logback的xml通过编程的方式植入钉钉append。
```
# 自动配置打开 
spring.dingtalk.logback.append.enable=true

# 告警 应用相关配置 【应用名称 、当前环境】
spring.dingtalk.logback.append.application-config.application-name=${spring.application.name}
spring.dingtalk.logback.append.application-config.env=测试环境

## 日志 通知范围配置 
spring.dingtalk.logback.append.log-config.append-logger-names[0]=root

## 处理ERROR 日志 【必须配置】
spring.dingtalk.logback.append.log-config.log-level=ERROR

## 关键字过滤 两种方式 1、关键字 2、表达式 http://logback.qos.ch/manual/filters.html#EvaluatorFilter
## 可以都不配置 只处理loglevel 的过滤
#spring.dingtalk.logback.append.log-config.log-key-words[0]=dingding
#spring.dingtalk.logback.append.log-config.log-key-words[1]=wangji
spring.dingtalk.logback.append.log-config.kew-word-expression=return  formattedMessage.contains("dingding") ||  formattedMessage.contains("wangji");

## 异步队列的配置 【需要注意队列的长度 可能丢日志】
spring.dingtalk.logback.append.log-config.async-appender-queue-size=256
spring.dingtalk.logback.append.log-config.async-appender-never-block=false
spring.dingtalk.logback.append.log-config.async-appender-include-caller-data=true

## 钉钉机器人告警配置 
spring.dingtalk.logback.append.robot-config.robot-title=钉钉日志告警
spring.dingtalk.logback.append.robot-config.webhook=https://oapi.dingtalk.com/robot/send?access_token=34b047c35744144f1433eb02fda6125ef850319e280ea4af6fe6e935ed7847df
spring.dingtalk.logback.append.robot-config.sign-secret=SEC5c6533fc0e86b4f89f4dae5b3d7ee7c42c9d968e360915a21d8c4263ce39c9ca

## 钉钉告警发送速度限制 1分钟20次 guava 限制一下 1/3.5 ~=0.2875
spring.dingtalk.logback.append.robot-config.rate-limiter-permits-per-second=0.2875

# 钉钉通知 快捷链接配置
spring.dingtalk.logback.append.quick-link-config.click-description=点击查看详情

## 链接地址支持配置各种变量[localIp]、[hostname] 等等 https://kaifa.baidu.com/searchPage?w=[localIp]
## 发现问题一键进入服务器、k8s集群的链接地址
spring.dingtalk.logback.append.quick-link-config.click-url=https://kaifa.baidu.com/searchPage?w=[localIp]
```


### 3.2 手动配置xml
#### 3.2.1  pattern layout 定义的格式
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="60 seconds" debug="true">
    <!--    spring 默认的-->
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <include resource="com/github/wangji92/dingtalkrobot/logback-dingtalk-robot-base.xml"/>

    <!--    layout 的格式 替换默认的 这里使用 spring.dingtalk.logback.append.quick-link-config.click-url=https://kaifa.baidu.com/searchPage?w=[localIp] 动态的配置 可以跟进需求 修改为hostname、app 等等变量-->
    <property name="DINGTALK_ROBOT_LOG_PATTERN" value="${CBT_CONVERT_DINGTALK_ROBOT_LOG_PATTERN}"/>
    <!--    替换掉默认的DINGTALK_ROBOT_LOG_PATTERN，没有打印 链接详情的那个-->

    <!--    钉钉机器人告警处理 使用pattern layout -->
    <include resource="com/github/wangji92/dingtalkrobot/logback-dingtalk-robot-pattern-layout.xml"/>


    <!-- ConsoleAppender 控制台输出日志 -->
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <root>
        <level value="info"/>
        <!-- 控制台输出 -->
        <appender-ref ref="console"/>
        <!--        钉钉告警处理输出-->
        <appender-ref ref="asyncDingtalkPatternLayoutEncoderAppend"/>
    </root>
</configuration>
```
#### 3.2.2  手动编程定义的格式 [学习自己玩一下]
com.github.wangji92.dingtalkrobot.logback.layout.DingTalkRobotLayout
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="60 seconds" debug="true">
    <!--    spring 默认的-->
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <include resource="com/github/wangji92/dingtalkrobot/logback-dingtalk-robot-base.xml"/>

    <!--    钉钉机器人告警处理 使用自定义的layout 可以了解一下 如何处理的 推荐使用 pattern-layout 更加灵活-->
    <include resource="com/github/wangji92/dingtalkrobot/logback-dingtalk-robot-custom-layout.xml"/>

    <!--    这里覆盖spring 定义的变量-->
    <property name="CONSOLE_LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger - %msg%n"/>

    <!-- ConsoleAppender 控制台输出日志 -->
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <root>
        <level value="info"/>
        <!-- 控制台输出 -->
        <appender-ref ref="console"/>
        <!--        钉钉告警处理输出-->
        <appender-ref ref="asyncDingTalkCustomLayoutAppend"/>
    </root>
</configuration>
```


