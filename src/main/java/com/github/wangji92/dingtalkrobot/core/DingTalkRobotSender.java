package com.github.wangji92.dingtalkrobot.core;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.google.common.util.concurrent.RateLimiter;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author 汪小哥
 * @date 17-04-2021
 */
@Slf4j
public class DingTalkRobotSender {

    /**
     * 钉钉机器人配置 webhook
     */
    private String webhook;
    /**
     * 钉钉机器人加签关键字
     */
    private String signSecret;

    /**
     * 发送速率 [每分钟最多20次] 1/3.5~= 0.2857
     */
    private Double rateLimiterPermitsPerSecond = 0.2857;

    /**
     * 每个机器人每分钟最多发送20条
     */
    private RateLimiter rateLimiter = null;

    public DingTalkRobotSender(String webhook, String signSecret, Double rateLimiterPermitsPerSecond) {
        this.webhook = webhook;
        this.signSecret = signSecret;
        if (rateLimiterPermitsPerSecond == null) {
            rateLimiterPermitsPerSecond = 0.2857;
        }
        rateLimiter = RateLimiter.create(rateLimiterPermitsPerSecond);
    }

    /**
     * 发送钉钉消息
     *
     * @param sendRequest
     */
    public void sendToRobot(OapiRobotSendRequest sendRequest) {
        try {
            //每个机器人每分钟最多发送20条、限流处理一下
            rateLimiter.acquire();
            Long timestamp = System.currentTimeMillis();
            String url = webhook;
            if (StringUtils.hasText(signSecret)) {
                String sign = getSign(timestamp);
                url = url + "&timestamp=" + timestamp + "&sign=" + sign;
            }
            DingTalkClient client = new DefaultDingTalkClient(url);
            try {
                OapiRobotSendResponse response = client.execute(sendRequest);
                if (!response.isSuccess()) {
                    System.out.println(String.format("send dingtalk errorCode=%s errorMsg=%s", response.getErrcode(), response.getErrmsg()));
                }
            } catch (ApiException e) {
                System.out.println(String.format("send dingtalk api error=%s", e.getMessage()));
            }
        } catch (Exception e) {
            /**
             * 这里不能写日志了
             * @see ch.qos.logback.core.UnsynchronizedAppenderBase#addError(String)
             */
            System.out.println(String.format("send dingtalk error last=%s", e.getMessage()));
        }
    }

    /**
     * 签名
     *
     * @param timestamp
     * @return
     */
    private String getSign(Long timestamp) {
        String secret = this.signSecret;
        try {
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return URLEncoder.encode(new String(Base64.getEncoder().encode(signData)), StandardCharsets.UTF_8.name());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return "";
    }
}
