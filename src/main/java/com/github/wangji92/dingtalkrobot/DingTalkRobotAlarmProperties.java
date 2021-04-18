package com.github.wangji92.dingtalkrobot;

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
