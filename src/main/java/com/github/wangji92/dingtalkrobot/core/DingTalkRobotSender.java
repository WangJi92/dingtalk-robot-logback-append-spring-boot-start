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
     * 每个机器人每分钟最多发送20条
     */
    private RateLimiter rateLimiter = null;

    public DingTalkRobotSender(String webhook, String signSecret) {
        this.webhook = webhook;
        this.signSecret = signSecret;
        rateLimiter = RateLimiter.create(1 / 3.0);
    }

    /**
     * 发送钉钉消息
     *
     * @param sendRequest
     */
    public void sendToRobot(OapiRobotSendRequest sendRequest) {
        //每个机器人每分钟最多发送20条
        try {
            rateLimiter.acquire();
            Long timestamp = System.currentTimeMillis();
            String url = webhook;
            if (StringUtils.hasText(signSecret)) {
                String sign = getSign(timestamp);
                url = url + "&timestamp=" + timestamp + "&sign=" + sign;
            }
            DingTalkClient client = new DefaultDingTalkClient(url);
            try {
                OapiRobotSendResponse execute = client.execute(sendRequest);
                if (!execute.isSuccess()) {
                    log.debug("send dingtalk errorCode={} errorMsg={}", execute.getErrcode(), execute.getErrmsg());
                }
            } catch (ApiException e) {
                log.debug("send dingtalk api error", e);
            }
        } catch (Exception e) {
            log.debug("send dingtalk error last", e);
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
