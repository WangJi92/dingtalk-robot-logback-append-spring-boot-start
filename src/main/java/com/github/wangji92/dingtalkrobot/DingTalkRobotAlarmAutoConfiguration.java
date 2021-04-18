package com.github.wangji92.dingtalkrobot;

import com.github.wangji92.dingtalkrobot.core.DingTalkRobotLogbackAlarmBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
    public DingTalkRobotLogbackAlarmBootstrap dingTalkRobotLogbackAlarmBootstrap() {
        return new DingTalkRobotLogbackAlarmBootstrap();
    }

}
