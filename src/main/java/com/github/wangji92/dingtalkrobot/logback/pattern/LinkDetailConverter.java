package com.github.wangji92.dingtalkrobot.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.PropertyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.collect.Lists;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串拼接起来 https://blog.csdn.net/WuLex/article/details/82116701
 *
 * @author 汪小哥
 * @date 19-04-2021
 */
public class LinkDetailConverter extends ClassicConverter {
    String REGEX = "\\[[^]\\]+]";


    private PropertyConverter propertyConverter = new PropertyConverter();


    @Override
    public String convert(ILoggingEvent event) {
        if (getFirstOption() == null) {
            return "";
        }
        String firstOption = getFirstOption();
        Map<String, String> propertyMap = event.getLoggerContextVO().getPropertyMap();
        Pattern compile = Pattern.compile(REGEX);
        Matcher matcher = compile.matcher(getFirstOption());
        Map<String, String> map = new HashMap<>(3);
        while (matcher.find()) {
            for (int i1 = 1; i1 <= matcher.groupCount(); i1++) {
                propertyConverter.setOptionList(Lists.newArrayList(propertyMap.get(matcher.group(i1))));
                String convert = propertyConverter.convert(event);
                if (StringUtils.hasText(convert)) {
                    map.put(matcher.group(i1), convert);
                }
            }
        }
        for (Map.Entry<String, String> keyValue : map.entrySet()) {
            firstOption = firstOption.replace("{" + keyValue.getKey() + "}", keyValue.getValue());
        }
        return firstOption;
    }


}
