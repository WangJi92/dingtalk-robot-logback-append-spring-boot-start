package com.github.wangji92.logbackappenddemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟服务不正常
 *
 * @author 汪小哥
 * @date 02-04-2021
 */
@RestController
@Slf4j
public class TestController {

    /**
     * 打印日志
     *
     * @param message
     * @return
     */
    @GetMapping("/logError")
    public ResponseEntity<Integer> logError(@RequestParam(required = false) String message) {
        try {
            doException();
        } catch (Exception e) {
            log.error("exception ", e);
        }

        MDC.put("testMdc", "testMdc");
        MDC.put("testMdc2", "testMdc2");

        return ResponseEntity.ok(200);
    }

    int doException() {
        int a = 1 / 0;
        return a;
    }

}
