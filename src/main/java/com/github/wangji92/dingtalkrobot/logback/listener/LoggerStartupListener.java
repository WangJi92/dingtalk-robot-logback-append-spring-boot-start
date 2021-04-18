package com.github.wangji92.dingtalkrobot.logback.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import com.github.wangji92.dingtalkrobot.logback.property.LocalIpDefiner;

/**
 * @author 汪小哥
 * @date 18-04-2021
 */
public class LoggerStartupListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {

    private boolean started = false;

    private LocalIpDefiner localIpDefiner = new LocalIpDefiner();

    @Override
    public boolean isResetResistant() {
        return false;
    }

    @Override
    public void onStart(LoggerContext context) {

    }

    @Override
    public void onReset(LoggerContext context) {

    }

    @Override
    public void onStop(LoggerContext context) {

    }

    @Override
    public void onLevelChange(Logger logger, Level level) {

    }

    @Override
    public void start() {
        if (started) {
            return;
        }
        context.putProperty("localIp", localIpDefiner.getPropertyValue());
        started = true;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return started;
    }
}
