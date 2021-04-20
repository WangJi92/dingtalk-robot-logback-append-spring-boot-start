package com.github.wangji92.dingtalkrobot.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中括号中变量处理  比如 https://kaifa.baidu.com/searchPage?w=[localIp]
 * https://blog.csdn.net/WuLex/article/details/82116701
 * https://xie.infoq.cn/article/73d2aa78bac23f1bd2731ed23
 *
 * @author 汪小哥
 * @date 19-04-2021
 */
public class CenterBracketsTemplateConverter extends ClassicConverter {

    private static final String REGEX = "(?<=\\[)([a-zA-Z0-9]*)(?=\\])";

    /**
     * 缓存值 第一次处理即可
     */
    private String convertContentCache = "";

    @Override
    public String convert(ILoggingEvent event) {
        if (StringUtils.hasText(convertContentCache)) {
            return convertContentCache;
        }

        if (getFirstOption() == null) {
            return "";
        }
        String firstOption = getFirstOption();
        Context context = getContext();
        Pattern compile = Pattern.compile(REGEX);
        Matcher matcher = compile.matcher(getFirstOption());
        Map<String, String> map = new HashMap<>(3);
        while (matcher.find()) {
            for (int index = 1; index <= matcher.groupCount(); index++) {
                String propertyValue = context.getProperty(matcher.group(index));
                if (StringUtils.hasText(propertyValue)) {
                    map.put(matcher.group(index), propertyValue);
                }
            }
        }
        for (Map.Entry<String, String> keyValue : map.entrySet()) {
            firstOption = firstOption.replace("[" + keyValue.getKey() + "]", keyValue.getValue());
        }
        convertContentCache = firstOption;
        return firstOption;
    }


}
