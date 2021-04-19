## 1、变量使用
### 1.1 使用变量添加默认值
```xml
${xxxLevel:-ERROR}
```
### 1.2 直接自定义变量
比如：日志需要获取本地的Ip 地址写在pattern 中 可以自己定义变量，比较灵活，比如获取本地Ip 
[http://logback.qos.ch/manual/configuration.html#definingPropsOnTheFly](http://logback.qos.ch/manual/configuration.html#definingPropsOnTheFly)
```java
package ch.qos.logback.core.spi;

public interface PropertyDefiner extends ContextAware {

    /**
     * Get the property value, defined by this property definer
     * 
     * @return defined property value
     */
    String getPropertyValue();
}
```
```xml
<configuration>

  <define name="rootLevel" class="a.class.implementing.PropertyDefiner">
    <shape>round</shape>
    <color>brown</color>
    <size>24</size>
  </define>

  <root level="${rootLevel}"/>
</configuration>
```
### 1.3 直接使用系统变量
eg 比如要获取 env | hostname, HOME 等等环境变量直接当前属性使用
```xml
${HOME}%n  ${hostname}%n
```

###  1.4 直接定义属性不需要编码获取

```xml
<property name="APP_NAME" value="DEMO"/>
<property name="LOG_PATH" value="${user.home}/${APP_NAME}/logs"/>
<property name="LOG_FILE" value="${LOG_PATH}/application.log"/>
```

## 2、使用表达式


比如说某个过滤器需要在xx条件才使用，比如 线上环境打开这个开关 
需要引入包
```xml
<dependency>
  <groupId>org.codehaus.janino</groupId>
  <artifactId>janino</artifactId>
  <version>3.1.3</version>
</dependency>
```
### if else 
[http://logback.qos.ch/manual/configuration.html#conditional](http://logback.qos.ch/manual/configuration.html#conditional)
```xml
<configuration scan="true" scanPeriod="60 seconds" debug="false">
  <property name ="notifySwitch" value="open"/>
  <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>
        <!-- 设置日志输出格式 -->
        %d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger - %msg%n
      </pattern>
    </encoder>
    <if condition='property("notifySwitch").length()>0'>
      <then>
        <filter class="ch.qos.logback.core.filter.EvaluatorFilter">
          <evaluator class="ch.qos.logback.classic.boolex.JaninoEventEvaluator">
            <expression>return  formattedMessage.contains("通知");</expression>
          </evaluator>
          <!-- 用于配置符合过滤条件的操作 -->
          <onMatch>ACCEPT</onMatch>
          <!-- 用于配置不符合过滤条件的操作 -->
          <onMismatch>DENY</onMismatch>
        </filter>
      </then>
    </if>
  </appender>
  <root>
     <appender-ref ref="console"/>   
   </root>
</configuration>
```
### evaluator
评估表达式  在logback 中大量使用这个表达式 
[http://logback.qos.ch/manual/layouts.html#Evaluators](http://logback.qos.ch/manual/layouts.html#Evaluators)


xEx{depth, evaluator-1, ..., evaluator-n}  这里学到了一招 可以减少异常的深度，可以根据异常的信息进行判断是否打印


eg pattern 变量中年使用


[http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout](http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout)
caller 只要满足条件 && 才答应出来
```xml
<configuration>
  <evaluator name="DISP_CALLER_EVAL">
    <expression>logger.contains("chapters.layouts") &amp;&amp; \
      message.contains("who calls thee")</expression>
  </evaluator>

  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender"> 
    <encoder>
      <pattern>
        %-4relative [%thread] %-5level - %msg%n %caller{2, DISP_CALLER_EVAL}
      </pattern>
    </encoder>
  </appender>

  <root level="DEBUG"> 
    <appender-ref ref="STDOUT" /> 
  </root>
</configuration>
```
## 3、自定义监听器
自定义监听器可以实现对于变量的注册、操作logcontext 等等! 有非常多的好处
[http://logback.qos.ch/manual/configuration.html#contextListener](http://logback.qos.ch/manual/configuration.html#contextListener)
```xml
<configuration debug="true">
  <contextListener class="ch.qos.logback.classic.jul.LevelChangePropagator">
    <resetJUL>true</resetJUL>
  </contextListener>
  ....
</configuration>
```
```xml
public interface LoggerContextListener {

    /**
     * Some listeners should not be removed when the LoggerContext is
     * reset. Such listeners are said to be reset resistant.
     * @return whether this listener is reset resistant or not.
     */
    boolean isResetResistant();

    void onStart(LoggerContext context);

    void onReset(LoggerContext context);

    void onStop(LoggerContext context);

    void onLevelChange(Logger logger, Level level);
}
```
## 4、善用include
logback 定义文件一大堆，可以通过include 的方法引入 比如定义框架 可以减少一些重复性的工作、引入即可。
结合if else 、变量 可以简单的编程都可以。
[http://logback.qos.ch/manual/configuration.html#fileInclusion](http://logback.qos.ch/manual/configuration.html#fileInclusion)
```xml
<configuration>
<!--一般spring 工程在resource 中创建 使用 resource 即可-->
<!--<include resource="includedConfig.xml"/>-->
  <include file="src/main/java/chapters/configuration/includedConfig.xml"/>

  <root level="DEBUG">
    <appender-ref ref="includedConsole" />
  </root>

</configuration>
```
```xml
<included>
  <appender name="includedConsole" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>"%d - %m%n"</pattern>
    </encoder>
  </appender>
</included>
```
## 5、自定义转换器
layout 中pattern中定义的转换器 比如thread 、level 你也可以自定义一个转换器去处理
[http://logback.qos.ch/manual/layouts.html#customConversionSpecifier](http://logback.qos.ch/manual/layouts.html#customConversionSpecifier)
## 6、InvokingJoranConfiguratordirectly
直接调用启动logback ，比如spring 如何实现的呢？
org.springframework.boot.logging.logback.LogbackLoggingSystem#configureByResourceUrl


[http://logback.qos.ch/manual/configuration.html#joranDirectly](http://logback.qos.ch/manual/configuration.html#joranDirectly)
Logback relies on a configuration library called Joran, part of logback-core. Logback's default configuration mechanism invokesJoranConfiguratoron the default configuration file it finds on the class path. If you wish to override logback's default configuration mechanism for whatever reason, you can do so by invokingJoranConfiguratordirectly. The next application,_MyApp3_, invokes JoranConfigurator on a configuration file passed as a parameter.
## 7、自定义Action
比如spring 中 
[http://logback.qos.ch/manual/onJoran.html#action](http://logback.qos.ch/manual/onJoran.html#action)


org.springframework.boot.logging.logback.SpringProfileAction
org.springframework.boot.logging.logback.SpringPropertyAction
```xml
<springProfile name="production">
<springProperty scope="context" name="appName" source="spring.application.name"
                    defaultValue="未定义"/>
```
