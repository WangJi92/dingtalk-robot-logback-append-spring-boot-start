package com.github.wangji92.dingtalkrobot.core;

import ch.qos.logback.classic.LoggerContext;
import com.github.wangji92.dingtalkrobot.DingTalkRobotAppendProperties;
import com.github.wangji92.dingtalkrobot.logback.append.DingTalkRobotAppend;
import com.github.wangji92.dingtalkrobot.logback.layout.DingTalkRobotLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 初始化构造 DingTalkRobotAppend
 *
 * @author 汪小哥
 * @date 17-04-2021
 */
@Slf4j
public class DingTalkRobotAppendBuilder {

    private static final String HTTP = "http";

    private DingTalkRobotAppendProperties dingTalkRobotAppendProperties;

    private ApplicationContext applicationContext;

    private LoggerContext loggerContext;

    public DingTalkRobotAppendBuilder(DingTalkRobotAppendProperties dingTalkRobotAppendProperties, ApplicationContext applicationContext) {
        this.dingTalkRobotAppendProperties = dingTalkRobotAppendProperties;
        this.applicationContext = applicationContext;
    }

    /**
     * 构建 DingTalkRobotAppend
     *
     * @return
     */
    public DingTalkRobotAppend buildDingTalkRobotAppend() {
        this.checkConfig();
        DingTalkRobotAppend dingTalkRobotAppend = new DingTalkRobotAppend();
        DingTalkRobotAppendProperties.DingTalkRobot robot = dingTalkRobotAppendProperties.getRobotConfig();
        dingTalkRobotAppend.setWebhook(robot.getWebhook());
        dingTalkRobotAppend.setSignSecret(robot.getSignSecret());
        dingTalkRobotAppend.setRateLimiterPermitsPerSecond(dingTalkRobotAppendProperties.getRobotConfig().getRateLimiterPermitsPerSecond());
        String title = robot.getRobotTitle();
        if (!StringUtils.hasText(title)) {
            title = "钉钉日志告警通知";
        }
        dingTalkRobotAppend.setRobotTitle(title);
        dingTalkRobotAppend.setContext(loggerContext);
        DingTalkRobotLayout layout = this.buildDingTalkRobotLayout(loggerContext, robot);
        layout.start();
        dingTalkRobotAppend.setLayout(layout);
        dingTalkRobotAppend.start();
        return dingTalkRobotAppend;
    }


    /**
     * 检查配置
     */
    private void checkConfig() {
        Assert.notNull(loggerContext, "logger context  must not be null");
        Assert.notNull(dingTalkRobotAppendProperties, "dingtalk dingTalkRobotAppendProperties config  must not be null");

        Assert.notNull(dingTalkRobotAppendProperties.getRobotConfig(), "dingtalk robot config  must not be null");
        Assert.hasText(dingTalkRobotAppendProperties.getRobotConfig().getWebhook(), "dingtalk robot config  must not be null");
    }

    /**
     * 构建 DingTalkRobotLayout
     *
     * @param loggerContext
     * @param robot
     * @return
     */
    private DingTalkRobotLayout buildDingTalkRobotLayout(LoggerContext loggerContext, DingTalkRobotAppendProperties.DingTalkRobot robot) {
        DingTalkRobotLayout layout = new DingTalkRobotLayout();
        layout.setContext(loggerContext);
        DingTalkRobotAppendProperties.ApplicationConfig applicationConfig = dingTalkRobotAppendProperties.getApplicationConfig();
        if (applicationConfig == null) {
            applicationConfig = new DingTalkRobotAppendProperties.ApplicationConfig();
        }
        String env = applicationConfig.getEnv();
        if (!StringUtils.hasText(env)) {
            env = applicationContext.getEnvironment().getProperty("spring.profiles.active");
        }
        layout.setEnv(env);
        String app = applicationConfig.getApplicationName();
        if (!StringUtils.hasText(env)) {
            app = applicationContext.getEnvironment().getProperty("spring.application.name");
        }
        layout.setApp(app);

        // 快捷链接 方便直接点击进入服务器
        DingTalkRobotAppendProperties.QuickLink quickLink = dingTalkRobotAppendProperties.getQuickLinkConfig();
        if (quickLink != null && StringUtils.hasText(quickLink.getClickDescription())) {
            layout.setClickDescription(quickLink.getClickDescription());
            if (StringUtils.hasText(quickLink.getClickUrl()) && quickLink.getClickUrl().contains(HTTP)) {
                layout.setClickUrl(quickLink.getClickUrl());
            }
        }
        return layout;
    }

    public DingTalkRobotAppendProperties getDingTalkRobotAppendProperties() {
        return dingTalkRobotAppendProperties;
    }

    public void setDingTalkRobotAppendProperties(DingTalkRobotAppendProperties dingTalkRobotAppendProperties) {
        this.dingTalkRobotAppendProperties = dingTalkRobotAppendProperties;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public LoggerContext getLoggerContext() {
        return loggerContext;
    }

    public void setLoggerContext(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }
}
