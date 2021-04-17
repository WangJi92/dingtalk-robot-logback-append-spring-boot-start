package com.github.wangji92.dingtalkrobot.logback.layout;

import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.pattern.MethodOfCallerConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import com.github.wangji92.dingtalkrobot.utils.IpUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * 解析处理日志
 *
 * @author 汪小哥
 * @date 17-04-2021
 */
public class DingTalkRobotLayout extends LayoutBase<ILoggingEvent> {
    /**
     * 环境
     */
    private String env;
    /**
     * 应用名称
     */
    private String app;
    /**
     * ip 地址
     */
    private String ip = "";

    /**
     * 钉钉通知 快捷链接
     */
    private String clickDescription;
    /**
     * 快捷链接 后面追加ip
     */
    private String urlAppendIp;

    /**
     * 快捷链接 后面追加 应用名称
     */
    private String urlAppendApplicationName;

    @Override
    public void start() {
        lineOfCallerConverter.start();
        methodOfCallerConverter.start();
        classOfCallerConverter.start();
        methodOfCallerConverter.start();
        throwableProxyConverter.start();
        ip = IpUtils.getIpAddress();
        super.start();
    }

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
    };

    private LineOfCallerConverter lineOfCallerConverter = new LineOfCallerConverter();
    private MethodOfCallerConverter methodOfCallerConverter = new MethodOfCallerConverter();
    private ClassOfCallerConverter classOfCallerConverter = new ClassOfCallerConverter();
    private ThrowableProxyConverter throwableProxyConverter = new ThrowableProxyConverter();

    @Override
    public String doLayout(ILoggingEvent event) {
        if (!isStarted()) {
            return CoreConstants.EMPTY_STRING;
        }
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(this.getPresentationHeader())) {
            sb.append("## ").append(this.getPresentationHeader()).append("\n");
        }
        this.markdownTextAppend(sb, "env", env);
        this.markdownTextAppend(sb, "app", app);
        this.markdownTextAppend(sb, "ip", ip);
        this.markdownTextAppend(sb, "time", DATE_FORMAT_THREAD_LOCAL.get().format(event.getTimeStamp()));
        this.markdownTextAppend(sb, "thread", event.getThreadName());
        this.markdownTextAppend(sb, "level", event.getLevel().levelStr);
        this.markdownTextAppend(sb, "logger", event.getLoggerName());

        String classConvert = classOfCallerConverter.convert(event);
        this.markdownTextAppend(sb, "class", classConvert);

        String method = methodOfCallerConverter.convert(event);
        this.markdownTextAppend(sb, "method", method);

        String line = lineOfCallerConverter.convert(event);
        this.markdownTextAppend(sb, "line", line);

        this.mdcAppend(sb, event);

        this.markdownTextAppend(sb, "message", event.getFormattedMessage());

        IThrowableProxy tp = event.getThrowableProxy();
        if (tp != null) {
            String stackTrace = throwableProxyConverter.convert(event);
            this.markdownTextAppend(sb, "stackTrace", stackTrace);
        }

        //增加快捷链接
        if (StringUtils.hasText(clickDescription)) {
            if (StringUtils.hasText(urlAppendIp)) {
                this.markdownTextAppendUrl(sb, clickDescription, urlAppendIp + ip, urlAppendIp + ip);
            } else if (StringUtils.hasText(urlAppendApplicationName)) {
                this.markdownTextAppendUrl(sb, clickDescription, urlAppendApplicationName + app, urlAppendApplicationName + app);
            }
        }
        return sb.toString();
    }

    /**
     * md 格式
     *
     * @param sb
     * @param key
     * @param value
     */
    private void markdownTextAppend(StringBuilder sb, String key, String value) {
        sb.append("- ").append(key).append(": ").append(value).append("\n");
    }

    /**
     * 增加链接
     *
     * @param sb
     * @param key
     * @param url
     * @param value
     */
    private void markdownTextAppendUrl(StringBuilder sb, String key, String url, String value) {
        String link = String.format("[%s](%s)", value, url);
        sb.append("- ").append(key).append(": ").append(link).append("\n");
    }

    /**
     * mdc 处理
     *
     * @param sb
     * @param event
     */
    private void mdcAppend(StringBuilder sb, ILoggingEvent event) {
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        for (Map.Entry<String, String> entry : mdcPropertyMap.entrySet()) {
            if (StringUtils.hasText(entry.getKey()) && StringUtils.hasText(entry.getValue())) {
                this.markdownTextAppend(sb, entry.getKey(), entry.getValue());
            }
        }
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getClickDescription() {
        return clickDescription;
    }

    public void setClickDescription(String clickDescription) {
        this.clickDescription = clickDescription;
    }

    public String getUrlAppendIp() {
        return urlAppendIp;
    }

    public void setUrlAppendIp(String urlAppendIp) {
        this.urlAppendIp = urlAppendIp;
    }

    public String getUrlAppendApplicationName() {
        return urlAppendApplicationName;
    }

    public void setUrlAppendApplicationName(String urlAppendApplicationName) {
        this.urlAppendApplicationName = urlAppendApplicationName;
    }
}
