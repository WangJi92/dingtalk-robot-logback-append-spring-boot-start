package com.github.wangji92.dingtalkrobot.logback.property;

import ch.qos.logback.core.PropertyDefinerBase;
import com.github.wangji92.dingtalkrobot.utils.IpUtils;

/**
 * how to define value {@literal https://blog.csdn.net/acohi68664/article/details/102178465}
 * 然后在logback.xml中，添加 <define> 配置，指定属性名（本例中为localIP）及获取属性值的实现类，这样就可以在配置中通过 ${localIp}来引用该属性值了。
 * <define name="localIp" class="com.github.wangji92.dingtalkrobot.logback.property.LocalIpDefiner"/>
 *
 * @author 汪小哥
 * @date 18-04-2021
 */
public class LocalIpDefiner extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        return IpUtils.getIpAddress();
    }
}
