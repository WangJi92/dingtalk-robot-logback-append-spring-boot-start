package com.github.wangji92.dingtalkrobot.logback.append;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.github.wangji92.dingtalkrobot.core.DingTalkRobotSender;
import com.github.wangji92.dingtalkrobot.logback.layout.DingTalkRobotLayout;


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
     * 格式处理
     */
    private DingTalkRobotLayout layout;


    private DingTalkRobotSender dingTalkRobotSender;

    public DingTalkRobotAppend() {
        super();
        super.setName("dingTalkRobot");
    }

    public DingTalkRobotLayout getLayout() {
        return layout;
    }

    public void setLayout(DingTalkRobotLayout layout) {
        this.layout = layout;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        String logMarkdownText = layout.doLayout(eventObject);
        if (dingTalkRobotSender != null) {
            OapiRobotSendRequest oapiRobotSendRequest = new OapiRobotSendRequest();
            oapiRobotSendRequest.setMsgtype("markdown");

            OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
            markdown.setText(logMarkdownText);
            markdown.setTitle(layout.getPresentationHeader());

            oapiRobotSendRequest.setMarkdown(markdown);
            dingTalkRobotSender.sendToRobot(oapiRobotSendRequest);
        }
    }

    @Override
    public void start() {
        if (webhook != null && webhook.length() > 0) {
            dingTalkRobotSender = new DingTalkRobotSender(webhook, signSecret);
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
}
