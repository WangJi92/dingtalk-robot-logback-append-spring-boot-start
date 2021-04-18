package com.github.wangji92.dingtalkrobot;

import ch.qos.logback.core.AsyncAppenderBase;
import com.github.wangji92.dingtalkrobot.core.DingTalkRobotLogbackAlarmBootstrap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Configuration;

/**
 * 钉钉告警配置
 *
 * @author 汪小哥
 * @date 17-04-2021
 */
@ConfigurationProperties(prefix = "spring.dingtalk.alarm")
@Data
@Configuration
public class DingTalkRobotAlarmProperties {
    /**
     * 是否启用
     */
    private boolean enable = true;
    /**
     * 钉钉机器人的配置
     */
    private DingTalkRobot robotConfig;

    /**
     * 应用配置
     */
    private ApplicationConfig applicationConfig;

    /**
     * 日志配置
     */
    private LogConfig logConfig;

    /**
     * 查看详情
     */
    private QuickLink quickLinkConfig;


    @Data
    public static class ApplicationConfig {
        /**
         * 当前环境 not set is  Environment=>spring.profiles.active
         */
        private String env = "";
        /**
         * 应用名称  not set is  Environment=>spring.application.name
         */
        private String applicationName = "";
    }

    @Data
    public static class LogConfig {
        /**
         * 当前level 等级之上的都打印
         */
        private LogLevel logLevel = LogLevel.ERROR;
        /**
         * 消息中有关键词才钉钉通知
         */
        private String logKeyWord = "";

        /**
         * 添加到哪些 logger name  逗号分割
         */
        private String appendLoggerNames = "root";

        /**
         * blockingQueue长度决定了队列能放多少信息，在默认的配置下，如果blockingQueue放满了，后续想要输出日志的线程会被阻塞，
         * 直到Worker线程处理掉队列中的信息为止。
         * 根据实际情况适当调整队列长度，可以防止线程被阻塞。
         * {@link DingTalkRobotLogbackAlarmBootstrap#buildAsyncAppender()}
         */
        private Integer asyncAppenderQueueSize = 256;

        /**
         * 这里可能会丢日志 {@link AsyncAppenderBase#put(Object) offer 方法没有足够空间直接扔掉}
         * 如果配置neverBlock=true，当队列满了之后，后面阻塞的线程想要输出的消息就直接被丢弃，从而线程不会阻塞。
         * 这个配置用于线程很重要，不能卡顿，而且日志又不是很重要的场景，因为很有可能会丢日志
         * {@link DingTalkRobotLogbackAlarmBootstrap#buildAsyncAppender()}
         */
        private Boolean asyncAppenderNeverBlock = true;

        /**
         * 提取调用方数据可能相当昂贵。
         * 若要提高性能，默认情况下，当事件添加到事件队列时，不会提取与事件关联的调用方数据。
         * 默认情况下，只有“廉价”数据，如线程名和 都被复制了。
         * {@link DingTalkRobotLogbackAlarmBootstrap#buildAsyncAppender()}
         */
        private Boolean asyncAppenderIncludeCallerData = true;
    }

    /**
     * 钉钉的配置
     */
    @Data
    public static class DingTalkRobot {
        /**
         * 钉钉机器人配置 webhook
         */
        private String webhook;
        /**
         * 钉钉机器人加签关键字
         */
        private String signSecret;
        /**
         * 钉钉发送消息头
         */
        private String robotTitle = "钉钉日志告警通知";
    }

    /**
     * 打印的日志追加一个详情
     */
    @Data
    public static class QuickLink {
        /**
         * 点击查看详情
         */
        private String clickDescription = "点击查看详情";
        /**
         * 支持变量 ip 或者 app (应用名称)
         * https://kaifa.baidu.com/searchPage?wd={ip}
         */
        private String clickUrl;
    }


}
