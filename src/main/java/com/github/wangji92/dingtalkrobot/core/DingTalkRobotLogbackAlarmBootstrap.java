package com.github.wangji92.dingtalkrobot.core;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.github.wangji92.dingtalkrobot.DingTalkRobotAlarmProperties;
import com.github.wangji92.dingtalkrobot.logback.append.DingTalkRobotAppend;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

import static ch.qos.logback.core.AsyncAppenderBase.DEFAULT_MAX_FLUSH_TIME;

/**
 * @author 汪小哥
 * @date 18-04-2021
 */
@Slf4j
public class DingTalkRobotLogbackAlarmBootstrap {

    /**
     * logback loggerContext
     */
    private LoggerContext loggerContext = null;

    @Autowired
    private DingTalkRobotAlarmProperties dingTalkRobotAlarmProperties;

    @Autowired
    private ApplicationContext applicationContext;


    @PostConstruct
    public void init() {
        this.initLoggerContext();
        this.configCheck();

        DingTalkRobotAppend dingTalkRobotAppend = this.buildDingTalkRobotAppend();


        ThresholdFilter thresholdFilter = this.buildThresholdFilter();
        EvaluatorFilter<ILoggingEvent> evaluatorFilter = this.buildJaninoEvaluatorFilter();


        AsyncAppender asyncAppender = this.buildAsyncAppender();
        asyncAppender.addAppender(dingTalkRobotAppend);
        asyncAppender.addFilter(thresholdFilter);

        if (evaluatorFilter != null) {
            asyncAppender.addFilter(evaluatorFilter);
        }

        this.addLoggerNameDingTalkRobotAppender(asyncAppender);

        asyncAppender.start();
    }

    /**
     * 构建 DingTalkRobotAppend
     *
     * @return
     */
    @Bean
    public DingTalkRobotAppend buildDingTalkRobotAppend() {
        DingTalkRobotAppendBuilder dingTalkRobotAppendBuilder = new DingTalkRobotAppendBuilder(dingTalkRobotAlarmProperties, applicationContext);
        dingTalkRobotAppendBuilder.setLoggerContext(loggerContext);
        return dingTalkRobotAppendBuilder.buildDingTalkRobotAppend();
    }

    /**
     * 构建 异步的 AsyncAppender 包装 DingTalkRobotAppend
     *
     * @return
     */
    @Bean
    public AsyncAppender buildAsyncAppender() {
        AsyncAppender asyncAppender = new AsyncAppender();

        DingTalkRobotAlarmProperties.LogConfig logConfig = dingTalkRobotAlarmProperties.getLogConfig();

        asyncAppender.setContext(loggerContext);
        // http://logback.qos.ch/manual/appenders.html#AsyncAppender
        // 提取调用方数据可能相当昂贵。
        // 若要提高性能，默认情况下，当事件添加到事件队列时，不会提取与事件关联的调用方数据。
        // 默认情况下，只有“廉价”数据，如线程名和 都被复制了。
        asyncAppender.setIncludeCallerData(logConfig.getAsyncAppenderIncludeCallerData());

        //blockingQueue长度决定了队列能放多少信息，在默认的配置下，如果blockingQueue放满了，后续想要输出日志的线程会被阻塞，直到Worker线程处理掉队列中的信息为止。
        // 根据实际情况适当调整队列长度，可以防止线程被阻塞。
        asyncAppender.setQueueSize(logConfig.getAsyncAppenderQueueSize());

        // 默认情况下，当阻塞队列剩余20% 的容量时，它将删除 TRACE、 DEBUG 和 INFO 级别的事件，只保留 WARN 和 ERROR 级别的事件。
        // 为了保留所有的事件，设置discardingThreshold 丢弃阈值 to 0
        // super.setDiscardingThreshold();


        //如果配置neverBlock=true，当队列满了之后，后面阻塞的线程想要输出的消息就直接被丢弃，从而线程不会阻塞。
        // 这个配置用于线程很重要，不能卡顿，而且日志又不是很重要的场景，因为很有可能会丢日志
        asyncAppender.setNeverBlock(logConfig.getAsyncAppenderNeverBlock());

        //Depending on the queue depth and latency to the referenced appender,
        // the AsyncAppender may take an unacceptable amount of time to fully flush the queue.
        // When the LoggerContext is stopped,
        // the AsyncAppender stop method waits up to this timeout for the worker thread to complete.
        // Use maxFlushTime to specify a maximum queue flush timeout in milliseconds.
        // Events that cannot be processed within this window are discarded.
        // Semantics of this value are identical to that of Thread.join(long).
        asyncAppender.setMaxFlushTime(DEFAULT_MAX_FLUSH_TIME);

        asyncAppender.setName("dRobotAsync");
        return asyncAppender;
    }

