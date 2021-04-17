package com.github.wangji92.dingtalkrobot;

import ch.qos.logback.classic.AsyncAppender;
import com.github.wangji92.dingtalkrobot.core.DingTalkRobotAppendConfigurator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static ch.qos.logback.core.AsyncAppenderBase.DEFAULT_MAX_FLUSH_TIME;
import static ch.qos.logback.core.AsyncAppenderBase.DEFAULT_QUEUE_SIZE;

/**
 * @author 汪小哥
 * @date 17-04-2021
 */
@Configuration
@ConditionalOnClass(name = "ch.qos.logback.classic.LoggerContext")
@ConditionalOnProperty(prefix = "spring.dingtalk.alarm", value = "enable", havingValue = "true", matchIfMissing = true)
@ComponentScan(value = "com.github.wangji92.dingtalkrobot")
@Slf4j
public class DingTalkRobotAlarmAutoConfiguration {


    @Bean
    public DingTalkRobotAppendConfigurator dingTalkRobotAppendConfigurator(DingTalkRobotAlarmProperties dingTalkRobotAlarmProperties, ApplicationContext applicationContext) {
        return new DingTalkRobotAppendConfigurator(dingTalkRobotAlarmProperties, applicationContext, buildAsyncAppender());
    }

    @Bean
    public AsyncAppender buildAsyncAppender() {
        AsyncAppender asyncAppender = new AsyncAppender();
        // http://logback.qos.ch/manual/appenders.html#AsyncAppender
        // 提取调用方数据可能相当昂贵。
        // 若要提高性能，默认情况下，当事件添加到事件队列时，不会提取与事件关联的调用方数据。
        // 默认情况下，只有“廉价”数据，如线程名和 都被复制了。
        asyncAppender.setIncludeCallerData(true);

        //blockingQueue长度决定了队列能放多少信息，在默认的配置下，如果blockingQueue放满了，后续想要输出日志的线程会被阻塞，直到Worker线程处理掉队列中的信息为止。
        // 根据实际情况适当调整队列长度，可以防止线程被阻塞。
        asyncAppender.setQueueSize(DEFAULT_QUEUE_SIZE);

        // 默认情况下，当阻塞队列剩余20% 的容量时，它将删除 TRACE、 DEBUG 和 INFO 级别的事件，只保留 WARN 和 ERROR 级别的事件。
        // 为了保留所有的事件，设置discardingThreshold 丢弃阈值 to 0
        // super.setDiscardingThreshold();


        //如果配置neverBlock=true，当队列满了之后，后面阻塞的线程想要输出的消息就直接被丢弃，从而线程不会阻塞。
        // 这个配置用于线程很重要，不能卡顿，而且日志又不是很重要的场景，因为很有可能会丢日志
        asyncAppender.setNeverBlock(true);

        //Depending on the queue depth and latency to the referenced appender,
        // the AsyncAppender may take an unacceptable amount of time to fully flush the queue.
        // When the LoggerContext is stopped,
        // the AsyncAppender stop method waits up to this timeout for the worker thread to complete.
        // Use maxFlushTime to specify a maximum queue flush timeout in milliseconds.
        // Events that cannot be processed within this window are discarded.
        // Semantics of this value are identical to that of Thread.join(long).
        asyncAppender.setMaxFlushTime(DEFAULT_MAX_FLUSH_TIME);

        asyncAppender.setName("dingTalkRobotAsync");
        return asyncAppender;
    }

}
