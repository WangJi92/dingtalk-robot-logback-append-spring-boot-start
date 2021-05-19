package com.github.wangji92.dingtalkrobot.logback.layout;

import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.pattern.MethodOfCallerConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import com.github.wangji92.dingtalkrobot.logback.pattern.CenterBracketsTemplateConverter;
import com.github.wangji92.dingtalkrobot.utils.IpUtils;
import com.google.common.collect.Lists;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;
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
    private String clickUrl;

    /**
     * 需要打印的mdc的信息
     */
    private List<String> mdcList = Lists.newArrayList();

    @Override
    public void start() {
        lineOfCallerConverter.start();
        methodOfCallerConverter.start();
        classOfCallerConverter.start();
        methodOfCallerConverter.start();
        throwableProxyConverter.setOptionList(Lists.newArrayList("5"));
        throwableProxyConverter.start();
        ip = IpUtils.getIpAddress();

        //处理动态的属性 https://kaifa.baidu.com/searchPage?wd=[localIp]  链接中的动态变量
        centerBracketsTemplateConverter.setOptionList(Lists.newArrayList(clickUrl));
        centerBracketsTemplateConverter.setContext(getContext());
        centerBracketsTemplateConverter.start();
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
    private CenterBracketsTemplateConverter centerBracketsTemplateConverter = new CenterBracketsTemplateConverter();

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
        this.markdownTextAppend(sb, "thread", event.getThreadName());
        this.markdownTextAppend(sb, "level", event.getLevel().levelStr);
//        this.markdownTextAppend(sb, "logger", event.getLoggerName());

//        String classConvert = classOfCallerConverter.convert(event);
//        this.markdownTextAppend(sb, "class", classConvert);

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
        if (StringUtils.hasText(clickDescription) && StringUtils.hasText(clickUrl)) {
            String clickUrlDetail = centerBracketsTemplateConverter.convert(event);
            this.markdownTextAppendUrl(sb, clickDescription, clickUrlDetail, clickUrlDetail);
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
        if (StringUtils.hasText(value)) {
            sb.append("- ").append(key).append(": ").append(value).append("\n");
        }
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
            if (StringUtils.hasText(entry.getKey()) && StringUtils.hasText(entry.getValue()) && mdcList.contains(entry.getKey())) {
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

    public String getClickUrl() {
        return clickUrl;
    }

    public void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    public List<String> getMdcList() {
        return mdcList;
    }

    public void setMdcList(List<String> mdcList) {
        if (mdcList == null) {
            return;
        }
        this.mdcList = mdcList;
    }
}