    /**
     * 添加 logger name 到 dingtalk robot append
     *
     * @param asyncAppender
     */
    private void addLoggerNameDingTalkRobotAppender(AsyncAppender asyncAppender) {
        DingTalkRobotAlarmProperties.LogConfig logConfig = dingTalkRobotAlarmProperties.getLogConfig();
        for (String loggerName : logConfig.getAppendLoggerNames()) {
            Logger logger = loggerContext.getLogger(loggerName);
            if (logger == null) {
                log.warn("dingtalk alarm logger name ={} not found", loggerName);
                continue;
            }
            logger.addAppender(asyncAppender);
        }
    }

    /**
     * 配置检查
     */
    private void configCheck() {
        Assert.notNull(dingTalkRobotAlarmProperties.getLogConfig(),
                "dingtalk robot log config  must not be null");
        Assert.notNull(dingTalkRobotAlarmProperties.getLogConfig().getLogLevel(),
                "dingtalk robot log config log level must not be null");
        Assert.notEmpty(dingTalkRobotAlarmProperties.getLogConfig().getAppendLoggerNames(),
                "dingtalk robot not config logger name[eg: root,org.springframework.boot]");

        Integer asyncAppenderQueueSize = dingTalkRobotAlarmProperties.getLogConfig().getAsyncAppenderQueueSize();
        Assert.isTrue(asyncAppenderQueueSize != null && asyncAppenderQueueSize > 0,
                "dingtalk robot log config asyncAppender QueueSize must not be null and Greater than 0");

        Assert.isTrue(dingTalkRobotAlarmProperties.getLogConfig().getAsyncAppenderNeverBlock() != null,
                "dingtalk robot log config asyncAppender neverBlock must not be null");

        Assert.isTrue(dingTalkRobotAlarmProperties.getLogConfig().getAsyncAppenderIncludeCallerData() != null,
                "dingtalk robot log config asyncAppender includeCallerData must not be null");
    }

    /**
     * 初始化日志上下文 @see org.springframework.boot.logging.logback.LogbackLoggingSystemLogbackLoggingSystem#getLoggerContext()
     */
    private void initLoggerContext() {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (!(factory instanceof LoggerContext)) {
            throw new IllegalArgumentException("LoggerFactory is not a Logback LoggerContext");
        }
        this.loggerContext = (LoggerContext) factory;
    }

    /**
     * 构建表达式 过滤器
     *
     * @return
     */
    private EvaluatorFilter<ILoggingEvent> buildJaninoEvaluatorFilter() {
        DingTalkRobotAlarmProperties.LogConfig logConfig = dingTalkRobotAlarmProperties.getLogConfig();
        if (!CollectionUtils.isEmpty(logConfig.getLogKeyWords())) {
            StringBuffer buffer = new StringBuffer("return ");
            for (int index = 0; index < logConfig.getLogKeyWords().size(); index++) {
                String keyword = logConfig.getLogKeyWords().get(index);
                if (index != 0 && index != logConfig.getLogKeyWords().size()) {
                    buffer.append(" || ");
                }
                buffer.append(" formattedMessage.contains(\"").append(keyword).append("\")");

            }
            buffer.append(";");
            return getEvaluatorFilter(buffer.toString());
        } else if (StringUtils.hasText(logConfig.getKewWordExpression())) {
            return getEvaluatorFilter(logConfig.getKewWordExpression());
        }
        return null;
    }


    /**
     * 构建表达式 DEBUG INFO WARN ERROR event message formattedMessage logger loggerContext level
     * timeStamp  marker mdc throwableProxy throwable 等等
     *
     * @param expression
     * @return
     */
    private EvaluatorFilter<ILoggingEvent> getEvaluatorFilter(String expression) {
        // 表达式实践  http://logback.qos.ch/manual/filters.html#EvaluatorFilter
        // 可以使用 event、message、logger、loggerContext、mdc、throwable、throwableProxy 等关键字
        EvaluatorFilter<ILoggingEvent> evaluatorFilter = new EvaluatorFilter<ILoggingEvent>();
        JaninoEventEvaluator eventEvaluator = new JaninoEventEvaluator();
        // 需要存在关键字才打印
        eventEvaluator.setExpression(expression);
        evaluatorFilter.setEvaluator(eventEvaluator);
        eventEvaluator.setContext(loggerContext);

        evaluatorFilter.setOnMatch(FilterReply.ACCEPT);
        evaluatorFilter.setOnMismatch(FilterReply.DENY);
        eventEvaluator.start();
        evaluatorFilter.start();
        return evaluatorFilter;
    }

    /**
     * 构建拦截器 伐值以上的日志都会打印 http://logback.qos.ch/manual/filters.html#ThresholdFilter
     *
     * @return
     */
    private ThresholdFilter buildThresholdFilter() {
        DingTalkRobotAlarmProperties.LogConfig logConfig = dingTalkRobotAlarmProperties.getLogConfig();
        ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setLevel(logConfig.getLogLevel().name());
        thresholdFilter.start();
        return thresholdFilter;
    }


}
