package com.github.wangji92.dingtalkrobot.core;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.EvaluatorFilter;
import com.github.wangji92.dingtalkrobot.DingTalkRobotAlarmProperties;
import com.github.wangji92.dingtalkrobot.logback.append.DingTalkRobotAppend;
import com.github.wangji92.dingtalkrobot.logback.layout.DingTalkRobotLayout;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

/**
 * 初始化构造 DingTalkRobotAppend
 *
 * @author 汪小哥
 * @date 17-04-2021
 */
@Slf4j
public class DingTalkRobotAppendConfigurator implements InitializingBean {

    public static final String HTTP = "http";
    private DingTalkRobotAlarmProperties dingTalkRobotAlarmProperties;

    private ApplicationContext applicationContext;

    /**
     * 异步的append
     */
    private AsyncAppender asyncAppender;

    public DingTalkRobotAppendConfigurator(DingTalkRobotAlarmProperties dingTalkRobotAlarmProperties, ApplicationContext applicationContext, AsyncAppender asyncAppender) {
        this.dingTalkRobotAlarmProperties = dingTalkRobotAlarmProperties;
        this.applicationContext = applicationContext;
        this.asyncAppender = asyncAppender;
    }

    /**
     * 初始化构造 将 DingTalkRobotAppend 添加到 logger 中
     *
     * @see org.springframework.boot.logging.logback.LogbackLoggingSystemLogbackLoggingSystem#getLoggerContext()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (!(factory instanceof LoggerContext)) {
            log.warn("LoggerFactory is not a Logback LoggerContext");
            return;
        }
        LoggerContext loggerContext = (LoggerContext) factory;
        DingTalkRobotAlarmProperties.LogConfig logConfig = dingTalkRobotAlarmProperties.getLogConfig();
        if (logConfig.getLogLevel() == null) {
            log.warn("dingtalk robot log config log level must not be null");
            return;
        }

        String appendLoggerNames = logConfig.getAppendLoggerNames();
        if (!StringUtils.hasText(appendLoggerNames)) {
            log.warn("dingtalk robot not config logger name");
            return;
        }
        DingTalkRobotAppend dingTalkRobotAppend = new DingTalkRobotAppend();
        DingTalkRobotAlarmProperties.DingTalkRobot robot = dingTalkRobotAlarmProperties.getRobotConfig();
        if (robot == null || !StringUtils.hasText(robot.getWebhook())) {
            log.warn("dingtalk robot not config robot info");
            return;
        }

        dingTalkRobotAppend.setWebhook(robot.getWebhook());
        dingTalkRobotAppend.setSignSecret(robot.getSignSecret());
        dingTalkRobotAppend.setContext(loggerContext);

        DingTalkRobotLayout layout = new DingTalkRobotLayout();
        layout.setContext(loggerContext);
        DingTalkRobotAlarmProperties.ApplicationConfig applicationConfig = dingTalkRobotAlarmProperties.getApplicationConfig();
        if (applicationConfig == null) {
            applicationConfig = new DingTalkRobotAlarmProperties.ApplicationConfig();
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

        String title = robot.getRobotTitle();
        if (!StringUtils.hasText(title)) {
            title = "钉钉日志告警通知";
        }
        layout.setPresentationHeader(title);

        // 快捷链接 方便直接点击进入服务器
        DingTalkRobotAlarmProperties.QuickLink quickLink = dingTalkRobotAlarmProperties.getQuickLinkConfig();
        if (quickLink != null && StringUtils.hasText(quickLink.getClickDescription())) {
            layout.setClickDescription(quickLink.getClickDescription());
            if (StringUtils.hasText(quickLink.getUrlAppendIp()) && quickLink.getUrlAppendIp().contains(HTTP)) {
                layout.setUrlAppendIp(quickLink.getUrlAppendIp());
            } else if (StringUtils.hasText(quickLink.getUrlAppendApplicationName()) && quickLink.getUrlAppendApplicationName().contains(HTTP)) {
                layout.setUrlAppendApplicationName(quickLink.getUrlAppendApplicationName());
            }
        }

        layout.start();
        dingTalkRobotAppend.setLayout(layout);


        //伐值以上的日志都会打印 http://logback.qos.ch/manual/filters.html#ThresholdFilter
        ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setLevel(logConfig.getLogLevel().name());
        thresholdFilter.start();
        asyncAppender.addFilter(thresholdFilter);
        asyncAppender.setContext(loggerContext);

        if (StringUtils.hasText(logConfig.getLogKeyWord())) {
            // 表达式实践  http://logback.qos.ch/manual/filters.html#EvaluatorFilter
            // 可以使用 event、message、logger、loggerContext、mdc、throwable、throwableProxy 等关键字
            EvaluatorFilter<ILoggingEvent> evaluatorFilter = new EvaluatorFilter<ILoggingEvent>();
            JaninoEventEvaluator eventEvaluator = new JaninoEventEvaluator();
            // 需要存在关键字才打印
            eventEvaluator.setExpression("formattedMessage.contains(" + logConfig.getLogKeyWord() + ")");
            evaluatorFilter.setEvaluator(eventEvaluator);
            evaluatorFilter.start();
            asyncAppender.addFilter(thresholdFilter);
        }
        dingTalkRobotAppend.start();

        // 异步append 是一个 代理
        asyncAppender.addAppender(dingTalkRobotAppend);
        asyncAppender.start();

        dingTalkRobotAppend.start();
        for (String loggerName : appendLoggerNames.split(",")) {
            Logger logger = loggerContext.getLogger(loggerName);
            if (logger == null) {
                log.warn("dingtalk alarm logger name ={} not found", loggerName);
                continue;
            }
            logger.addAppender(asyncAppender);
        }
    }

    public DingTalkRobotAlarmProperties getDingTalkRobotAlarmProperties() {
        return dingTalkRobotAlarmProperties;
    }

    public void setDingTalkRobotAlarmProperties(DingTalkRobotAlarmProperties dingTalkRobotAlarmProperties) {
        this.dingTalkRobotAlarmProperties = dingTalkRobotAlarmProperties;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public AsyncAppender getAsyncAppender() {
        return asyncAppender;
    }

    public void setAsyncAppender(AsyncAppender asyncAppender) {
        this.asyncAppender = asyncAppender;
    }
}
