package com.github.wangji92.dingtalkrobot.logback.append;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.github.wangji92.dingtalkrobot.core.DingTalkRobotSender;

import java.nio.charset.StandardCharsets;


/**
 * 实现异步 日志队列
 *
 * @author 汪小哥
 * @date 17-04-2021
 */
public class DingTalkRobotAppend extends UnsynchronizedAppenderBase<ILoggingEvent> {
    /**
     * 钉钉机器人配置 webhook
     */
    private String webhook;
    /**
     * 钉钉机器人加签关键字
     */
    private String signSecret;
    /**
     * 通知标题
     */
    private String robotTitle;

    /**
     * 发送速率 [每分钟最多20次] 1/3.5~= 0.2857
     */
    private Double rateLimiterPermitsPerSecond = 0.2857;

    /**
     * 定义 layout 处理器 Encode
     *
     * @see PatternLayoutEncoder
     * @see LayoutWrappingEncoder
     * {@literal http://logback.qos.ch/manual/encoders.html}
     */
    private Encoder<ILoggingEvent> encoder;
    /**
     * 发送钉钉机器人消息
     */
    private DingTalkRobotSender dingTalkRobotSender;

    public DingTalkRobotAppend() {
        super();
        super.setName("dRobot");
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (encoder == null) {
            addWarn("encoder is null");
            return;
        }
        byte[] encodeBytes = encoder.encode(eventObject);
        if (dingTalkRobotSender != null) {
            OapiRobotSendRequest oapiRobotSendRequest = new OapiRobotSendRequest();
            oapiRobotSendRequest.setMsgtype("markdown");

            OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
            markdown.setText(new String(encodeBytes, StandardCharsets.UTF_8));
            markdown.setTitle(robotTitle);

            oapiRobotSendRequest.setMarkdown(markdown);
            dingTalkRobotSender.sendToRobot(oapiRobotSendRequest);
        }
    }

    @Override
    public void start() {
        if (webhook != null && webhook.length() > 0) {
            dingTalkRobotSender = new DingTalkRobotSender(webhook, signSecret,rateLimiterPermitsPerSecond);
            super.start();
        }
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getSignSecret() {
        return signSecret;
    }

    public void setSignSecret(String signSecret) {
        this.signSecret = signSecret;
    }


    /**
     * 设置 layout
     *
     * @param layout
     */
    public void setLayout(Layout<ILoggingEvent> layout) {
        LayoutWrappingEncoder<ILoggingEvent> customLayoutEncoder = new LayoutWrappingEncoder<ILoggingEvent>();
        customLayoutEncoder.setLayout(layout);
        customLayoutEncoder.setContext(context);
        this.encoder = customLayoutEncoder;

    }

    public Double getRateLimiterPermitsPerSecond() {
        return rateLimiterPermitsPerSecond;
    }

    public void setRateLimiterPermitsPerSecond(Double rateLimiterPermitsPerSecond) {
        this.rateLimiterPermitsPerSecond = rateLimiterPermitsPerSecond;
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    public String getRobotTitle() {
        return robotTitle;
    }

    public void setRobotTitle(String robotTitle) {
        this.robotTitle = robotTitle;
    }
}
